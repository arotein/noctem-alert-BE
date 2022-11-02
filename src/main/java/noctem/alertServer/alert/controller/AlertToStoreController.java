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
import noctem.alertServer.global.common.JwtDataExtractor;
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
    private final JwtDataExtractor jwtDataExtractor;

    @GetMapping(path = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamStoreEvent(ServerWebExchange exchange) {
        Long storeId = jwtDataExtractor.extractStoreId(exchange);
        log.info("{}번 매장에서 연결 요청", storeId);
        return sinkSessionRegistry.getOrRegisterStoreSinkSession(storeId)
                .getSink().asFlux().log();
    }

    // == test용 dev code ==
    @GetMapping(path = "/{storeId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamStoreEventDev(@PathVariable Long storeId) {
        log.info("{}번 매장에서 연결 요청", storeId);
        return sinkSessionRegistry.getOrRegisterStoreSinkSession(storeId)
                .getSink().asFlux().log();
    }

    @GetMapping(path = "/jwt/{encodedJwt}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamStoreEventJwtDev(@PathVariable String encodedJwt) {
        Long storeId = jwtDataExtractor.extractStoreIdFromJwt(encodedJwt);
        log.info("{}번 매장에서 연결 요청", storeId);
        return sinkSessionRegistry.getOrRegisterStoreSinkSession(storeId)
                .getSink().asFlux().log();
    }

    @KafkaListener(topics = {PURCHASE_FROM_USER_TOPIC})
    public void purchaseFromUser(ConsumerRecord<String, String> consumerRecord) {
        try {
            PurchaseFromUserVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), PurchaseFromUserVo.class);
            log.info("{}번 매장에 주문 요청", vo.getStoreId());
            Sinks.Many<String> sink = sinkSessionRegistry.getStoreSinkSession(vo.getStoreId()).getSink();
            String message;
            if (vo.getTotalMenuQty() == 1) {
                message = String.format("%s", vo.getMenuFullName());
            } else {
                message = String.format("%s 외 %d건", vo.getMenuFullName(), vo.getTotalMenuQty() - 1);
            }
            if (sink != null) {
                sink.tryEmitNext(AlertCommonResponse.builder()
                        .message(message)
                        .alertCode(1)
                        .data(new PurchaseFromUserResDto(vo.getOrderNumber()))
                        .build()
                        .convertToString());
            }
        } catch (JsonProcessingException e) {
            log.warn("{}", e.getMessage());
        } catch (Exception e) {

        }
    }

    @KafkaListener(topics = {ORDER_CANCEL_FROM_USER_TOPIC})
    public void orderCancelFromUser(ConsumerRecord<String, String> consumerRecord) {
        try {
            OrderCancelFromUserVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), OrderCancelFromUserVo.class);
            log.info("{}번 매장에 주문취소 요청", vo.getStoreId());
            Sinks.Many<String> sink = sinkSessionRegistry.getStoreSinkSession(vo.getStoreId()).getSink();
            if (sink != null) {
                sink.tryEmitNext(AlertCommonResponse.builder()
                        .message(String.format("A-%d번 주문이 취소되었습니다.", vo.getOrderNumber()))
                        .alertCode(2)
                        .data(new OrderCancelFromUserResDto(vo.getOrderNumber()))
                        .build()
                        .convertToString());
            }
            sinkSessionRegistry.expireUserSession(vo.getUserAccountId());
        } catch (JsonProcessingException e) {
            log.warn("{}", e.getMessage());
        } catch (Exception e) {

        }
    }
}
