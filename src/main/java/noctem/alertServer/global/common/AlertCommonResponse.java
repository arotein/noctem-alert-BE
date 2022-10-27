package noctem.alertServer.global.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import noctem.alertServer.AppConfig;

@Getter
@Slf4j
public class AlertCommonResponse<T> {
    private String message;
    private T data;

    @Builder
    public AlertCommonResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public String convertToString() {
        try {
            return AppConfig.objectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.warn("JsonProcessingException in AlertCommonResponse");
            this.message = null;
            this.data = null;
            return null;
        }
    }
}
