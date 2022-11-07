package noctem.alertServer.alert.domain.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisRepositoryImpl implements RedisRepository {
    private final RedisTemplate<String, String> redisStringTemplate;
    private final String ALERT_MESSAGE_TO_USER_KEY_PREFIX = "alert:message:user";
    private final long NUMBER_TO_SAVE = 20L;

    @Override
    public void pushResponseMessage(Long userAccountId, String responseMessage) {
        String key = String.format("%s:%d", ALERT_MESSAGE_TO_USER_KEY_PREFIX, userAccountId);
        ListOperations<String, String> opsForList = redisStringTemplate.opsForList();
        opsForList.leftPush(key, responseMessage);
        long size = opsForList.size(key).longValue();
        if (size > NUMBER_TO_SAVE) {
            opsForList.rightPop(key);
        }
    }

    @Override
    public String getLastResponseMessage(Long userAccountId) {
        String key = String.format("%s:%d", ALERT_MESSAGE_TO_USER_KEY_PREFIX, userAccountId);
        List<String> list = redisStringTemplate.opsForList().range(key, 0, 0);
        return list.size() != 0 ? list.get(0) : null;
    }
}
