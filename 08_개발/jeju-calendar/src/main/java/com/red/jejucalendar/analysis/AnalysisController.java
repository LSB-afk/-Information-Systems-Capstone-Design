package com.red.jejucalendar.analysis;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.red.jejucalendar.analysis.AnalysisDtos.ConsumptionResponse;
import com.red.jejucalendar.analysis.AnalysisDtos.TrendResponse;
import com.red.jejucalendar.auth.CurrentUser;
import com.red.jejucalendar.auth.LoginUser;

/** SCR-C-003 지역 분석 (사업체 운영자·서비스 운영자 모두 조회) */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    /** 방문·계절 비교: regionCode·year를 비우면 내 사업체 지역과 가장 최근 연도 */
    @GetMapping("/trend")
    public TrendResponse trend(@CurrentUser LoginUser user,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String metric) {
        return service.trend(user, regionCode, year, metric);
    }

    /** 소비 비교: category는 시간대별 비중을 볼 업종 (기본 CAFE) */
    @GetMapping("/consumption")
    public ConsumptionResponse consumption(@CurrentUser LoginUser user,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String category) {
        return service.consumption(user, regionCode, year, category);
    }
}
