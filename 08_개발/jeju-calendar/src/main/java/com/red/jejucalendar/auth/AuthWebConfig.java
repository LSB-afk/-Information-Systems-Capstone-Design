package com.red.jejucalendar.auth;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.red.jejucalendar.common.ApiException;

/**
 * 로그인 확인 설정
 * Controller에서 @CurrentUser LoginUser 인자를 받으면 로그인이 필요한 요청이 됨
 */
@Configuration
public class AuthWebConfig implements WebMvcConfigurer {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(CurrentUser.class)
                        && LoginUser.class.equals(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mav,
                    NativeWebRequest request, WebDataBinderFactory binderFactory) {
                Object user = request.getAttribute(LoginUser.SESSION_KEY, RequestAttributes.SCOPE_SESSION);
                if (user instanceof LoginUser loginUser) {
                    return loginUser;
                }
                throw ApiException.unauthorized("로그인이 필요합니다.");
            }
        });
    }
}
