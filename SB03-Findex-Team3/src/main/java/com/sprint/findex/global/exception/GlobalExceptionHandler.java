package com.sprint.findex.global.exception;

import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body("잘못된 요청입니다.: " + ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseBody
    public ResponseEntity<String> handleNoSuchElement(NoSuchElementException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body("리소스를 찾을 수 없습니다.: " + ex.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    public ResponseEntity<String> handleNullPointer(NullPointerException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("널포인터 예외가 발생했습니다.: " + ex.getMessage());
    }

    // defalt 예외 처리기
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("서버 내부 오류 발생", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("서버 내부 오류: " + ex.getMessage());
    }
}
