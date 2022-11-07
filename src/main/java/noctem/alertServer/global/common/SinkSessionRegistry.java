package noctem.alertServer.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import noctem.alertServer.alert.domain.repository.RedisRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Getter
@Component
@RequiredArgsConstructor
public class SinkSessionRegistry {
    // <storeId, storeSink>
    private final HashMap<Long, StoreSink> storeSinksMap = new HashMap<>();
    // <userAccountId, userSink>
    private final HashMap<Long, UserSink> userSinksMap = new HashMap<>();
    private final RedisRepository redisRepository;

    public StoreSink getOrRegisterStoreSinkSession(Long storeId) {
        if (storeSinksMap.get(storeId) == null) {
            StoreSink sink = new StoreSink(storeId);
            storeSinksMap.put(storeId, sink);
            return sink;
        } else {
            return storeSinksMap.get(storeId);
        }
    }

    public UserSink getOrRegisterUserSinkSession(Long userAccountId, Long storeId) {
        if (userSinksMap.get(userAccountId) == null) {
            UserSink sink = new UserSink(userAccountId, storeId);
            userSinksMap.put(userAccountId, sink);
            return sink;
        } else {
            return userSinksMap.get(userAccountId).updateStoreId(storeId);
        }
    }

    public StoreSink getStoreSinkSession(Long storeId) {
        return storeSinksMap.containsKey(storeId) ? storeSinksMap.get(storeId) : null;
    }

    public UserSink getUserSinkSession(Long userAccountId) {
        return userSinksMap.containsKey(userAccountId) ? userSinksMap.get(userAccountId) : null;
    }

    public void disconnectAndDeleteUserSession(Long userAccountId) {
        UserSink session = userSinksMap.get(userAccountId);
        if (session != null) {
            session.getSink().tryEmitComplete();
        }
        userSinksMap.remove(userAccountId);
    }

    public void disconnectUserSession(Long userAccountId) {
        UserSink session = userSinksMap.get(userAccountId);
        if (session != null) {
            session.getSink().tryEmitComplete();
        }
    }
}
