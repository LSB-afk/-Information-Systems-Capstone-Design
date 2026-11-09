package com.red.jejucalendar.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.red.jejucalendar.domain.RegionMonthlyStat;

/** REGION_MONTHLY_STAT 테이블 DAO (PK는 SEQ_REGION_MONTHLY_STAT 시퀀스로 발급) */
@Repository
public class RegionMonthlyStatDao {

    private static final String COLUMNS = "STAT_ID, SOURCE_ID, REGION_CODE, STAT_MONTH, METRIC_TYPE, STAT_VALUE";
    private static final String INSERT_WITH_SEQUENCE = "INSERT INTO REGION_MONTHLY_STAT (" + COLUMNS + ")"
            + " VALUES (SEQ_REGION_MONTHLY_STAT.NEXTVAL, ?, ?, ?, ?, ?)";
    private static final RowMapper<RegionMonthlyStat> ROW_MAPPER = RegionMonthlyStatDao::mapRow;

    private final JdbcTemplate jdbc;

    public RegionMonthlyStatDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 한 행을 저장하고 발급된 STAT_ID를 돌려줌 */
    public long insert(RegionMonthlyStat stat) {
        long id = JdbcSupport.nextVal(jdbc, "SEQ_REGION_MONTHLY_STAT");
        jdbc.update("INSERT INTO REGION_MONTHLY_STAT (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?)",
                id, stat.sourceId(), stat.regionCode(), JdbcSupport.date(stat.statMonth()), stat.metricType(),
                stat.statValue());
        return id;
    }

    /** CSV 가져오기용 일괄 저장 (STAT_ID는 시퀀스가 행마다 발급) */
    public int insertAll(List<RegionMonthlyStat> stats) {
        int[][] counts = jdbc.batchUpdate(INSERT_WITH_SEQUENCE, stats, 500, (ps, stat) -> {
            ps.setLong(1, stat.sourceId());
            ps.setString(2, stat.regionCode());
            ps.setDate(3, JdbcSupport.date(stat.statMonth()));
            ps.setString(4, stat.metricType());
            if (stat.statValue() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, stat.statValue());
            }
        });
        return stats.size() - JdbcSupport.countFailures(counts);
    }

    public Optional<RegionMonthlyStat> findById(long statId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM REGION_MONTHLY_STAT WHERE STAT_ID = ?", ROW_MAPPER, statId)
                .stream().findFirst();
    }

    /** 지역 분석 화면: 자료 하나에서 지역·지표의 기간별 값 (from, to는 매월 1일) */
    public List<RegionMonthlyStat> findByRegionAndPeriod(long sourceId, String regionCode, String metricType,
            LocalDate from, LocalDate to) {
        return jdbc.query("SELECT " + COLUMNS + " FROM REGION_MONTHLY_STAT WHERE SOURCE_ID = ? AND REGION_CODE = ?"
                        + " AND METRIC_TYPE = ? AND STAT_MONTH BETWEEN ? AND ? ORDER BY STAT_MONTH",
                ROW_MAPPER, sourceId, regionCode, metricType, JdbcSupport.date(from), JdbcSupport.date(to));
    }

    public List<RegionMonthlyStat> findBySourceId(long sourceId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM REGION_MONTHLY_STAT WHERE SOURCE_ID = ?"
                + " ORDER BY REGION_CODE, METRIC_TYPE, STAT_MONTH", ROW_MAPPER, sourceId);
    }

    public int update(RegionMonthlyStat stat) {
        return jdbc.update("UPDATE REGION_MONTHLY_STAT SET STAT_VALUE = ? WHERE STAT_ID = ?",
                stat.statValue(), stat.statId());
    }

    public int deleteById(long statId) {
        return jdbc.update("DELETE FROM REGION_MONTHLY_STAT WHERE STAT_ID = ?", statId);
    }

    /** 같은 자료를 다시 가져올 때 이전 행을 지움 */
    public int deleteBySourceId(long sourceId) {
        return jdbc.update("DELETE FROM REGION_MONTHLY_STAT WHERE SOURCE_ID = ?", sourceId);
    }

    private static RegionMonthlyStat mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new RegionMonthlyStat(
                rs.getLong("STAT_ID"),
                rs.getLong("SOURCE_ID"),
                rs.getString("REGION_CODE"),
                JdbcSupport.localDate(rs, "STAT_MONTH"),
                rs.getString("METRIC_TYPE"),
                JdbcSupport.integer(rs, "STAT_VALUE"));
    }
}
