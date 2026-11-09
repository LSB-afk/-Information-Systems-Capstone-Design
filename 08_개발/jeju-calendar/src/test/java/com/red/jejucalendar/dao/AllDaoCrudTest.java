package com.red.jejucalendar.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.red.jejucalendar.domain.Business;
import com.red.jejucalendar.domain.BusinessPermission;
import com.red.jejucalendar.domain.DataSourceFile;
import com.red.jejucalendar.domain.DataValidationResult;
import com.red.jejucalendar.domain.ExecutionRecord;
import com.red.jejucalendar.domain.PromoPlan;
import com.red.jejucalendar.domain.PromoSuggestion;
import com.red.jejucalendar.domain.PromoSuggestionItem;
import com.red.jejucalendar.domain.RecommendationCriteria;
import com.red.jejucalendar.domain.Region;
import com.red.jejucalendar.domain.RegionConsumptionStat;
import com.red.jejucalendar.domain.RegionMonthlyStat;
import com.red.jejucalendar.domain.User;

/**
 * 나머지 12개 테이블 DAO의 저장·조회·수정·삭제와 FK 규칙 확인
 * Transactional 이라 테스트가 끝나면 모두 되돌려 DB에 데이터가 남지 않음 (시퀀스 번호만 증가)
 */
@SpringBootTest
@Transactional
@EnabledIf("com.red.jejucalendar.support.LocalDb#configured")
class AllDaoCrudTest {

    static final String REGION = "TEST0001";
    static final LocalDate MONTH = LocalDate.of(2025, 1, 1);

    @Autowired UserDao userDao;
    @Autowired RegionDao regionDao;
    @Autowired BusinessDao businessDao;
    @Autowired BusinessPermissionDao permissionDao;
    @Autowired DataSourceFileDao sourceDao;
    @Autowired DataValidationResultDao validationDao;
    @Autowired RegionMonthlyStatDao monthlyDao;
    @Autowired RegionConsumptionStatDao consumptionDao;
    @Autowired RecommendationCriteriaDao criteriaDao;
    @Autowired PromoSuggestionDao suggestionDao;
    @Autowired PromoSuggestionItemDao itemDao;
    @Autowired PromoPlanDao planDao;
    @Autowired ExecutionRecordDao recordDao;

    long userId;
    long businessId;
    long sourceId;
    long criteriaId;

    @BeforeEach
    void baseRows() {
        userId = userDao.insert(new User(null, "dao-test@red.example", "hash", "BUSINESS", MONTH));
        regionDao.insert(new Region(REGION, "테스트읍"));
        businessId = businessDao.insert(new Business(null, userId, "테스트 카페", "CAFE", REGION));
        sourceId = sourceDao.insert(new DataSourceFile(null, userId, "visit_2025.csv", "v1",
                MONTH, LocalDate.of(2025, 12, 1), "PERSON_10K", "PENDING", "제주관광공사", 24, MONTH));
        criteriaId = criteriaDao.insert(new RecommendationCriteria(null, 10, "지역별 방문객", "2023년부터 2025년까지",
                "방문이 {rate}% 줄어든 달", "검증 통과 자료", null));
    }

