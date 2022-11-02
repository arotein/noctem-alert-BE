package noctem.alertServer.alert.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import noctem.alertServer.AppConfig;
import noctem.alertServer.alert.dto.response.OrderStatusChangeFromStoreDto;
import noctem.alertServer.alert.vo.OrderCancelFromStoreVo;
import noctem.alertServer.alert.vo.OrderStatusChangeFromStoreVo;
import noctem.alertServer.global.common.AlertCommonResponse;
import noctem.alertServer.global.common.IncludedTimeSink;
import noctem.alertServer.global.common.JwtDataExtractor;
import noctem.alertServer.global.common.SinkSessionRegistry;
import noctem.alertServer.global.enumeration.OrderStatus;
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
    private final JwtDataExtractor jwtDataExtractor;

    @GetMapping(path = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamUserEvent(ServerWebExchange exchange) {
        Long userAccountId = jwtDataExtractor.extractUserAccountId(exchange);
        log.info("{}번 유저가 연결 요청", userAccountId);
        return sinkSessionRegistry.getOrRegisterUserSinkSession(userAccountId)
                .getSink().asFlux().log();
    }

    // == test용 dev code ==
    @GetMapping(path = "/{userAccountId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamUserEvent(@PathVariable Long userAccountId) {
        log.info("{}번 유저가 연결 요청", userAccountId);
        return sinkSessionRegistry.getOrRegisterUserSinkSession(userAccountId)
                .getSink().asFlux().log();
    }

    @GetMapping(path = "/jwt/{encodedJwt}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamUserEvent(@PathVariable String encodedJwt) {
        Long userAccountId = jwtDataExtractor.extractUserAccountIdFromJwt(encodedJwt);
        log.info("{}번 유저가 연결 요청", userAccountId);
        return sinkSessionRegistry.getOrRegisterUserSinkSession(userAccountId)
                .getSink().asFlux().log();
    }


    @KafkaListener(topics = {ORDER_STATUS_CHANGE_FROM_STORE_TOPIC})
    public void orderStatusChangeFromStore(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        OrderStatusChangeFromStoreVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), OrderStatusChangeFromStoreVo.class);
        log.info("purchaseId={} 제조상태 {}로 변경", vo.getPurchaseId(), vo.getOrderStatus());
        IncludedTimeSink session = sinkSessionRegistry.getUserSinkSession(vo.getUserAccountId());
        if (session != null) {
            Sinks.Many<String> sink = session.getSink();
            String message = null;
            if (OrderStatus.MAKING.getValue().equals(vo.getOrderStatus())) {
                message = String.format("A-%d번 음료가 제조중이에요.", vo.getOrderNumber());
                sink.tryEmitNext(AlertCommonResponse.builder()
                        .message(message)
                        .alertCode(3)
                        .data(new OrderStatusChangeFromStoreDto(vo.getUserAccountId(), vo.getPurchaseId(), vo.getOrderStatus()))
                        .build()
                        .convertToString());
            } else if (OrderStatus.COMPLETED.getValue().equals(vo.getOrderStatus())) {
                message = String.format("A-%d번 음료가 완성되었어요. 음료를 찾으러 와주세요.", vo.getOrderNumber());
                sink.tryEmitNext(AlertCommonResponse.builder()
                        .message(message)
                        .alertCode(4)
                        .data(new OrderStatusChangeFromStoreDto(vo.getUserAccountId(), vo.getPurchaseId(), vo.getOrderStatus()))
                        .build()
                        .convertToString());
            }
            if (OrderStatus.COMPLETED.getValue().equals(vo.getOrderStatus())) {
                sinkSessionRegistry.expireUserSession(vo.getUserAccountId());
            }
        }
    }

    @KafkaListener(topics = {ORDER_CANCEL_FROM_STORE_TOPIC})
    public void orderCancelFromStore(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        OrderCancelFromStoreVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), OrderCancelFromStoreVo.class);
        IncludedTimeSink session = sinkSessionRegistry.getUserSinkSession(vo.getUserAccountId());
        if (session != null) {
            Sinks.Many<String> sink = session.getSink();
            sink.tryEmitNext(AlertCommonResponse.builder()
                    .message(String.format("재료 부족으로 인해 A-%d번 주문이 취소되었어요. 카운터에 방문해주세요.", vo.getOrderNumber()))
                    .alertCode(5)
//                    .data(new OrderCancelFromStoreResDto())
                    .build()
                    .convertToString());
            sink.tryEmitComplete();
            sinkSessionRegistry.expireUserSession(vo.getUserAccountId());
        }
    }
}
