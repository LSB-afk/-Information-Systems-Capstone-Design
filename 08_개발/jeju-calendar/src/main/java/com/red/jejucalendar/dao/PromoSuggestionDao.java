package com.red.jejucalendar.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.red.jejucalendar.domain.PromoSuggestion;

/** PROMO_SUGGESTION 테이블 DAO (PK는 SEQ_PROMO_SUGGESTION 시퀀스로 발급) */
@Repository
public class PromoSuggestionDao {

    private static final String COLUMNS = "SUGGESTION_ID, CRITERIA_ID, TARGET_MONTH, TITLE, REASON, REGION_CODE,"
            + " DESCRIPTION, RECOMMENDED_DATE, CHANNEL, PREPARATION_TASK, TAG, SOURCE_ID";
    private static final RowMapper<PromoSuggestion> ROW_MAPPER = PromoSuggestionDao::mapRow;

    private final JdbcTemplate jdbc;

    public PromoSuggestionDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 새 홍보 제안을 저장하고 발급된 SUGGESTION_ID를 돌려줌 */
    public long insert(PromoSuggestion s) {
        long id = JdbcSupport.nextVal(jdbc, "SEQ_PROMO_SUGGESTION");
        jdbc.update("INSERT INTO PROMO_SUGGESTION (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, s.criteriaId(), JdbcSupport.date(s.targetMonth()), s.title(), s.reason(), s.regionCode(),
                s.description(), JdbcSupport.date(s.recommendedDate()), s.channel(), s.preparationTask(), s.tag(),
                s.sourceId());
        return id;
    }

    public Optional<PromoSuggestion> findById(long suggestionId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM PROMO_SUGGESTION WHERE SUGGESTION_ID = ?",
                ROW_MAPPER, suggestionId).stream().findFirst();
    }

    /** 홍보 제안 화면: 지역과 대상 월(매월 1일)로 조회 */
    public List<PromoSuggestion> findByRegionAndMonth(String regionCode, LocalDate targetMonth) {
        return jdbc.query("SELECT " + COLUMNS + " FROM PROMO_SUGGESTION WHERE REGION_CODE = ? AND TARGET_MONTH = ?"
                + " ORDER BY SUGGESTION_ID", ROW_MAPPER, regionCode, JdbcSupport.date(targetMonth));
    }

    /** 대상 월(매월 1일)이 from 이상 to 이하인 모든 지역의 제안 */
    public List<PromoSuggestion> findByTargetMonthBetween(LocalDate from, LocalDate to) {
        return jdbc.query("SELECT " + COLUMNS + " FROM PROMO_SUGGESTION WHERE TARGET_MONTH BETWEEN ? AND ?"
                + " ORDER BY REGION_CODE, TARGET_MONTH, SUGGESTION_ID", ROW_MAPPER, JdbcSupport.date(from), JdbcSupport.date(to));
    }

    /** 분석 자료를 바꾸면 제안도 바뀌므로 자료 기준 조회 */
    public List<PromoSuggestion> findBySourceId(long sourceId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM PROMO_SUGGESTION WHERE SOURCE_ID = ? ORDER BY SUGGESTION_ID",
                ROW_MAPPER, sourceId);
    }

    public int update(PromoSuggestion s) {
        return jdbc.update("UPDATE PROMO_SUGGESTION SET CRITERIA_ID = ?, TARGET_MONTH = ?, TITLE = ?, REASON = ?,"
                        + " REGION_CODE = ?, DESCRIPTION = ?, RECOMMENDED_DATE = ?, CHANNEL = ?, PREPARATION_TASK = ?,"
                        + " TAG = ?, SOURCE_ID = ? WHERE SUGGESTION_ID = ?",
                s.criteriaId(), JdbcSupport.date(s.targetMonth()), s.title(), s.reason(), s.regionCode(),
                s.description(), JdbcSupport.date(s.recommendedDate()), s.channel(), s.preparationTask(), s.tag(),
                s.sourceId(), s.suggestionId());
    }

    /** 이 제안으로 만든 계획의 SUGGESTION_ID는 FK 규칙(ON DELETE SET NULL)에 따라 NULL이 됨 */
    public int deleteById(long suggestionId) {
        return jdbc.update("DELETE FROM PROMO_SUGGESTION WHERE SUGGESTION_ID = ?", suggestionId);
    }

    private static PromoSuggestion mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PromoSuggestion(
                rs.getLong("SUGGESTION_ID"),
                rs.getLong("CRITERIA_ID"),
                JdbcSupport.localDate(rs, "TARGET_MONTH"),
                rs.getString("TITLE"),
                rs.getString("REASON"),
                rs.getString("REGION_CODE"),
                rs.getString("DESCRIPTION"),
                JdbcSupport.localDate(rs, "RECOMMENDED_DATE"),
                rs.getString("CHANNEL"),
                rs.getString("PREPARATION_TASK"),
                rs.getString("TAG"),
                rs.getLong("SOURCE_ID"));
    }
}
