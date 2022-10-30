package noctem.alertServer.global.common;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.util.HashMap;

@Component
public class SinkSessionRegistry {
    public final HashMap<Long, Sinks.Many<String>> storeSinksMap = new HashMap<>();
    public final HashMap<Long, Sinks.Many<String>> userSinksMap = new HashMap<>();

    public Sinks.Many<String> getOrRegisterStoreSinkSession(Long storeId) {
        if (storeSinksMap.get(storeId) == null) {
            Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
            storeSinksMap.put(storeId, sink);
            return sink;
        } else {
            return storeSinksMap.get(storeId);
        }
    }

    public Sinks.Many<String> getOrRegisterUserSinkSession(Long userAccountId) {
        Sinks.Many<String> userSink = userSinksMap.get(userAccountId);
        if (userSinksMap.get(userAccountId) != null) {
            userSink.tryEmitComplete();
        }
        return Sinks.many().unicast().onBackpressureBuffer();
    }

    public Sinks.Many<String> getStoreSinkSession(Long storeId) {
        return storeSinksMap.containsKey(storeId) ? storeSinksMap.get(storeId) : null;
    }

    public Sinks.Many<String> getUserSinkSession(Long userAccountId) {
        return userSinksMap.containsKey(userAccountId) ? userSinksMap.get(userAccountId) : null;
    }

    public void expireUserSession(Long userAccountId) {
        Sinks.Many<String> session = userSinksMap.get(userAccountId);
        if (session != null) {
            session.tryEmitComplete();
        }
        userSinksMap.remove(userAccountId);
    }
}
