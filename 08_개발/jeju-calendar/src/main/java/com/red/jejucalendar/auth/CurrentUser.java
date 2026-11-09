package com.red.jejucalendar.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Controller 메서드 인자에 붙이면 세션의 로그인 사용자를 넣어 줌 (로그인하지 않았으면 401) */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
