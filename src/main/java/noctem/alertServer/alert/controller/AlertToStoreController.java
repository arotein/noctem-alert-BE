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

/***
 * PURCHASE_FROM_USER_TOPIC : 유저결제 -> 매장에게 알림
 * ORDER_CANCEL_FROM_USER_TOPIC : 유저가 주문 취소 -> 매장에게 알림
 */
@Slf4j
@RestController
@RequestMapping("${global.api.base-path}/store")
@RequiredArgsConstructor
public class AlertToStoreController {
    // 유저가 결제 -> 매장에 알림
    private final String PURCHASE_FROM_USER_TOPIC = "purchase-from-user-alert";
    // 유저가 주문 취소 -> 매장에 알림
    private final String ORDER_CANCEL_FROM_USER_TOPIC = "order-cancel-from-user-alert";
    private final SinkSessionRegistry sinkSessionRegistry;

    @GetMapping(path = "/{storeId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamStoreEvent(@PathVariable Long storeId, ServerWebExchange exchange) {
        log.info("{}번 매장에서 연결 요청", storeId);
        Sinks.Many<String> sink = sinkSessionRegistry.getOrRegisterStoreSinkSession(storeId);
        return sink.asFlux().log();
    }

    @KafkaListener(topics = {PURCHASE_FROM_USER_TOPIC})
    public void purchaseFromUser(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        try {
            PurchaseFromUserVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), PurchaseFromUserVo.class);
            log.info("{}번 매장에 주문 요청", vo.getStoreId());
            Sinks.Many<String> sink = sinkSessionRegistry.getStoreSinkSession(vo.getStoreId());
            if (sink != null) {
                sink.tryEmitNext(AlertCommonResponse.builder()
                        .message(String.format("%d건의 음료 요청이 있습니다.", vo.getTotalMenuQty()))
                        .data(new PurchaseFromUserResDto())
                        .build()
                        .convertToString());
            }
        } catch (JsonProcessingException e) {
            log.warn("{}", e.getMessage());
        } catch (Exception e) {

        }
    }

    @KafkaListener(topics = {ORDER_CANCEL_FROM_USER_TOPIC})
    public void orderCancelFromUser(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        try {
            OrderCancelFromUserVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), OrderCancelFromUserVo.class);
            log.info("{}번 매장에 주문취소 요청", vo.getStoreId());
            Sinks.Many<String> sink = sinkSessionRegistry.getStoreSinkSession(vo.getStoreId());
            if (sink != null) {
                sink.tryEmitNext(AlertCommonResponse.builder()
                        .message(String.format("%d번 주문이 취소되었습니다.", vo.getOrderNumber()))
                        .data(new OrderCancelFromUserResDto(vo.getOrderNumber()))
                        .build()
                        .convertToString());
            }
            sinkSessionRegistry.expireUserSession(vo.getUserAccountId());
        } catch (Exception e) {

        }
    }
}
