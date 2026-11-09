package com.red.jejucalendar.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 오류를 { "message": ..., "field": ... } 형태로 돌려줌 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErrorResponse(String message, String field) {
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> handle(ApiException e) {
        return ResponseEntity.status(e.status()).body(new ErrorResponse(e.getMessage(), e.field()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse("입력 형식을 확인해 주세요.", null));
    }

    /** 저장 실패: 화면은 입력값을 유지하고 다시 저장을 안내함 (SCR-B-001-P01 저장 실패) */
    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ErrorResponse> handleDb(DataAccessException e) {
        log.error("DB 처리 실패", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("저장에 실패했습니다. 입력값을 유지했으니 다시 저장해 주세요.", null));
    }
}
