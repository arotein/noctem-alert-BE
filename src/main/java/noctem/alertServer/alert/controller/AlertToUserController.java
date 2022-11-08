package noctem.alertServer.alert.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import noctem.alertServer.alert.service.AlertToUserService;
import noctem.alertServer.global.common.AlertCommonResponse;
import noctem.alertServer.global.common.JwtDataExtractor;
import noctem.alertServer.global.common.SinkSessionRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

/***
 * ORDER_STATUS_CHANGE_FROM_STORE_TOPIC : 매장에서 주문상태 변경 -> 유저에게 알림
 * ORDER_CANCEL_FROM_STORE_TOPIC : 매장에서 주문 반려 -> 유저에게 알림
 */
@Slf4j
@RestController
@RequestMapping("${global.api.base-path}/user")
@RequiredArgsConstructor
public class AlertToUserController {
    private final SinkSessionRegistry sinkSessionRegistry;
    private final JwtDataExtractor jwtDataExtractor;
    private final AlertToUserService alertToUserService;

    @GetMapping(path = "/jwt/{encodedJwt}/lastMessage", produces = MediaType.APPLICATION_JSON_VALUE)
    public String lastMessageJwt(@PathVariable String encodedJwt) {
        Long userAccountId = jwtDataExtractor.extractUserAccountIdFromJwt(encodedJwt);
        String lastResponseMessage = alertToUserService.getLastResponseMessage(userAccountId);
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
        return alertToUserService.getLastResponseMessage(userAccountId);
    }
}
