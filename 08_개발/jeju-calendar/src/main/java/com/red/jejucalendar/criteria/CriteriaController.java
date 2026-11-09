package com.red.jejucalendar.criteria;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.red.jejucalendar.auth.CurrentUser;
import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.criteria.CriteriaService.CriteriaRequest;
import com.red.jejucalendar.domain.RecommendationCriteria;

/** SCR-A-001-P02 추천 기준·근거 편집 (서비스 운영자 전용) */
@RestController
@RequestMapping("/api/admin/criteria")
public class CriteriaController {

    private final CriteriaService service;

    public CriteriaController(CriteriaService service) {
        this.service = service;
    }

    /** 현재 기준 (아직 저장한 기준이 없으면 204) */
    @GetMapping
    public ResponseEntity<RecommendationCriteria> current(@CurrentUser LoginUser user) {
        return service.current(user).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping
    public RecommendationCriteria save(@CurrentUser LoginUser user, @RequestBody CriteriaRequest request) {
        return service.save(user, request);
    }
}
