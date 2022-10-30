package noctem.alertServer.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

/***
 * errorCode: 8000 ~ 8999
 * 사용가능 : 8001 ~
 */
@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {
//    @ExceptionHandler
//    public void runtimeExHandle(RuntimeException ex) {
//        log.error("Exception Name = {}, Code = 8000, Message = {}", ex.getClass().getName(), ex.getMessage());
//    }

    @ExceptionHandler
    public void accessDeniedExHandle(AccessDeniedException ex) {
        log.warn("Exception Name = {}, Code = 8001, Message = {}", ex.getClass().getName(), ex.getMessage());
    }

    @ExceptionHandler
    public void illegalArgumentExHandle(IllegalArgumentException ex) {
        log.warn("Exception Name = {}, Code = 8002, Message = {}", ex.getClass().getName(), ex.getMessage());
    }

//    @ExceptionHandler
//    public void jsonProcessingExHandle(JsonProcessingException ex) {
//        log.warn("Exception Name = {}, Code = 8003, Message = {}, Location = {}", ex.getClass().getName(), ex.getMessage(), ex.getLocation());
//    }
}
