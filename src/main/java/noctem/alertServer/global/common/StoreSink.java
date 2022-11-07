package noctem.alertServer.global.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.time.LocalDateTime;

@Slf4j
@Getter
public class StoreSink {
    private Sinks.Many<String> sink;
    private Long storeId;
    private LocalDateTime connectionDateTime;

    public StoreSink(Long storeId) {
        this.sink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
        this.storeId = storeId;
        this.connectionDateTime = LocalDateTime.now();
    }

    public StoreSink emitNext(String message) {
        try {
            sink.tryEmitNext(message);
        } catch (Exception exception) {
            log.info("exception in StoreSink.emitNext");
        }
        return this;
    }
}
