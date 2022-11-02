package noctem.alertServer.global.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import noctem.alertServer.global.common.AlertCommonResponse;
import noctem.alertServer.global.common.CommonException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

/***
 * errorCode: 8000 ~ 8999
 * 사용가능 : 8003 ~
 */
@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {
//    @ExceptionHandler
//    public void runtimeExHandle(RuntimeException ex) {
//        log.error("Exception Name = {}, Code = 8000, Message = {}", ex.getClass().getName(), ex.getMessage());
//    }

    @ExceptionHandler
    public AlertCommonResponse accessDeniedExHandle(AccessDeniedException ex) {
        log.warn("Exception Name = {}, Code = 8001, Message = {}", ex.getClass().getName(), ex.getMessage());
        return AlertCommonResponse.builder().build().setErrorCode(8001);
    }

    @ExceptionHandler
    public AlertCommonResponse illegalArgumentExHandle(IllegalArgumentException ex) {
        log.warn("Exception Name = {}, Code = 8002, Message = {}", ex.getClass().getName(), ex.getMessage());
        return AlertCommonResponse.builder().build().setErrorCode(8002);
    }

    @ExceptionHandler
    public AlertCommonResponse jsonProcessingExHandle(JsonProcessingException ex) {
        log.warn("Exception Name = {}, Code = 8003, Message = {}, Location = {}", ex.getClass().getName(), ex.getMessage(), ex.getLocation());
        return AlertCommonResponse.builder().build().setErrorCode(8003);
    }

    @ExceptionHandler
    public AlertCommonResponse commonExHandle(CommonException ex) {
        log.warn("Exception Name = {}, Code = 8004, Message = {}, Location = {}", ex.getClass().getName(), ex.getMessage());
        return AlertCommonResponse.builder().build().setErrorCode(ex.getErrorCode());
    }
}
