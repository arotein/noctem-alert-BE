package noctem.alertServer.global.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import noctem.alertServer.AppConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/***
 * alertCode: 8까지 사용.
 */
@Getter
@Slf4j
public class AlertCommonResponse<T> {
    private String message;
    private Integer alertCode;
    private T data;
    private String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
    private Integer errorCode;

    @Builder
    public AlertCommonResponse(String message, Integer alertCode, T data) {
        this.message = message;
        this.alertCode = alertCode;
        this.data = data;
    }

    public String convertToString() {
        try {
            return AppConfig.objectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.warn("JsonProcessingException in AlertCommonResponse");
            this.message = null;
            this.alertCode = null;
            this.data = null;
            return null;
        }
    }

    public AlertCommonResponse setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
        this.message = null;
        this.alertCode = null;
        this.data = null;
        return this;
    }
}
