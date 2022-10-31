package noctem.alertServer.global.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class IncludedTimeSink {
    private Sinks.Many<String> sink;
    private LocalDateTime dateTime = LocalDateTime.now();

    public IncludedTimeSink createStoreSink() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
        return this;
    }

    public IncludedTimeSink createUserSink() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer(Queues.XS_BUFFER_SIZE, false);
        return this;
    }
}
