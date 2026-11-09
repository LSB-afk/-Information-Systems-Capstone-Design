package com.red.jejucalendar.common;

import org.springframework.http.HttpStatus;

/**
 * 화면에 그대로 보여줄 오류 (상태 코드, 안내 문구, 문제가 된 입력란)
 * field는 화면에서 해당 입력란에 초점을 옮길 때 사용함 입력란과 관계없으면 null
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String field;

    public ApiException(HttpStatus status, String message, String field) {
        super(message);
        this.status = status;
        this.field = field;
    }

    public static ApiException badRequest(String field, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message, field);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, message, null);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message, null);
    }

    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message, null);
    }

    public static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message, null);
    }

    public HttpStatus status() {
        return status;
    }

    public String field() {
        return field;
    }
}
