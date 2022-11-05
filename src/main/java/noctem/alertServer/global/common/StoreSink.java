package noctem.alertServer.global.common;

import lombok.Getter;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.time.LocalDateTime;

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
}
