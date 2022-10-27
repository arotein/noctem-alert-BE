package noctem.alertServer.global.exception;

import lombok.extern.slf4j.Slf4j;
import noctem.alertServer.global.common.CommonException;
import noctem.alertServer.global.common.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
//    public ResponseEntity runtimeExHandle(RuntimeException ex) {
//        log.error("Exception Name = {}, Code = 8000, Message = {}", ex.getClass().getName(), ex.getMessage());
//        return ResponseEntity.ok()
//                .body(CommonResponse.builder().errorCode(8000).build());
//    }

    @ExceptionHandler
    public ResponseEntity accessDeniedExHandle(AccessDeniedException ex) {
        log.warn("Exception Name = {}, Code = 2001, Message = {}", ex.getClass().getName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.builder().errorCode(2001).httpStatus(HttpStatus.UNAUTHORIZED).build());
    }

    @ExceptionHandler
    public ResponseEntity illegalArgumentExHandle(IllegalArgumentException ex) {
        log.warn("Exception Name = {}, Code = 2003, Message = {}", ex.getClass().getName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.builder().errorCode(2003).httpStatus(HttpStatus.BAD_REQUEST).build());
    }

    @ExceptionHandler
    public ResponseEntity httpMessageNotReadableExHandle(HttpMessageNotReadableException ex) {
        log.warn("Exception Name = {}, Code = 2023, Message = {}", ex.getClass().getName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.builder().errorCode(2023).httpStatus(HttpStatus.BAD_REQUEST).build());
    }

    @ExceptionHandler
    public ResponseEntity commonExHandle(CommonException ex) {
        log.warn("Exception Name = {}, Code = {}, Message = {}", ex.getClass().getName(), ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(CommonResponse.builder().errorCode(ex.getErrorCode()).httpStatus(ex.getHttpStatus()).build());
    }
}
