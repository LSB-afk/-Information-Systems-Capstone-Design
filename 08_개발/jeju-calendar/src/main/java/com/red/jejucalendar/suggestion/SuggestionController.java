package com.red.jejucalendar.suggestion;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.red.jejucalendar.auth.CurrentUser;
import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.suggestion.SuggestionDtos.Detail;
import com.red.jejucalendar.suggestion.SuggestionDtos.ListResponse;
import com.red.jejucalendar.suggestion.SuggestionDtos.RecalcResult;

/**
 * SCR-C-004 홍보 제안, SCR-C-004-P01 추천 근거 상세
 * 제안 담기는 계획 API를 씀: 개별 담기 POST /api/businesses/{id}/plans (suggestionId),
 * 남은 제안 한 번에 담기 POST /api/businesses/{id}/plans/from-suggestions
 */
@RestController
public class SuggestionController {

    private final SuggestionService service;

    public SuggestionController(SuggestionService service) {
        this.service = service;
    }

    /** regionCode·businessId를 비우면 처리할 수 있는 첫 사업체와 그 지역 */
    @GetMapping("/api/suggestions")
    public ListResponse list(@CurrentUser LoginUser user,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) Long businessId) {
        return service.list(user, regionCode, businessId);
    }

    @GetMapping("/api/suggestions/{suggestionId}")
    public Detail detail(@CurrentUser LoginUser user, @PathVariable long suggestionId,
            @RequestParam(required = false) Long businessId) {
        return service.detail(user, suggestionId, businessId);
    }

    /** 서비스 운영자용 수동 재계산 (기준 저장·방문 자료 등록 때는 자동으로 다시 계산함) */
    @PostMapping("/api/admin/suggestions/recalculate")
    public RecalcResult recalculate(@CurrentUser LoginUser user) {
        return service.recalculate(user);
    }
}
