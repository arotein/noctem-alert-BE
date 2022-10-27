package noctem.alertServer.alert.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import noctem.alertServer.AppConfig;
import noctem.alertServer.alert.dto.response.OrderCancelFromUserResDto;
import noctem.alertServer.alert.dto.response.PurchaseFromUserResDto;
import noctem.alertServer.alert.vo.OrderCancelFromUserVo;
import noctem.alertServer.alert.vo.PurchaseFromUserVo;
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
@RequestMapping("${global.api.base-path}/store")
@RequiredArgsConstructor
public class AlertToStoreController {
    // 유저가 결제 -> 매장에 알림
    private final String PURCHASE_FROM_USER_TOPIC = "purchase-from-user-alert";
    // 유저가 주문 취소 -> 매장에 알림
    private final String ORDER_CANCEL_FROM_USER_TOPIC = "order-cancel-from-user-alert";
    private final HashMap<Long, Sinks.Many<String>> storeSinksMap = new HashMap<>();

    @GetMapping(path = "/{storeId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamStoreEvent(@PathVariable Long storeId) {
        Sinks.Many<String> sink;
        if (storeSinksMap.get(storeId) != null) {
            sink = storeSinksMap.get(storeId);
        } else {
            sink = Sinks.many().multicast().onBackpressureBuffer();
            storeSinksMap.put(storeId, sink);
        }
        return sink.asFlux();
    }

    @KafkaListener(topics = {PURCHASE_FROM_USER_TOPIC})
    public void purchaseFromUser(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        PurchaseFromUserVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), PurchaseFromUserVo.class);
        Sinks.Many<String> sink = storeSinksMap.get(vo.getStoreId());
        if (sink != null) {
            sink.tryEmitNext(AlertCommonResponse.builder()
                    .message(String.format("%d건의 새 주문요청이 도착했습니다.", vo.getTotalMenuQty()))
                    .data(new PurchaseFromUserResDto())
                    .build()
                    .convertToString());
        }
    }

    @KafkaListener(topics = {ORDER_CANCEL_FROM_USER_TOPIC})
    public void orderCancelFromUser(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        OrderCancelFromUserVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), OrderCancelFromUserVo.class);
        Sinks.Many<String> sink = storeSinksMap.get(vo.getStoreId());
        if (sink != null) {
            sink.tryEmitNext(AlertCommonResponse.builder()
                    .message(String.format("주문번호%d가 취소되었습니다.", vo.getPurchaseId()))
                    .data(new OrderCancelFromUserResDto(vo.getPurchaseId()))
                    .build()
                    .convertToString());
        }
    }
}
