package noctem.alertServer.alert.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import noctem.alertServer.global.common.JwtDataExtractor;
import noctem.alertServer.global.common.SinkSessionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

/***
 * PURCHASE_FROM_USER_TOPIC : 유저결제 -> 매장에게 알림
 * ORDER_CANCEL_FROM_USER_TOPIC : 유저가 주문 취소 -> 매장에게 알림
 */
@Slf4j
@RestController
@RequestMapping("${global.api.base-path}/store")
@RequiredArgsConstructor
public class AlertToStoreController {
    private final SinkSessionRegistry sinkSessionRegistry;
    private final JwtDataExtractor jwtDataExtractor;

    @GetMapping(path = "", produces = "text/event-stream;charset=utf-8")
    public Flux<String> streamStoreEvent(ServerWebExchange exchange) {
        Long storeId = jwtDataExtractor.extractStoreId(exchange);
        log.info("{}번 매장에서 연결 요청", storeId);
        return sinkSessionRegistry.getOrRegisterStoreSinkSession(storeId)
                .getSink().asFlux().log();
    }

    // == test용 dev code ==
    @GetMapping(path = "/{storeId}", produces = "text/event-stream;charset=utf-8")
    public Flux<String> streamStoreEventDev(@PathVariable Long storeId) {
        log.info("{}번 매장에서 연결 요청", storeId);
        return sinkSessionRegistry.getOrRegisterStoreSinkSession(storeId)
                .getSink().asFlux().log();
    }

    @GetMapping(path = "/jwt/{encodedJwt}", produces = "text/event-stream;charset=utf-8")
    public Flux<String> streamStoreEventJwtDev(@PathVariable String encodedJwt) {
        Long storeId = jwtDataExtractor.extractStoreIdFromJwt(encodedJwt);
        log.info("{}번 매장에서 연결 요청", storeId);
        return sinkSessionRegistry.getOrRegisterStoreSinkSession(storeId)
                .getSink().asFlux().log();
    }
}
