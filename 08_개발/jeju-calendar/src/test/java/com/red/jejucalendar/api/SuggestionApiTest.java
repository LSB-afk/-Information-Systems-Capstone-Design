package com.red.jejucalendar.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.red.jejucalendar.dao.BusinessDao;
import com.red.jejucalendar.dao.DataSourceFileDao;
import com.red.jejucalendar.dao.PromoPlanDao;
import com.red.jejucalendar.dao.RegionConsumptionStatDao;
import com.red.jejucalendar.dao.RegionDao;
import com.red.jejucalendar.dao.UserDao;
import com.red.jejucalendar.domain.Business;
import com.red.jejucalendar.domain.DataSourceFile;
import com.red.jejucalendar.domain.Region;
import com.red.jejucalendar.domain.RegionConsumptionStat;
import com.red.jejucalendar.domain.User;

/**
 * 홍보 제안 API 확인 (SCR-C-004, SCR-C-004-P01) — 기준 저장·CSV 등록 → 제안 계산 → 조회·담기 흐름
 * 실제 팀 DB를 쓰지만 Transactional 이라 테스트마다 모두 되돌림
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIf("com.red.jejucalendar.support.LocalDb#configured")
class SuggestionApiTest {

    static final String PASSWORD = "Test-pass-26!";
    static final String A = "TEST0001";
    static final String B = "TEST0002";
    static final int[] A_VISITS = {35, 32, 44, 49, 55, 47, 65, 72, 57, 51, 37, 34};
    static final int[] B_VISITS = {27, 25, 33, 38, 42, 36, 51, 55, 43, 39, 29, 26};

    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @Autowired UserDao userDao;
    @Autowired RegionDao regionDao;
    @Autowired BusinessDao businessDao;
    @Autowired PromoPlanDao planDao;
    @Autowired DataSourceFileDao sourceDao;
    @Autowired RegionConsumptionStatDao consumptionDao;

    MockHttpSession operator;
    MockHttpSession owner;
    long businessId;
    long operatorId;

    @BeforeEach
    void fixtures() throws Exception {
        String hash = encoder.encode(PASSWORD);
        operatorId = userDao.insert(new User(null, "sugg-op@red.example", hash, "SERVICE", null));
        long ownerId = userDao.insert(new User(null, "sugg-owner@red.example", hash, "BUSINESS", null));
        regionDao.insert(new Region(A, "테스트읍"));
        regionDao.insert(new Region(B, "시험면"));
        businessId = businessDao.insert(new Business(null, ownerId, "테스트 카페", "CAFE", A));
        operator = login("SERVICE", "sugg-op@red.example");
        owner = login("BUSINESS", "sugg-owner@red.example");

        saveCriteria(20).andExpect(status().isOk());
        registerVisitCsv().andExpect(status().isCreated());
    }

    @Test
    void registeringDataCreatesSuggestionsGroupedByMonth() throws Exception {
        mvc.perform(get("/api/suggestions").session(owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regionCode").value(A))
                .andExpect(jsonPath("$.periodLabel").value("2026년 10월부터 12월까지"))
                .andExpect(jsonPath("$.criteria.declineRateThreshold").value(20))
                .andExpect(jsonPath("$.businessId").value(businessId))
                .andExpect(jsonPath("$.canAddToPlan").value(true))
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.message").value(nullValue()))
                .andExpect(jsonPath("$.months", hasSize(3)))
                .andExpect(jsonPath("$.months[0].suggestions", hasSize(0)))
                .andExpect(jsonPath("$.months[1].month").value("2026-11"))
                .andExpect(jsonPath("$.months[1].suggestions[0].title").value("따뜻한 음료로 채우는 평일 오후"))
                .andExpect(jsonPath("$.months[1].suggestions[0].reason")
                        .value("2025년 10월 대비 11월 방문 27.5% 감소 · 기준 20% 이상"))
                .andExpect(jsonPath("$.months[1].suggestions[0].recommendedDate").value("2026-11-16"))
                .andExpect(jsonPath("$.months[1].suggestions[0].items", hasSize(4)))
                .andExpect(jsonPath("$.months[1].suggestions[0].items[0].itemType").value("TIMING"))
                .andExpect(jsonPath("$.months[1].suggestions[0].items[0].reason").value("2025년 10월 51만 명 → 11월 37만 명"))
                .andExpect(jsonPath("$.months[1].suggestions[0].items[1].content").value("홍보 대상 자료 미확보"))
                .andExpect(jsonPath("$.months[1].suggestions[0].items[3].content").value("주변 관광지 자료 미확보"))
                .andExpect(jsonPath("$.months[1].suggestions[0].source.providerBasis").value("테스트 제공자"))
                .andExpect(jsonPath("$.months[1].suggestions[0].addedPlanId").value(nullValue()));

        // 다른 지역을 보면 같은 기준으로 계산한 그 지역 제안 (일정 담기는 사업체 지역에서만)
        mvc.perform(get("/api/suggestions").session(owner).param("regionCode", B))
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.months[1].suggestions[0].reason")
                        .value("2025년 10월 대비 11월 방문 25.6% 감소 · 기준 20% 이상"))
                .andExpect(jsonPath("$.canAddToPlan").value(false));
    }

    @Test
    void criteriaChangeRecalculatesAndKeepsPlanLinks() throws Exception {
        long novId = idOf(mvc.perform(get("/api/suggestions").session(owner)), "suggestionId");
        long planId = idOf(mvc.perform(post("/api/businesses/" + businessId + "/plans").session(owner)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"따뜻한 음료로 채우는 평일 오후\",\"scheduledDate\":\"2026-11-16\","
                        + "\"channel\":\"STORE\",\"preparationNote\":null,\"suggestionId\":" + novId + "}"))
                .andExpect(status().isCreated()), "planId");
        mvc.perform(get("/api/suggestions").session(owner))
                .andExpect(jsonPath("$.months[1].suggestions[0].addedPlanId").value(planId));

        // 기준 10%: 10월 제안이 새로 생기고 11월 제안은 같은 번호로 남아 계획 연결 유지
        saveCriteria(10).andExpect(status().isOk());
        mvc.perform(get("/api/suggestions").session(owner))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.months[0].suggestions[0].reason").value(containsString("10.5% 감소")))
                .andExpect(jsonPath("$.months[1].suggestions[0].suggestionId").value(novId))
                .andExpect(jsonPath("$.months[1].suggestions[0].addedPlanId").value(planId));

        // 남은 제안 한 번에 담기: 아직 담지 않은 10월 제안만 추가
        mvc.perform(post("/api/businesses/" + businessId + "/plans/from-suggestions").session(owner))
                .andExpect(jsonPath("$.added").value(1))
                .andExpect(jsonPath("$.plans[0].scheduledDate").value("2026-10-19"));

        // 기준 30%: 기준을 넘는 달이 없어 제안이 지워지고, 계획은 남되 제안 연결만 비워짐
        saveCriteria(30).andExpect(status().isOk());
        mvc.perform(get("/api/suggestions").session(owner))
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.message").value(containsString("조건에 맞는 제안이 없습니다")));
        mvc.perform(get("/api/plans/" + planId).session(owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestionId").value(nullValue()));
    }

    @Test
    void detailShowsObservedValuesConditionsAndNotice() throws Exception {
        long novId = idOf(mvc.perform(get("/api/suggestions").session(owner)), "suggestionId");
        mvc.perform(get("/api/suggestions/" + novId).session(owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card.title").value("따뜻한 음료로 채우는 평일 오후"))
                .andExpect(jsonPath("$.baseYear").value(2025))
                .andExpect(jsonPath("$.observed", hasSize(12)))
                .andExpect(jsonPath("$.observed[9].value").value(51))
                .andExpect(jsonPath("$.observed[10].value").value(37))
                .andExpect(jsonPath("$.criteria.reasonTemplate").value("직전 월 대비 방문 감소"))
                .andExpect(jsonPath("$.uncheckedConditions", hasItem("판매 가능 여부 확인")))
                .andExpect(jsonPath("$.canAddToPlan").value(true))
                .andExpect(jsonPath("$.notice").value(containsString("매출이나 방문 증가가 확인된 결과는 아닙니다")));
        mvc.perform(get("/api/suggestions/999999999").session(owner)).andExpect(status().isNotFound());
    }

    @Test
    void consumptionSharesFillTheAudienceItem() throws Exception {
        long source = sourceDao.insert(new DataSourceFile(null, operatorId, "share.csv", "v1", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 1), "KRW_100M", "PASS", "테스트 제공자", 2, LocalDate.now()));
        consumptionDao.insertAll(List.of(
                new RegionConsumptionStat(null, source, A, LocalDate.of(2025, 11, 1), "CAFE", "DOMESTIC", "ALL", new BigDecimal("70.00")),
                new RegionConsumptionStat(null, source, A, LocalDate.of(2025, 11, 1), "CAFE", "FOREIGN", "ALL", new BigDecimal("30.00"))));
        mvc.perform(post("/api/admin/suggestions/recalculate").session(operator))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated").value(2));
        mvc.perform(get("/api/suggestions").session(owner))
                .andExpect(jsonPath("$.months[1].suggestions[0].items[1].content").value("내국인 중심 안내 후보"))
                .andExpect(jsonPath("$.months[1].suggestions[0].items[1].reason")
                        .value("2025년 11월 카페 카드 소비 내국인 70% · 외국인 30%"));
    }

    @Test
    void permissions() throws Exception {
        mvc.perform(post("/api/admin/suggestions/recalculate").session(owner)).andExpect(status().isForbidden());
        mvc.perform(get("/api/suggestions")).andExpect(status().isUnauthorized());
        // 서비스 운영자는 권한을 받은 사업체가 없어 담기 기능이 없음
        mvc.perform(get("/api/suggestions").session(operator).param("regionCode", A))
                .andExpect(jsonPath("$.businessId").value(nullValue()))
                .andExpect(jsonPath("$.canAddToPlan").value(false));
    }

    // ---------- 도우미 ----------

    private ResultActions saveCriteria(int threshold) throws Exception {
        return mvc.perform(put("/api/admin/criteria").session(operator).contentType(MediaType.APPLICATION_JSON)
                .content(("{\"declineRateThreshold\":%d,\"sourceInfo\":\"지역별 방문객 2025\",\"basePeriod\":\"2025년\","
                        + "\"reasonTemplate\":\"직전 월 대비 방문 감소\",\"requiredCondition\":\"판매 가능 여부 확인\"}")
                        .formatted(threshold)));
    }

    private ResultActions registerVisitCsv() throws Exception {
        StringBuilder csv = new StringBuilder("region_code,month,value,unit,provider,aggregation\n");
        for (int m = 1; m <= 12; m++) {
            csv.append(A).append(",2025-%02d,".formatted(m)).append(A_VISITS[m - 1]).append(",만 명,테스트 제공자,월별 지역 집계\n");
            csv.append(B).append(",2025-%02d,".formatted(m)).append(B_VISITS[m - 1]).append(",만 명,테스트 제공자,월별 지역 집계\n");
        }
        return mvc.perform(multipart("/api/admin/data-sources").session(operator)
                .file(new MockMultipartFile("file", "visit_2025.csv", "text/csv",
                        csv.toString().getBytes(StandardCharsets.UTF_8)))
                .param("sourceVersion", "테스트 방문 자료 v1").param("startMonth", "2025-01").param("endMonth", "2025-12")
                .param("unit", "PERSON_10K").param("providerBasis", "테스트 제공자"));
    }

    private MockHttpSession login(String role, String email) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}".formatted(role, email, PASSWORD)))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }

    private static long idOf(ResultActions result, String field) throws Exception {
        String body = result.andReturn().getResponse().getContentAsString();
        Matcher m = Pattern.compile("\"" + field + "\":(\\d+)").matcher(body);
        if (!m.find()) {
            throw new AssertionError(field + " 없음: " + body);
        }
        return Long.parseLong(m.group(1));
    }
}
