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
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("${global.api.base-path}/user")
@RequiredArgsConstructor
public class AlertToUserController {
    // 매장에서 주문 상태변경 -> 유저에 알림
    private final String ORDER_STATUS_CHANGE_FROM_STORE_TOPIC = "order-status-change-from-store-alert";
    // 매장에서 주문 취소 -> 유저에 알림
    private final String ORDER_CANCEL_FROM_STORE_TOPIC = "order-cancel-from-store-alert";
    private final HashMap<Long, Sinks.Many<String>> userSinksMap = new HashMap<>();

    @GetMapping(path = "/{userAccountId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamUserEvent(@PathVariable Long userAccountId) {
        Sinks.Many<String> sink;
        if (userSinksMap.get(userAccountId) != null) {
            sink = userSinksMap.get(userAccountId);
        } else {
            sink = Sinks.many().multicast().onBackpressureBuffer();
            userSinksMap.put(userAccountId, sink);
        }
        return sink.asFlux();
    }

    @KafkaListener(topics = {ORDER_STATUS_CHANGE_FROM_STORE_TOPIC})
    public void orderStatusChangeFromStore(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        OrderStatusChangeFromStoreVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), OrderStatusChangeFromStoreVo.class);
        Sinks.Many<String> sink = userSinksMap.get(vo.getUserAccountId());
        if (sink != null) {
            sink.tryEmitNext(AlertCommonResponse.builder()
                    .message(String.format("음료 제조상태가 %s로 변경되었습니다.", vo.getPurchaseId()))
                    .data(new OrderStatusChangeFromStoreDto(vo.getUserAccountId(), vo.getPurchaseId(), vo.getOrderStatus()))
                    .build()
                    .convertToString());
        }
    }

    @KafkaListener(topics = {ORDER_CANCEL_FROM_STORE_TOPIC})
    public void orderCancelFromStore(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        OrderCancelFromStoreVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), OrderCancelFromStoreVo.class);
        Sinks.Many<String> sink = userSinksMap.get(vo.getUserAccountId());
        if (sink != null) {
            sink.tryEmitNext(AlertCommonResponse.builder()
                    .message(String.format("재고 부족으로 인해 주문이 취소되었습니다. 카운터에 방문해주세요."))
                    .data(new OrderCancelFromStoreResDto(vo.getOrderStatus()))
                    .build()
                    .convertToString());
        }
    }
}
