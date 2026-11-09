package com.red.jejucalendar.auth;

import java.io.Serializable;

import com.red.jejucalendar.common.ApiException;
import com.red.jejucalendar.common.Codes;

/** 세션에 저장하는 로그인 사용자 (비밀번호 해시는 담지 않음) */
public record LoginUser(long userId, String email, String role) implements Serializable {

    public static final String SESSION_KEY = "loginUser";

    public boolean isServiceOperator() {
        return Codes.ROLE_SERVICE.equals(role);
    }

    /** 자료·기준 관리(SCR-A-001)는 서비스 운영자만 사용함 */
    public void requireServiceOperator() {
        if (!isServiceOperator()) {
            throw ApiException.forbidden("자료·기준 관리는 서비스 운영자만 사용할 수 있습니다.");
        }
    }
}
