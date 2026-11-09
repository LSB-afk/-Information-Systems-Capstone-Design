package com.red.jejucalendar.criteria;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.common.ApiException;
import com.red.jejucalendar.common.Inputs;
import com.red.jejucalendar.dao.RecommendationCriteriaDao;
import com.red.jejucalendar.domain.RecommendationCriteria;

/**
 * UC09 추천 기준·근거 관리 (SCR-A-001 ⑫⑬, SCR-A-001-P02)
 * 적용 중인 기준은 CRITERIA_ID가 가장 큰 행 하나이고, 저장하면 그 행을 고침 (없으면 새로 만듦)
 * 기존 홍보 제안이 CRITERIA_ID로 기준을 가리키므로 행을 지우고 다시 만들지 않음
 */
@Service
public class CriteriaService {

    /** 저장 입력 (threshold는 직전 월 대비 방문 감소 기준 %) */
    public record CriteriaRequest(
            Integer declineRateThreshold,
            String sourceInfo,
            String basePeriod,
            String reasonTemplate,
            String requiredCondition) {
    }

    private final RecommendationCriteriaDao criteriaDao;

    public CriteriaService(RecommendationCriteriaDao criteriaDao) {
        this.criteriaDao = criteriaDao;
    }

    /** 화면 요약·홍보 제안 계산에 쓰는 현재 기준 (아직 없으면 비어 있음) */
    public Optional<RecommendationCriteria> current() {
        List<RecommendationCriteria> all = criteriaDao.findAll();
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(all.size() - 1));
    }

    public Optional<RecommendationCriteria> current(LoginUser user) {
        user.requireServiceOperator();
        return current();
    }

    /** SCR-A-001-P02 ⑧: 권한·입력 검사 후 저장 */
    public RecommendationCriteria save(LoginUser user, CriteriaRequest r) {
        user.requireServiceOperator();
        Integer threshold = r.declineRateThreshold();
        if (threshold == null || threshold < 0 || threshold > 100) {
            throw ApiException.badRequest("declineRateThreshold", "방문 감소 기준은 0부터 100까지의 정수로 입력해 주세요.");
        }
        String source = text("sourceInfo", "분석 결과·출처", r.sourceInfo(), 250);
        String period = text("basePeriod", "기준 기간", r.basePeriod(), 50);
        String reason = text("reasonTemplate", "추천 판단 이유", r.reasonTemplate(), 500);
        String condition = text("requiredCondition", "적용 조건", r.requiredCondition(), 500);
        LocalDate today = LocalDate.now();

        Optional<RecommendationCriteria> existing = current();
        if (existing.isPresent()) {
            long id = existing.get().criteriaId();
            criteriaDao.update(new RecommendationCriteria(id, threshold, source, period, reason, condition, today));
            return criteriaDao.findById(id).orElseThrow();
        }
        long id = criteriaDao.insert(new RecommendationCriteria(null, threshold, source, period, reason, condition, today));
        return criteriaDao.findById(id).orElseThrow();
    }

    private static String text(String field, String label, String value, int maxBytes) {
        String v = Inputs.required(field, value, label + "을(를) 입력해 주세요.");
        return Inputs.maxBytes(field, label, v, maxBytes);
    }
}
