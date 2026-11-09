package com.red.jejucalendar.business;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.red.jejucalendar.auth.CurrentUser;
import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.domain.Business;

/** 로그인 사용자가 계획·기록을 처리할 수 있는 사업체 목록 */
@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessAccessService accessService;

    public BusinessController(BusinessAccessService accessService) {
        this.accessService = accessService;
    }

    @GetMapping
    public List<Business> list(@CurrentUser LoginUser user) {
        return accessService.accessibleBusinesses(user);
    }
}
