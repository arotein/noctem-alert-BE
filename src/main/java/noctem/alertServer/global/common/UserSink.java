package noctem.alertServer.global.common;

import lombok.Getter;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.time.LocalDateTime;

@Getter
public class UserSink {
    private Sinks.Many<String> sink;

    private Long userAccountId;
    private Long storeId;
    private LocalDateTime connectionDateTime;

    public UserSink(Long userAccountId, Long storeId) {
        this.sink = Sinks.many().multicast().onBackpressureBuffer(Queues.XS_BUFFER_SIZE, false);
        this.userAccountId = userAccountId;
        this.storeId = storeId;
        this.connectionDateTime = LocalDateTime.now();
    }

    public UserSink updateStoreId(Long storeId) {
        this.storeId = storeId;
        return this;
    }
}
