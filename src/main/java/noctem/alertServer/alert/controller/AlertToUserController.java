package noctem.alertServer.alert.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import noctem.alertServer.AppConfig;
import noctem.alertServer.alert.domain.repository.RedisRepository;
import noctem.alertServer.alert.dto.response.OrderStatusChangeFromStoreDto;
import noctem.alertServer.alert.vo.OrderCancelFromStoreVo;
import noctem.alertServer.alert.vo.OrderStatusChangeFromStoreVo;
import noctem.alertServer.global.common.AlertCommonResponse;
import noctem.alertServer.global.common.JwtDataExtractor;
import noctem.alertServer.global.common.SinkSessionRegistry;
import noctem.alertServer.global.common.UserSink;
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
    private final JwtDataExtractor jwtDataExtractor;
    private final RedisRepository redisRepository;

    @GetMapping(path = "/jwt/{encodedJwt}/lastMessage", produces = MediaType.APPLICATION_JSON_VALUE)
    public String lastMessageJwt(@PathVariable String encodedJwt) {
        Long userAccountId = jwtDataExtractor.extractUserAccountIdFromJwt(encodedJwt);
        String lastResponseMessage = redisRepository.getLastResponseMessage(userAccountId);
        return lastResponseMessage != null ? lastResponseMessage : AlertCommonResponse.builder().build().convertToString();
    }

    @GetMapping(path = "/jwt/{encodedJwt}/{storeId}", produces = "text/event-stream;charset=utf-8")
    public Flux<String> streamUserEventJwtDev(@PathVariable String encodedJwt, @PathVariable Long storeId) {
        Long userAccountId = jwtDataExtractor.extractUserAccountIdFromJwt(encodedJwt);
        log.info("{}번 유저가 연결 요청", userAccountId);
        return sinkSessionRegistry.getOrRegisterUserSinkSession(userAccountId, storeId)
                .getSink().asFlux().log();
    }

    @GetMapping(path = "/{userAccountId}/{storeId}", produces = "text/event-stream;charset=utf-8")
    public Flux<String> streamUserEventDev(@PathVariable Long userAccountId, @PathVariable Long storeId) {
        log.info("{}번 유저가 연결 요청", userAccountId);
        return sinkSessionRegistry.getOrRegisterUserSinkSession(userAccountId, storeId)
                .getSink().asFlux().log();
    }

    // == test용 dev code ==
    @GetMapping(path = "/{storeId}", produces = "text/event-stream;charset=utf-8")
    public Flux<String> streamUserEvent(ServerWebExchange exchange, @PathVariable Long storeId) {
        Long userAccountId = jwtDataExtractor.extractUserAccountId(exchange);
        log.info("{}번 유저가 연결 요청", userAccountId);
        return sinkSessionRegistry.getOrRegisterUserSinkSession(userAccountId, storeId)
                .getSink().asFlux().log();
    }

    @GetMapping(path = "/{userAccountId}/lastMessage", produces = MediaType.APPLICATION_JSON_VALUE)
    public String lastMessage(@PathVariable Long userAccountId) {
        return redisRepository.getLastResponseMessage(userAccountId);
    }


    @KafkaListener(topics = {ORDER_STATUS_CHANGE_FROM_STORE_TOPIC})
    public void orderStatusChangeFromStore(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        OrderStatusChangeFromStoreVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), OrderStatusChangeFromStoreVo.class);
        log.info("purchaseId={} 제조상태 {}로 변경", vo.getPurchaseId(), vo.getOrderStatus());
        // 본인 매장에 알림 전송
        sendAlertSameStore(vo.getStoreId());
        // 주체 유저에게 알림 전송
        String responseMessage = null;
        UserSink session = sinkSessionRegistry.getUserSinkSession(vo.getUserAccountId());
        if (OrderStatus.MAKING.getValue().equals(vo.getOrderStatus())) {
            log.info("orderStatusChangeFromStore MAKING");
            responseMessage = AlertCommonResponse.builder()
                    .message(String.format("A-%d번 음료가 제조중이에요.", vo.getOrderNumber()))
                    .alertCode(3)
                    .data(new OrderStatusChangeFromStoreDto(vo.getUserAccountId(), vo.getPurchaseId(), vo.getOrderStatus()))
                    .build()
                    .convertToString();
            redisRepository.pushResponseMessage(vo.getUserAccountId(), responseMessage);
        } else if (OrderStatus.COMPLETED.getValue().equals(vo.getOrderStatus())) {
            log.info("orderStatusChangeFromStore COMPLETED");
            responseMessage = AlertCommonResponse.builder()
                    .message(String.format("A-%d번 음료가 완성되었어요. 음료를 찾으러 와주세요.", vo.getOrderNumber()))
                    .alertCode(4)
                    .data(new OrderStatusChangeFromStoreDto(vo.getUserAccountId(), vo.getPurchaseId(), vo.getOrderStatus()))
                    .build()
                    .convertToString();
            redisRepository.pushResponseMessage(vo.getUserAccountId(), responseMessage);
        }
        if (session != null) {
            session.emitNext(responseMessage);
        }
        if (OrderStatus.COMPLETED.getValue().equals(vo.getOrderStatus())) {
            sinkSessionRegistry.disconnectAndDeleteUserSession(vo.getUserAccountId());
        }
        sendAlertMessageOtherUser(vo.getStoreId(), vo.getUserAccountId());
    }

    @KafkaListener(topics = {ORDER_CANCEL_FROM_STORE_TOPIC})
    public void orderCancelFromStore(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        OrderCancelFromStoreVo vo = AppConfig.objectMapper().readValue(consumerRecord.value(), OrderCancelFromStoreVo.class);
        // 본인 매장에 알림 전송
        sendAlertSameStore(vo.getStoreId());
        // 주체 유저에게 알림 전송
        UserSink session = sinkSessionRegistry.getUserSinkSession(vo.getUserAccountId());
        log.info("orderCancelFromStore");
        String responseMessage = AlertCommonResponse.builder()
                .message(String.format("재료 부족으로 인해 A-%d번 주문이 취소되었어요. 카운터에 방문해주세요.", vo.getOrderNumber()))
                .alertCode(5)
                .build()
                .convertToString();
        redisRepository.pushResponseMessage(vo.getUserAccountId(), responseMessage);
        if (session != null) {
            session.emitNext(responseMessage);
        }
        sinkSessionRegistry.disconnectAndDeleteUserSession(vo.getUserAccountId());
        sendAlertMessageOtherUser(vo.getStoreId(), vo.getUserAccountId());
    }

    // subjectUserAccountId: 알림의 주체가 되는 유저
    // sendAlertMessageOtherUser: 주체가 되는 유저 이외의 유저에게 알림전송
    private void sendAlertMessageOtherUser(Long storeId, Long subjectUserAccountId) {
        sinkSessionRegistry.getUserSinksMap().forEach((userAccountId, userSink) -> {
            if (Objects.equals(storeId, userSink.getStoreId())
                    && !Objects.equals(subjectUserAccountId, userAccountId)) {
                userSink.emitNext(AlertCommonResponse.builder()
                        .alertCode(6)
                        .build()
                        .convertToString());
            }
        });
    }

    // 본인 매장에 알림 전송. 다른 포스기에서 주문상태 변경했을 경우
    private void sendAlertSameStore(Long storeId) {
        sinkSessionRegistry.getStoreSinkSession(storeId)
                .emitNext(AlertCommonResponse.builder()
                        .alertCode(7)
                        .build()
                        .convertToString());
    }
}
