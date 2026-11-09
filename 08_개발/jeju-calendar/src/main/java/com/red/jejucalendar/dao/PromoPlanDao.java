package com.red.jejucalendar.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.red.jejucalendar.domain.PromoPlan;

/** PROMO_PLAN 테이블 DAO (PK는 SEQ_PROMO_PLAN 시퀀스로 발급) */
@Repository
public class PromoPlanDao {

    private static final String COLUMNS = "PLAN_ID, BUSINESS_ID, SUGGESTION_ID, TITLE, SCHEDULED_DATE, CHANNEL,"
            + " PREPARATION_NOTE";
    private static final RowMapper<PromoPlan> ROW_MAPPER = PromoPlanDao::mapRow;

    private final JdbcTemplate jdbc;

    public PromoPlanDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 새 계획을 저장하고 발급된 PLAN_ID를 돌려줌 (직접 추가한 계획은 suggestionId가 null) */
    public long insert(PromoPlan plan) {
        long id = JdbcSupport.nextVal(jdbc, "SEQ_PROMO_PLAN");
        jdbc.update("INSERT INTO PROMO_PLAN (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, plan.businessId(), plan.suggestionId(), plan.title(), JdbcSupport.date(plan.scheduledDate()),
                plan.channel(), plan.preparationNote());
        return id;
    }

    public Optional<PromoPlan> findById(long planId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM PROMO_PLAN WHERE PLAN_ID = ?", ROW_MAPPER, planId)
                .stream().findFirst();
    }

    public List<PromoPlan> findByBusinessId(long businessId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM PROMO_PLAN WHERE BUSINESS_ID = ? ORDER BY SCHEDULED_DATE, PLAN_ID",
                ROW_MAPPER, businessId);
    }

    /** 3개월 캘린더 화면: from 이상 to 이하 날짜의 계획 */
    public List<PromoPlan> findByBusinessIdAndPeriod(long businessId, LocalDate from, LocalDate to) {
        return jdbc.query("SELECT " + COLUMNS + " FROM PROMO_PLAN WHERE BUSINESS_ID = ?"
                        + " AND SCHEDULED_DATE BETWEEN ? AND ? ORDER BY SCHEDULED_DATE, PLAN_ID",
                ROW_MAPPER, businessId, JdbcSupport.date(from), JdbcSupport.date(to));
    }

    public int update(PromoPlan plan) {
        return jdbc.update("UPDATE PROMO_PLAN SET SUGGESTION_ID = ?, TITLE = ?, SCHEDULED_DATE = ?, CHANNEL = ?,"
                        + " PREPARATION_NOTE = ? WHERE PLAN_ID = ?",
                plan.suggestionId(), plan.title(), JdbcSupport.date(plan.scheduledDate()), plan.channel(),
                plan.preparationNote(), plan.planId());
    }

    /** 이 계획의 실행 결과 기록도 FK 규칙(ON DELETE CASCADE)에 따라 함께 지워짐 */
    public int deleteById(long planId) {
        return jdbc.update("DELETE FROM PROMO_PLAN WHERE PLAN_ID = ?", planId);
    }

    private static PromoPlan mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PromoPlan(
                rs.getLong("PLAN_ID"),
                rs.getLong("BUSINESS_ID"),
                JdbcSupport.longValue(rs, "SUGGESTION_ID"),
                rs.getString("TITLE"),
                JdbcSupport.localDate(rs, "SCHEDULED_DATE"),
                rs.getString("CHANNEL"),
                rs.getString("PREPARATION_NOTE"));
    }
}
