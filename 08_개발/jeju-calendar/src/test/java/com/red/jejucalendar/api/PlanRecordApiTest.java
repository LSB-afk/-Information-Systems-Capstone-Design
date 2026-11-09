package com.red.jejucalendar.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.red.jejucalendar.dao.BusinessDao;
import com.red.jejucalendar.dao.BusinessPermissionDao;
import com.red.jejucalendar.dao.DataSourceFileDao;
import com.red.jejucalendar.dao.PromoSuggestionDao;
import com.red.jejucalendar.dao.RecommendationCriteriaDao;
import com.red.jejucalendar.dao.RegionDao;
import com.red.jejucalendar.dao.UserDao;
import com.red.jejucalendar.domain.Business;
import com.red.jejucalendar.domain.BusinessPermission;
import com.red.jejucalendar.domain.DataSourceFile;
import com.red.jejucalendar.domain.PromoSuggestion;
import com.red.jejucalendar.domain.RecommendationCriteria;
import com.red.jejucalendar.domain.Region;
import com.red.jejucalendar.domain.User;

/**
 * 로그인, 3개월 홍보 일정표, 계획 작성·수정, 실행 기록, 사용 결과 요약 API 확인
 * 실제 팀 DB를 쓰지만 Transactional 이라 테스트마다 모두 되돌림
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIf("com.red.jejucalendar.support.LocalDb#configured")
class PlanRecordApiTest {

    static final String PASSWORD = "Test-pass-26!";
    static final String REGION = "TEST0001";

    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @Autowired UserDao userDao;
    @Autowired RegionDao regionDao;
    @Autowired BusinessDao businessDao;
    @Autowired BusinessPermissionDao permissionDao;
    @Autowired DataSourceFileDao sourceDao;
    @Autowired RecommendationCriteriaDao criteriaDao;
    @Autowired PromoSuggestionDao suggestionDao;

    long myBusiness;
    long otherBusiness;
    long octSuggestion;
    long novSuggestion;

    @BeforeEach
    void fixtures() {
        String hash = encoder.encode(PASSWORD);
        long owner = userDao.insert(new User(null, "owner@red.example", hash, "BUSINESS", null));
        long otherOwner = userDao.insert(new User(null, "other@red.example", hash, "BUSINESS", null));
        long operator = userDao.insert(new User(null, "operator@red.example", hash, "SERVICE", null));
        regionDao.insert(new Region(REGION, "테스트읍"));
        myBusiness = businessDao.insert(new Business(null, owner, "테스트 카페", "CAFE", REGION));
        otherBusiness = businessDao.insert(new Business(null, otherOwner, "다른 카페", "CAFE", REGION));
        permissionDao.insert(new BusinessPermission(operator, myBusiness, "MANAGER", LocalDate.of(2026, 9, 1)));

        long source = sourceDao.insert(new DataSourceFile(null, operator, "visit.csv", "v1", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 1), "PERSON_10K", "PASS", "제주관광공사", 12, LocalDate.of(2026, 9, 1)));
        long criteria = criteriaDao.insert(new RecommendationCriteria(null, 20, "지역별 방문객", "2025년",
                "방문 감소", "판매 가능 여부 확인", null));
        octSuggestion = suggestionDao.insert(new PromoSuggestion(null, criteria, LocalDate.of(2026, 10, 1),
                "가을 메뉴 소개", "방문 감소", REGION, null, LocalDate.of(2026, 10, 20), "BLOG", "사진 준비", null, source));
        novSuggestion = suggestionDao.insert(new PromoSuggestion(null, criteria, LocalDate.of(2026, 11, 1),
                "평일 세트 안내", "방문 감소", REGION, null, null, null, null, null, source));
    }

    // ---------- 로그인 (SCR-C-001) ----------

    @Test
    void loginChecksInputAccountAndRole() throws Exception {
        login("BUSINESS", "", PASSWORD).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("email"))
                .andExpect(jsonPath("$.message").value("올바른 이메일 주소를 입력해 주세요."));
        login("BUSINESS", "owner@red.example", "").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("password"));
        login("BUSINESS", "owner@red.example", "wrong").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호를 확인해 주세요."));
        // 비밀번호가 맞아도 선택한 역할이 계정 역할과 다르면 실패
        login("SERVICE", "owner@red.example", PASSWORD).andExpect(status().isUnauthorized());
        login("BUSINESS", "nobody@red.example", PASSWORD).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        MockHttpSession session = session("BUSINESS", " Owner@Red.example ");
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("BUSINESS"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
    }

    // ---------- 권한 ----------

    @Test
    void ownerAndOperatorSeeOnlyPermittedBusinesses() throws Exception {
        MockHttpSession owner = session("BUSINESS", "owner@red.example");
        MockHttpSession operator = session("SERVICE", "operator@red.example");

        mvc.perform(get("/api/businesses").session(owner)).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].businessId").value(myBusiness));
        mvc.perform(get("/api/businesses").session(operator)).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].businessId").value(myBusiness));

        mvc.perform(get("/api/businesses/" + otherBusiness + "/calendar").session(owner))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/businesses/" + otherBusiness + "/records").session(operator))
                .andExpect(status().isForbidden());
        // 서비스 운영자는 권한을 받은 사업체의 계획을 처리할 수 있음
        createPlan(operator, myBusiness, "운영자 작성 계획", "2026-12-01", "ETC").andExpect(status().isCreated());
        mvc.perform(get("/api/businesses/" + myBusiness + "/calendar")).andExpect(status().isUnauthorized());
    }

    // ---------- 일정표·계획 (SCR-B-001, SCR-B-001-P01) ----------

    @Test
    void calendarAndPlanValidation() throws Exception {
        MockHttpSession owner = session("BUSINESS", "owner@red.example");

        mvc.perform(get("/api/businesses/" + myBusiness + "/calendar").session(owner)).andExpect(status().isOk())
                .andExpect(jsonPath("$.periodLabel").value("2026년 10월부터 12월까지"))
                .andExpect(jsonPath("$.months", hasSize(3)))
                .andExpect(jsonPath("$.months[0].month").value("2026-10"))
                .andExpect(jsonPath("$.months[0].plans", hasSize(0)));

        createPlan(owner, myBusiness, "   ", "2026-10-05", "INSTAGRAM").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("title"))
                .andExpect(jsonPath("$.message").value("계획 이름을 입력해 주세요."));
        createPlan(owner, myBusiness, "범위 밖", "2027-01-05", "INSTAGRAM").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("scheduledDate"));
        createPlan(owner, myBusiness, "채널 오류", "2026-10-05", "TIKTOK").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("channel"));
        // TITLE VARCHAR(100)은 100바이트라 한글 34자부터 들어가지 않음
        createPlan(owner, myBusiness, "가".repeat(34), "2026-10-05", "INSTAGRAM").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("한글 기준 33자")));
        createPlan(owner, myBusiness, "가".repeat(33), "2026-10-06", "INSTAGRAM").andExpect(status().isCreated());

        long planId = idOf(createPlan(owner, myBusiness, " 가을 메뉴 콘텐츠 ", "2026-10-05", "INSTAGRAM")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("가을 메뉴 콘텐츠"))
                .andExpect(jsonPath("$.suggestionId").value(nullValue())), "planId");

        mvc.perform(put("/api/plans/" + planId).session(owner).contentType(MediaType.APPLICATION_JSON)
                        .content(planJson("가을 메뉴 콘텐츠 수정", "2026-11-09", "STORE", "메뉴 구성 확인", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduledDate").value("2026-11-09"))
                .andExpect(jsonPath("$.preparationNote").value("메뉴 구성 확인"));

        mvc.perform(get("/api/businesses/" + myBusiness + "/calendar").session(owner))
                .andExpect(jsonPath("$.planCount").value(2))
                .andExpect(jsonPath("$.months[1].plans", hasSize(1)))
                .andExpect(jsonPath("$.months[1].plans[0].title").value("가을 메뉴 콘텐츠 수정"));

        MockHttpSession other = session("BUSINESS", "other@red.example");
        mvc.perform(put("/api/plans/" + planId).session(other).contentType(MediaType.APPLICATION_JSON)
                .content(planJson("남의 계획", "2026-11-09", "STORE", null, null))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/plans/" + planId).session(owner)).andExpect(status().isNoContent());
        mvc.perform(get("/api/plans/" + planId).session(owner)).andExpect(status().isNotFound());
    }

    @Test
    void suggestionsAreAddedOnlyOnce() throws Exception {
        MockHttpSession owner = session("BUSINESS", "owner@red.example");

        mvc.perform(post("/api/businesses/" + myBusiness + "/plans").session(owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planJson("가을 메뉴 소개", "2026-10-20", "BLOG", "사진 준비", octSuggestion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.suggestionId").value(octSuggestion));
        mvc.perform(post("/api/businesses/" + myBusiness + "/plans").session(owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planJson("가을 메뉴 소개", "2026-10-21", "BLOG", null, octSuggestion)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 담은 제안입니다. 저장된 계획을 수정할 수 있어요."));

        // 자동 채우기: 아직 담지 않은 11월 제안만 추가 (추천 날짜가 없으면 15일, 채널이 없으면 인스타그램)
        mvc.perform(post("/api/businesses/" + myBusiness + "/plans/from-suggestions").session(owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.added").value(1))
                .andExpect(jsonPath("$.plans[0].suggestionId").value(novSuggestion))
                .andExpect(jsonPath("$.plans[0].scheduledDate").value("2026-11-15"))
                .andExpect(jsonPath("$.plans[0].channel").value("INSTAGRAM"));
        mvc.perform(post("/api/businesses/" + myBusiness + "/plans/from-suggestions").session(owner))
                .andExpect(jsonPath("$.added").value(0))
                .andExpect(jsonPath("$.message").value("이미 모든 제안을 담았거나 담을 제안이 없습니다."));
    }

    // ---------- 실행 기록·요약 (SCR-B-002, P01, P02) ----------

    @Test
    void recordFormSaveListAndSummary() throws Exception {
        MockHttpSession owner = session("BUSINESS", "owner@red.example");
        long fromSuggestion = idOf(mvc.perform(post("/api/businesses/" + myBusiness + "/plans").session(owner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(planJson("가을 메뉴 소개", "2026-10-20", "BLOG", "사진 준비", octSuggestion))), "planId");
        long manual = idOf(createPlan(owner, myBusiness, "매장 안내문", "2026-11-03", "STORE"), "planId");

        mvc.perform(get("/api/businesses/" + myBusiness + "/records/summary").session(owner))
                .andExpect(jsonPath("$.planCount").value(2))
                .andExpect(jsonPath("$.recordCount").value(0))
                .andExpect(jsonPath("$.spentTimeMin.total").value(nullValue()));

        // 최초 작성: 계획 값으로 자동 채움, 제안에서 담은 계획은 채택이 기본값
        mvc.perform(get("/api/plans/" + fromSuggestion + "/record").session(owner))
                .andExpect(jsonPath("$.prefilled").value(true))
                .andExpect(jsonPath("$.recordId").value(nullValue()))
                .andExpect(jsonPath("$.values.execStatus").value("DONE"))
                .andExpect(jsonPath("$.values.executedDate").value("2026-10-20"))
                .andExpect(jsonPath("$.values.adoptionStatus").value("YES"))
                .andExpect(jsonPath("$.values.actualActivity").value("블로그 · 사진 준비"));
        mvc.perform(get("/api/plans/" + manual + "/record").session(owner))
                .andExpect(jsonPath("$.values.adoptionStatus").value("UNKNOWN"));

        saveRecord(owner, fromSuggestion, recordJson("DONE", null, "-1", "YES"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field").value("executedDate"));
        saveRecord(owner, fromSuggestion, recordJson("DONE", "\"2026-10-20\"", "-1", "YES"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field").value("checkedClickCount"));
        saveRecord(owner, fromSuggestion, recordJson("DONE", "\"2026-10-20\"", "0", "MAYBE"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field").value("adoptionStatus"));

        // 클릭 0은 실제 0건, 예약·쿠폰 빈칸은 미확보
        long recordId = idOf(saveRecord(owner, fromSuggestion, recordJson("DONE", "\"2026-10-20\"", "0", "YES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.values.checkedClickCount").value(0))
                .andExpect(jsonPath("$.values.checkedBookingCount").value(nullValue())), "recordId");
        // 미실행은 날짜 없이 저장할 수 있음
        saveRecord(owner, manual, recordJson("NOT_DONE", null, "null", "UNKNOWN")).andExpect(status().isOk());

        // 다시 열면 저장된 기록을 불러오고, 다시 저장하면 같은 기록을 수정함
        mvc.perform(get("/api/plans/" + fromSuggestion + "/record").session(owner))
                .andExpect(jsonPath("$.prefilled").value(false))
                .andExpect(jsonPath("$.recordId").value(recordId));
        saveRecord(owner, fromSuggestion, recordJson("DONE", "\"2026-10-21\"", "12", "YES"))
                .andExpect(jsonPath("$.recordId").value(recordId))
                .andExpect(jsonPath("$.values.checkedClickCount").value(12));

        mvc.perform(get("/api/businesses/" + myBusiness + "/records").session(owner))
                .andExpect(jsonPath("$.planCount").value(2))
                .andExpect(jsonPath("$.recordedCount").value(2))
                .andExpect(jsonPath("$.unrecordedCount").value(0));
        mvc.perform(get("/api/businesses/" + myBusiness + "/records/summary").session(owner))
                .andExpect(jsonPath("$.recordCount").value(2))
                .andExpect(jsonPath("$.doneCount").value(1))
                .andExpect(jsonPath("$.adoptedYes").value(1))
                .andExpect(jsonPath("$.adoptedUnknown").value(1))
                .andExpect(jsonPath("$.clicks.total").value(12))
                .andExpect(jsonPath("$.clicks.inputCount").value(1))
                .andExpect(jsonPath("$.spentTimeMin.total").value(30))
                .andExpect(jsonPath("$.bookings.total").value(nullValue()));

        // 계획을 지우면 연결된 기록도 지워짐
        mvc.perform(delete("/api/plans/" + fromSuggestion).session(owner)).andExpect(status().isNoContent());
        mvc.perform(get("/api/businesses/" + myBusiness + "/records").session(owner))
                .andExpect(jsonPath("$.planCount").value(1))
                .andExpect(jsonPath("$.recordedCount").value(1));
    }

    // ---------- 도우미 ----------

    private ResultActions login(String role, String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}".formatted(role, email, password)));
    }

    private MockHttpSession session(String role, String email) throws Exception {
        return (MockHttpSession) login(role, email, PASSWORD).andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }

    private ResultActions createPlan(MockHttpSession s, long businessId, String title, String date, String channel)
            throws Exception {
        return mvc.perform(post("/api/businesses/" + businessId + "/plans").session(s)
                .contentType(MediaType.APPLICATION_JSON).content(planJson(title, date, channel, null, null)));
    }

    private ResultActions saveRecord(MockHttpSession s, long planId, String json) throws Exception {
        return mvc.perform(put("/api/plans/" + planId + "/record").session(s)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String planJson(String title, String date, String channel, String note, Long suggestionId) {
        return "{\"title\":\"%s\",\"scheduledDate\":\"%s\",\"channel\":\"%s\",\"preparationNote\":%s,\"suggestionId\":%s}"
                .formatted(title, date, channel, note == null ? "null" : "\"" + note + "\"", suggestionId);
    }

    private static String recordJson(String status, String date, String clicks, String adoption) {
        return ("{\"execStatus\":\"%s\",\"executedDate\":%s,\"actualActivity\":\"블로그 게시\",\"adoptionStatus\":\"%s\","
                + "\"spentTimeMin\":%s,\"checkedClickCount\":%s,\"checkedBookingCount\":null,\"couponUsageCount\":null,"
                + "\"resultMemo\":\"조회수 확인\",\"collectionMethod\":\"블로그 통계\"}")
                .formatted(status, date == null ? "null" : date, adoption,
                        "DONE".equals(status) ? "30" : "null", clicks);
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
