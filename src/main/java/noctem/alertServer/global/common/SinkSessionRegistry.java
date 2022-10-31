package noctem.alertServer.global.common;

import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class SinkSessionRegistry {
    public final HashMap<Long, IncludedTimeSink> storeSinksMap = new HashMap<>();
    public final HashMap<Long, IncludedTimeSink> userSinksMap = new HashMap<>();

    public IncludedTimeSink getOrRegisterStoreSinkSession(Long storeId) {
        if (storeSinksMap.get(storeId) == null) {
            IncludedTimeSink sink = new IncludedTimeSink().createStoreSink();
            storeSinksMap.put(storeId, sink);
            return sink;
        } else {
            return storeSinksMap.get(storeId);
        }
    }

    public IncludedTimeSink getOrRegisterUserSinkSession(Long userAccountId) {
        if (userSinksMap.get(userAccountId) == null) {
            IncludedTimeSink sink = new IncludedTimeSink().createUserSink();
            userSinksMap.put(userAccountId, sink);
            return sink;
        } else {
            return userSinksMap.get(userAccountId);
        }
    }

    public IncludedTimeSink getStoreSinkSession(Long storeId) {
        return storeSinksMap.containsKey(storeId) ? storeSinksMap.get(storeId) : null;
    }

    public IncludedTimeSink getUserSinkSession(Long userAccountId) {
        return userSinksMap.containsKey(userAccountId) ? userSinksMap.get(userAccountId) : null;
    }

    public void expireUserSession(Long userAccountId) {
        IncludedTimeSink session = userSinksMap.get(userAccountId);
        if (session != null) {
            session.getSink().tryEmitComplete();
        }
        userSinksMap.remove(userAccountId);
    }
}