    @Test
    void regionAndBusiness() {
        assertThat(regionDao.findByCode(REGION)).contains(new Region(REGION, "테스트읍"));
        assertThat(regionDao.update(new Region(REGION, "테스트면"))).isEqualTo(1);
        assertThat(regionDao.findByCode(REGION).orElseThrow().regionName()).isEqualTo("테스트면");

        Business b = businessDao.findById(businessId).orElseThrow();
        assertThat(b.businessName()).isEqualTo("테스트 카페");
        assertThat(businessDao.findByUserId(userId)).hasSize(1);
        assertThat(businessDao.update(new Business(businessId, userId, "새 이름", "EXPERIENCE", REGION))).isEqualTo(1);
        assertThat(businessDao.findById(businessId).orElseThrow().category()).isEqualTo("EXPERIENCE");

        // 사업체가 있으면 지역을 지울 수 없음 (BusinessRegion FK)
        assertThatThrownBy(() -> regionDao.deleteByCode(REGION)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void businessPermission() {
        permissionDao.insert(new BusinessPermission(userId, businessId, "OWNER", MONTH));
        assertThat(permissionDao.find(userId, businessId).orElseThrow().permissionType()).isEqualTo("OWNER");
        assertThat(businessDao.findAccessibleByUserId(userId)).extracting(Business::businessId).containsExactly(businessId);
        assertThat(permissionDao.findByBusinessId(businessId)).hasSize(1);

        assertThat(permissionDao.update(new BusinessPermission(userId, businessId, "MANAGER", MONTH.plusDays(1)))).isEqualTo(1);
        assertThat(permissionDao.find(userId, businessId).orElseThrow().grantedAt()).isEqualTo(MONTH.plusDays(1));

        assertThat(permissionDao.delete(userId, businessId)).isEqualTo(1);
        assertThat(permissionDao.findByUserId(userId)).isEmpty();
    }

    @Test
    void dataSourceAndValidation() {
        DataSourceFile s = sourceDao.findById(sourceId).orElseThrow();
        assertThat(s.startMonth()).isEqualTo(MONTH);
        assertThat(s.rowCount()).isEqualTo(24);
        assertThat(sourceDao.findByValidationStatus("PENDING")).extracting(DataSourceFile::sourceId).contains(sourceId);

        validationDao.insert(new DataValidationResult(sourceId, "PERIOD", "PASS", null, MONTH));
        validationDao.insert(new DataValidationResult(sourceId, "MISSING_VALUE", "FAIL", "3행 누락", MONTH));
        assertThat(validationDao.findBySourceId(sourceId)).hasSize(2);
        assertThat(validationDao.find(sourceId, "PERIOD").orElseThrow().reason()).isNull();

        assertThat(validationDao.update(new DataValidationResult(sourceId, "MISSING_VALUE", "PASS", null, MONTH))).isEqualTo(1);
        assertThat(validationDao.find(sourceId, "MISSING_VALUE").orElseThrow().result()).isEqualTo("PASS");
        assertThat(sourceDao.updateValidationStatus(sourceId, "PASS")).isEqualTo(1);
        assertThat(sourceDao.update(new DataSourceFile(sourceId, userId, "visit_2025.csv", "v2", MONTH,
                LocalDate.of(2025, 12, 1), "PERSON_10K", "PASS", "제주관광공사", 25, MONTH))).isEqualTo(1);
        assertThat(sourceDao.findById(sourceId).orElseThrow().sourceVersion()).isEqualTo("v2");

        assertThat(validationDao.delete(sourceId, "PERIOD")).isEqualTo(1);
        assertThat(validationDao.deleteBySourceId(sourceId)).isEqualTo(1);
    }

    @Test
    void regionStats() {
        long statId = monthlyDao.insert(new RegionMonthlyStat(null, sourceId, REGION, MONTH, "VISIT", 120));
        int saved = monthlyDao.insertAll(List.of(
                new RegionMonthlyStat(null, sourceId, REGION, MONTH.plusMonths(1), "VISIT", 95),
                new RegionMonthlyStat(null, sourceId, REGION, MONTH.plusMonths(2), "VISIT", null)));
        assertThat(saved).isEqualTo(2);
        List<RegionMonthlyStat> visits = monthlyDao.findByRegionAndPeriod(sourceId, REGION, "VISIT",
                MONTH, MONTH.plusMonths(2));
        assertThat(visits).extracting(RegionMonthlyStat::statValue).containsExactly(120, 95, null);
        assertThat(monthlyDao.update(new RegionMonthlyStat(statId, null, null, null, null, 130))).isEqualTo(1);
        assertThat(monthlyDao.findById(statId).orElseThrow().statValue()).isEqualTo(130);

        // 같은 자료·지역·월·지표는 두 번 넣을 수 없음 (AK1)
        assertThatThrownBy(() -> monthlyDao.insert(new RegionMonthlyStat(null, sourceId, REGION, MONTH, "VISIT", 1)))
                .isInstanceOf(DataIntegrityViolationException.class);

        long cid = consumptionDao.insert(new RegionConsumptionStat(null, sourceId, REGION, MONTH, "CAFE", "DOMESTIC",
                "ALL", new BigDecimal("62.50")));
        consumptionDao.insertAll(List.of(
                new RegionConsumptionStat(null, sourceId, REGION, MONTH, "CAFE", "FOREIGN", "ALL", new BigDecimal("37.50")),
                new RegionConsumptionStat(null, sourceId, REGION, MONTH, "CAFE", "ALL", "T1114", null)));
        assertThat(consumptionDao.findByRegionAndMonth(sourceId, REGION, MONTH)).hasSize(3);
        assertThat(consumptionDao.update(new RegionConsumptionStat(cid, null, null, null, null, null, null,
                new BigDecimal("60.25")))).isEqualTo(1);
        assertThat(consumptionDao.findById(cid).orElseThrow().shareRate()).isEqualByComparingTo("60.25");

        assertThat(monthlyDao.deleteBySourceId(sourceId)).isEqualTo(3);
        assertThat(consumptionDao.deleteById(cid)).isEqualTo(1);
        assertThat(consumptionDao.deleteBySourceId(sourceId)).isEqualTo(2);
    }

    @Test
    void suggestionPlanAndRecord() {
        assertThat(criteriaDao.findById(criteriaId).orElseThrow().updatedAt()).isNull();
        assertThat(criteriaDao.update(new RecommendationCriteria(criteriaId, 15, "지역별 방문객", "2023년부터 2025년까지",
                "방문이 {rate}% 줄어든 달", "검증 통과 자료", MONTH))).isEqualTo(1);
        assertThat(criteriaDao.findById(criteriaId).orElseThrow().declineRateThreshold()).isEqualTo(15);

        long suggestionId = suggestionDao.insert(new PromoSuggestion(null, criteriaId, MONTH, "1월 비수기 할인",
                "방문 감소", REGION, null, null, "INSTAGRAM", null, null, sourceId));
        assertThat(suggestionDao.findByRegionAndMonth(REGION, MONTH)).hasSize(1);
        assertThat(suggestionDao.findBySourceId(sourceId)).hasSize(1);
        itemDao.insert(new PromoSuggestionItem(suggestionId, "TIMING", "1월 둘째 주", null, null));
        itemDao.insert(new PromoSuggestionItem(suggestionId, "AUDIENCE", "도민", "주말 방문 비중", null));
        assertThat(itemDao.findBySuggestionId(suggestionId)).hasSize(2);
        assertThat(itemDao.update(new PromoSuggestionItem(suggestionId, "TIMING", "1월 셋째 주", null, "확인함"))).isEqualTo(1);
        assertThat(itemDao.find(suggestionId, "TIMING").orElseThrow().checkNote()).isEqualTo("확인함");

        long planFromSuggestion = planDao.insert(new PromoPlan(null, businessId, suggestionId, "인스타 쿠폰",
                LocalDate.of(2025, 1, 10), "INSTAGRAM", null));
        long manualPlan = planDao.insert(new PromoPlan(null, businessId, null, "매장 안내문",
                LocalDate.of(2025, 2, 3), "STORE", "A4 인쇄"));
        assertThat(planDao.findById(manualPlan).orElseThrow().suggestionId()).isNull();
        assertThat(planDao.findByBusinessIdAndPeriod(businessId, MONTH, LocalDate.of(2025, 1, 31)))
                .extracting(PromoPlan::planId).containsExactly(planFromSuggestion);
        assertThat(planDao.update(new PromoPlan(manualPlan, businessId, null, "매장 안내문 수정",
                LocalDate.of(2025, 2, 4), "STORE", null))).isEqualTo(1);

        long recordId = recordDao.insert(new ExecutionRecord(null, planFromSuggestion, "DONE", LocalDate.of(2025, 1, 10),
                30, null, 4, "반응 좋음", "인스타 통계", "YES", null, 45));
        ExecutionRecord r = recordDao.findByPlanId(planFromSuggestion).orElseThrow();
        assertThat(r.checkedBookingCount()).isNull();
        assertThat(r.spentTimeMin()).isEqualTo(45);
        assertThat(recordDao.findByBusinessId(businessId)).hasSize(1);
        assertThat(recordDao.update(new ExecutionRecord(recordId, planFromSuggestion, "DONE", LocalDate.of(2025, 1, 10),
                30, 2, 4, "예약 2건", "인스타 통계", "YES", "쿠폰 발송", 50))).isEqualTo(1);
        assertThat(recordDao.findById(recordId).orElseThrow().checkedBookingCount()).isEqualTo(2);

        // 계획 하나에 기록 하나 (AK1: PLAN_ID)
        assertThatThrownBy(() -> recordDao.insert(new ExecutionRecord(null, planFromSuggestion, "NOT_DONE", null,
                null, null, null, null, null, "UNKNOWN", null, null)))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 항목이 남아 있으면 제안을 지울 수 없음 (ItemSuggestion FK)
        assertThatThrownBy(() -> suggestionDao.deleteById(suggestionId))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(itemDao.delete(suggestionId, "AUDIENCE")).isEqualTo(1);
        assertThat(itemDao.deleteBySuggestionId(suggestionId)).isEqualTo(1);

        // 제안을 지우면 그 제안으로 만든 계획의 SUGGESTION_ID는 NULL (ON DELETE SET NULL)
        assertThat(suggestionDao.deleteById(suggestionId)).isEqualTo(1);
        assertThat(planDao.findById(planFromSuggestion).orElseThrow().suggestionId()).isNull();

        // 계획을 지우면 실행 결과 기록도 함께 지워짐 (ON DELETE CASCADE)
        assertThat(planDao.deleteById(planFromSuggestion)).isEqualTo(1);
        assertThat(recordDao.findById(recordId)).isEmpty();
        assertThat(recordDao.deleteById(recordId)).isZero();
        assertThat(planDao.deleteById(manualPlan)).isEqualTo(1);
        assertThat(criteriaDao.deleteById(criteriaId)).isEqualTo(1);
    }

    @Test
    void deleteBaseRowsInChildFirstOrder() {
        assertThat(sourceDao.deleteById(sourceId)).isEqualTo(1);
        assertThat(businessDao.deleteById(businessId)).isEqualTo(1);
        assertThat(regionDao.deleteByCode(REGION)).isEqualTo(1);
        assertThat(criteriaDao.findAll()).extracting(RecommendationCriteria::criteriaId).contains(criteriaId);
        assertThat(userDao.findAll()).extracting(User::userId).contains(userId);
    }
}
