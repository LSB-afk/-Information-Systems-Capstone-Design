package com.red.jejucalendar.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.transaction.annotation.Transactional;

import com.red.jejucalendar.dao.DataSourceFileDao;
import com.red.jejucalendar.dao.RegionConsumptionStatDao;
import com.red.jejucalendar.dao.RegionDao;
import com.red.jejucalendar.dao.RegionMonthlyStatDao;
import com.red.jejucalendar.dao.UserDao;
import com.red.jejucalendar.domain.DataSourceFile;
import com.red.jejucalendar.domain.Region;
import com.red.jejucalendar.domain.RegionConsumptionStat;
import com.red.jejucalendar.domain.RegionMonthlyStat;
import com.red.jejucalendar.domain.User;

/**
 * 지역 분석 API 확인 (SCR-C-003 방문·계절, 소비 비교, 자료 없음)
 * 실제 팀 DB를 쓰지만 Transactional 이라 테스트마다 모두 되돌림
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIf("com.red.jejucalendar.support.LocalDb#configured")
class AnalysisApiTest {

    static final String PASSWORD = "Test-pass-26!";
    static final String A = "TEST0001";
    static final String B = "TEST0002";
    static final int[] A_VISITS = {35, 32, 44, 49, 55, 47, 65, 72, 57, 51, 37, 34};

    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @Autowired UserDao userDao;
    @Autowired RegionDao regionDao;
    @Autowired DataSourceFileDao sourceDao;
    @Autowired RegionMonthlyStatDao monthlyDao;
    @Autowired RegionConsumptionStatDao consumptionDao;

    MockHttpSession session;

    @BeforeEach
    void fixtures() throws Exception {
        long user = userDao.insert(new User(null, "analysis@red.example", encoder.encode(PASSWORD), "SERVICE", null));
        regionDao.insert(new Region(A, "테스트읍"));
        regionDao.insert(new Region(B, "시험면"));

        long visitSource = source(user, "visit_2025.csv", "PERSON_10K");
        List<RegionMonthlyStat> visits = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            visits.add(new RegionMonthlyStat(null, visitSource, A, LocalDate.of(2025, m, 1), "VISIT", A_VISITS[m - 1]));
        }
        visits.add(new RegionMonthlyStat(null, visitSource, B, LocalDate.of(2025, 10, 1), "VISIT", 40));
        visits.add(new RegionMonthlyStat(null, visitSource, B, LocalDate.of(2025, 11, 1), "VISIT", 30));
        monthlyDao.insertAll(visits);

        // 소비 자료는 11월이 없음 (미수집)
        long spendSource = source(user, "spend_2025.csv", "KRW_100M");
        monthlyDao.insertAll(List.of(
                new RegionMonthlyStat(null, spendSource, A, LocalDate.of(2025, 10, 1), "SPEND", 29),
                new RegionMonthlyStat(null, spendSource, A, LocalDate.of(2025, 12, 1), "SPEND", 21)));

        long shareSource = source(user, "share_2025.csv", "KRW_100M");
        LocalDate oct = LocalDate.of(2025, 10, 1);
        consumptionDao.insertAll(List.of(
                share(shareSource, oct, "CAFE", "DOMESTIC", "ALL", "75.00"),
                share(shareSource, oct, "CAFE", "FOREIGN", "ALL", "25.00"),
                share(shareSource, oct, "RESTAURANT", "DOMESTIC", "ALL", "70.00"),
                share(shareSource, oct, "CAFE", "ALL", "T0611", "15.00"),
                share(shareSource, oct, "CAFE", "ALL", "T1114", "25.00"),
                share(shareSource, oct, "CAFE", "ALL", "T1418", "40.00"),
                share(shareSource, oct, "CAFE", "ALL", "T1806", "20.00")));

        session = (MockHttpSession) mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SERVICE\",\"email\":\"analysis@red.example\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }

    @Test
    void visitTrendShowsTwelveMonthsPeakLowAndSource() throws Exception {
        mvc.perform(get("/api/analysis/trend").session(session).param("regionCode", A).param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasData").value(true))
                .andExpect(jsonPath("$.metric").value("VISIT"))
                .andExpect(jsonPath("$.unitLabel").value("만 명"))
                .andExpect(jsonPath("$.availableYears", hasItem(2025)))
                .andExpect(jsonPath("$.months", hasSize(12)))
                .andExpect(jsonPath("$.months[7].value").value(72))
                .andExpect(jsonPath("$.peak.month").value(8))
                .andExpect(jsonPath("$.low.month").value(2))
                .andExpect(jsonPath("$.low.value").value(32))
                .andExpect(jsonPath("$.source.fileName").value("visit_2025.csv"))
                .andExpect(jsonPath("$.source.providerBasis").value("테스트 제공자"))
                .andExpect(jsonPath("$.notice").value("지역 방문 집계는 개별 가게의 실제 손님 수가 아닙니다."));
    }

    @Test
    void seasonTableComparesRegionsOnTheSameSource() throws Exception {
        mvc.perform(get("/api/analysis/trend").session(session).param("regionCode", A).param("year", "2025"))
                .andExpect(jsonPath("$.changeLabel").value("10월 → 11월"))
                .andExpect(jsonPath("$.seasonTable", hasSize(2)))
                .andExpect(jsonPath("$.seasonTable[0].regionCode").value(A))
                // (37 − 51) ÷ 51 × 100 = −27.45… → −27.5
                .andExpect(jsonPath("$.seasonTable[0].changeRate").value(-27.5))
                .andExpect(jsonPath("$.seasonTable[1].regionCode").value(B))
                .andExpect(jsonPath("$.seasonTable[1].changeRate").value(-25.0))
                .andExpect(jsonPath("$.regions[1].value").value(40));
        // 시험면은 10월·11월 자료만 있어 나머지 달은 미확보로 남음
        mvc.perform(get("/api/analysis/trend").session(session).param("regionCode", B).param("year", "2025"))
                .andExpect(jsonPath("$.months[0].value").value(nullValue()))
                .andExpect(jsonPath("$.months[9].value").value(40))
                .andExpect(jsonPath("$.low.month").value(11));
    }

    @Test
    void missingDataIsNotTurnedIntoZero() throws Exception {
        mvc.perform(get("/api/analysis/trend").session(session).param("regionCode", A).param("year", "2019"))
                .andExpect(jsonPath("$.hasData").value(false))
                .andExpect(jsonPath("$.months", hasSize(0)))
                .andExpect(jsonPath("$.message").value(containsString("2019년 자료가 없습니다")))
                .andExpect(jsonPath("$.message").value(containsString("2025년")));
        mvc.perform(get("/api/analysis/trend").session(session).param("regionCode", A).param("year", "2025")
                        .param("metric", "SPEND"))
                .andExpect(jsonPath("$.unitLabel").value("억 원"))
                .andExpect(jsonPath("$.months[9].value").value(29))
                .andExpect(jsonPath("$.months[10].value").value(nullValue()))
                .andExpect(jsonPath("$.seasonTable[0].changeRate").value(nullValue()));
        mvc.perform(get("/api/analysis/trend").session(session).param("metric", "CLICK"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field").value("metric"));
        mvc.perform(get("/api/analysis/trend").session(session).param("regionCode", "NOPE"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field").value("regionCode"));
    }

    @Test
    void consumptionComparisonKeepsUnitsAndLimitsIncompleteShares() throws Exception {
        mvc.perform(get("/api/analysis/consumption").session(session).param("regionCode", A).param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shareMonth").value(10))
                .andExpect(jsonPath("$.visitSpend", hasSize(12)))
                .andExpect(jsonPath("$.visitSpend[9].visit").value(51))
                .andExpect(jsonPath("$.visitSpend[9].spend").value(29))
                .andExpect(jsonPath("$.visitSpend[9].note").value("각 지표의 변화만 비교"))
                .andExpect(jsonPath("$.visitSpend[10].spend").value(nullValue()))
                .andExpect(jsonPath("$.visitSpend[10].note").value("소비 비교 불가 · 0으로 대체하지 않음"))
                .andExpect(jsonPath("$.visitSource.unitLabel").value("만 명"))
                .andExpect(jsonPath("$.spendSource.unitLabel").value("억 원"))
                .andExpect(jsonPath("$.shares.hasData").value(true))
                .andExpect(jsonPath("$.shares.segments[0].category").value("CAFE"))
                .andExpect(jsonPath("$.shares.segments[0].comparable").value(true))
                .andExpect(jsonPath("$.shares.segments[1].category").value("RESTAURANT"))
                .andExpect(jsonPath("$.shares.segments[1].comparable").value(false))
                .andExpect(jsonPath("$.shares.segments[1].foreign").value(nullValue()))
                .andExpect(jsonPath("$.shares.timeSlots", hasSize(4)))
                .andExpect(jsonPath("$.shares.timeSlots[2].label").value("14시부터 18시"))
                .andExpect(jsonPath("$.shares.timeSlots[2].share").value(40.0))
                .andExpect(jsonPath("$.notice").value(containsString("전환율·1인당 소비액은 계산하지 않습니다")));

        mvc.perform(get("/api/analysis/consumption").session(session).param("regionCode", B).param("year", "2025"))
                .andExpect(jsonPath("$.shares.hasData").value(false))
                .andExpect(jsonPath("$.shares.message").value(containsString("소비 비중 자료가 없습니다")));
    }

    @Test
    void loginIsRequired() throws Exception {
        mvc.perform(get("/api/analysis/trend")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/analysis/consumption")).andExpect(status().isUnauthorized());
    }

    private long source(long user, String fileName, String unit) {
        return sourceDao.insert(new DataSourceFile(null, user, fileName, "v1", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 1), unit, "PASS", "테스트 제공자", 12, LocalDate.now()));
    }

    private static RegionConsumptionStat share(long source, LocalDate month, String category, String segment,
            String slot, String rate) {
        return new RegionConsumptionStat(null, source, A, month, category, segment, slot, new BigDecimal(rate));
    }
}
