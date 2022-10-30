package noctem.alertServer.alert.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import noctem.alertServer.AppConfig;
import noctem.alertServer.alert.dto.response.OrderCancelFromStoreResDto;
import noctem.alertServer.alert.dto.response.OrderStatusChangeFromStoreDto;
import noctem.alertServer.alert.vo.OrderCancelFromStoreVo;
import noctem.alertServer.alert.vo.OrderStatusChangeFromStoreVo;
import noctem.alertServer.global.common.AlertCommonResponse;
import noctem.alertServer.global.common.SinkSessionRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Objects;

/***
 * ORDER_STATUS_CHANGE_FROM_STORE_TOPIC : 매장에서 주문상태 변경 -> 유저에게 알림
 * ORDER_CANCEL_FROM_STORE_TOPIC : 매장에서 주문 반려 -> 유저에게 알림
 */
@Slf4j
@RestController
@RequestMapping("${global.api.base-path}/user")
@RequiredArgsConstructor
public class AlertToUserController {
    // 매장에서 주문 상태변경 -> 유저에 알림
    private final String ORDER_STATUS_CHANGE_FROM_STORE_TOPIC = "order-status-change-from-store-alert";
    // 매장에서 주문 취소 -> 유저에 알림
    private final String ORDER_CANCEL_FROM_STORE_TOPIC = "order-cancel-from-store-alert";
    private final SinkSessionRegistry sinkSessionRegistry;

    // /{userAccountId}는 토큰에서
    @GetMapping(path = "/{userAccountId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamUserEvent(@PathVariable Long userAccountId,
                                        ServerWebExchange exchange) {
        log.info("path={}", exchange.getRequest().getPath());
        return sinkSessionRegistry.getOrRegisterUserSinkSession(userAccountId).asFlux();
    }

    @KafkaListener(topics = {ORDER_STATUS_CHANGE_FROM_STORE_TOPIC})
    public void orderStatusChangeFromStore(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        OrderStatusChangeFromStoreVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), OrderStatusChangeFromStoreVo.class);
        log.info("purchaseId={} 제조상태 {}로 변경", vo.getPurchaseId(), vo.getOrderStatus());
        Sinks.Many<String> sink = sinkSessionRegistry.getUserSinkSession(vo.getUserAccountId());
        if (sink != null) {
            sink.tryEmitNext(AlertCommonResponse.builder()
                    .message(String.format("%d번 주문의 음료 제조상태가 %s로 변경되었습니다.", vo.getOrderNumber(), vo.getOrderStatus()))
                    .data(new OrderStatusChangeFromStoreDto(vo.getUserAccountId(), vo.getPurchaseId(), vo.getOrderStatus()))
                    .build()
                    .convertToString());
            if (Objects.equals(vo.getOrderStatus(), "제조완료")) {
                sink.tryEmitComplete();
                sinkSessionRegistry.expireUserSession(vo.getUserAccountId());
            }
        }
    }

    @KafkaListener(topics = {ORDER_CANCEL_FROM_STORE_TOPIC})
    public void orderCancelFromStore(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        OrderCancelFromStoreVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), OrderCancelFromStoreVo.class);
        Sinks.Many<String> sink = sinkSessionRegistry.getUserSinkSession(vo.getUserAccountId());
        if (sink != null) {
            sink.tryEmitNext(AlertCommonResponse.builder()
                    .message(String.format("재료 부족으로 인해 %d번 주문이 취소되었습니다. 카운터에 방문해주세요.", vo.getOrderNumber()))
                    .data(new OrderCancelFromStoreResDto(vo.getOrderStatus()))
                    .build()
                    .convertToString());
            sink.tryEmitComplete();
            sinkSessionRegistry.expireUserSession(vo.getUserAccountId());
        }
    }
}
