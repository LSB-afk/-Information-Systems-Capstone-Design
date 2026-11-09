package com.red.jejucalendar.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.red.jejucalendar.domain.RecommendationCriteria;

/** RECOMMENDATION_CRITERIA 테이블 DAO (PK는 SEQ_RECOMMENDATION_CRITERIA 시퀀스로 발급) */
@Repository
public class RecommendationCriteriaDao {

    private static final String COLUMNS = "CRITERIA_ID, DECLINE_RATE_THRESHOLD, SOURCE_INFO, BASE_PERIOD,"
            + " REASON_TEMPLATE, REQUIRED_CONDITION, UPDATED_AT";
    private static final RowMapper<RecommendationCriteria> ROW_MAPPER = RecommendationCriteriaDao::mapRow;

    private final JdbcTemplate jdbc;

    public RecommendationCriteriaDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 새 추천 기준을 저장하고 발급된 CRITERIA_ID를 돌려줌 */
    public long insert(RecommendationCriteria criteria) {
        long id = JdbcSupport.nextVal(jdbc, "SEQ_RECOMMENDATION_CRITERIA");
        jdbc.update("INSERT INTO RECOMMENDATION_CRITERIA (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, criteria.declineRateThreshold(), criteria.sourceInfo(), criteria.basePeriod(),
                criteria.reasonTemplate(), criteria.requiredCondition(), JdbcSupport.date(criteria.updatedAt()));
        return id;
    }

    public Optional<RecommendationCriteria> findById(long criteriaId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM RECOMMENDATION_CRITERIA WHERE CRITERIA_ID = ?",
                ROW_MAPPER, criteriaId).stream().findFirst();
    }

    public List<RecommendationCriteria> findAll() {
        return jdbc.query("SELECT " + COLUMNS + " FROM RECOMMENDATION_CRITERIA ORDER BY CRITERIA_ID", ROW_MAPPER);
    }

    public int update(RecommendationCriteria criteria) {
        return jdbc.update("UPDATE RECOMMENDATION_CRITERIA SET DECLINE_RATE_THRESHOLD = ?, SOURCE_INFO = ?,"
                        + " BASE_PERIOD = ?, REASON_TEMPLATE = ?, REQUIRED_CONDITION = ?, UPDATED_AT = ?"
                        + " WHERE CRITERIA_ID = ?",
                criteria.declineRateThreshold(), criteria.sourceInfo(), criteria.basePeriod(),
                criteria.reasonTemplate(), criteria.requiredCondition(), JdbcSupport.date(criteria.updatedAt()),
                criteria.criteriaId());
    }

    public int deleteById(long criteriaId) {
        return jdbc.update("DELETE FROM RECOMMENDATION_CRITERIA WHERE CRITERIA_ID = ?", criteriaId);
    }

    private static RecommendationCriteria mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new RecommendationCriteria(
                rs.getLong("CRITERIA_ID"),
                JdbcSupport.integer(rs, "DECLINE_RATE_THRESHOLD"),
                rs.getString("SOURCE_INFO"),
                rs.getString("BASE_PERIOD"),
                rs.getString("REASON_TEMPLATE"),
                rs.getString("REQUIRED_CONDITION"),
                JdbcSupport.localDate(rs, "UPDATED_AT"));
    }
}
