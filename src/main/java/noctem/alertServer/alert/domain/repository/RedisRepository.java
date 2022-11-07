package noctem.alertServer.alert.domain.repository;

public interface RedisRepository {
    void pushResponseMessage(Long userAccountId, String responseMessage);

    String getLastResponseMessage(Long userAccountId);
}
