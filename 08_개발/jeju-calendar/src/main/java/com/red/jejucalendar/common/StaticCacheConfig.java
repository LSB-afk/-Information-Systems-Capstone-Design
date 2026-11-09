package com.red.jejucalendar.common;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

/**
 * 화면 파일에 내용 기반 ETag를 붙임
 * 화면 파일을 고쳐 다시 배포하면 내용이 바뀌어 ETag가 달라지므로 브라우저가 새 파일을 받음
 */
@Configuration
public class StaticCacheConfig {

    @Bean
    FilterRegistrationBean<ShallowEtagHeaderFilter> staticEtagFilter() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> bean = new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        // "/"는 index.html로 넘기는(forward) 요청이라 이 필터를 걸면 본문이 비므로 제외함 (no-cache라 매번 새로 받음)
        bean.addUrlPatterns("/index.html", "/app.js", "/styles.css");
        return bean;
    }
}
