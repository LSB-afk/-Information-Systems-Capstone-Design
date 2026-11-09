package com.red.jejucalendar.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.red.jejucalendar.domain.RegionConsumptionStat;

/** REGION_CONSUMPTION_STAT 테이블 DAO (PK는 SEQ_REGION_CONSUMPTION_STAT 시퀀스로 발급) */
@Repository
public class RegionConsumptionStatDao {

    private static final String COLUMNS = "CONSUMPTION_STAT_ID, SOURCE_ID, REGION_CODE, STAT_MONTH, CATEGORY,"
            + " SEGMENT, TIME_SLOT, SHARE_RATE";
    private static final String INSERT_WITH_SEQUENCE = "INSERT INTO REGION_CONSUMPTION_STAT (" + COLUMNS + ")"
            + " VALUES (SEQ_REGION_CONSUMPTION_STAT.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
    private static final RowMapper<RegionConsumptionStat> ROW_MAPPER = RegionConsumptionStatDao::mapRow;

    private final JdbcTemplate jdbc;

    public RegionConsumptionStatDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 한 행을 저장하고 발급된 CONSUMPTION_STAT_ID를 돌려줌 */
    public long insert(RegionConsumptionStat stat) {
        long id = JdbcSupport.nextVal(jdbc, "SEQ_REGION_CONSUMPTION_STAT");
        jdbc.update("INSERT INTO REGION_CONSUMPTION_STAT (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, stat.sourceId(), stat.regionCode(), JdbcSupport.date(stat.statMonth()), stat.category(),
                stat.segment(), stat.timeSlot(), stat.shareRate());
        return id;
    }

    /** CSV 가져오기용 일괄 저장 (ID는 시퀀스가 행마다 발급) */
    public int insertAll(List<RegionConsumptionStat> stats) {
        int[][] counts = jdbc.batchUpdate(INSERT_WITH_SEQUENCE, stats, 500, (ps, stat) -> {
            ps.setLong(1, stat.sourceId());
            ps.setString(2, stat.regionCode());
            ps.setDate(3, JdbcSupport.date(stat.statMonth()));
            ps.setString(4, stat.category());
            ps.setString(5, stat.segment());
            ps.setString(6, stat.timeSlot());
            ps.setBigDecimal(7, stat.shareRate());
        });
        return stats.size() - JdbcSupport.countFailures(counts);
    }

    public Optional<RegionConsumptionStat> findById(long consumptionStatId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM REGION_CONSUMPTION_STAT WHERE CONSUMPTION_STAT_ID = ?",
                ROW_MAPPER, consumptionStatId).stream().findFirst();
    }

    /** 소비 비교 화면: 자료·지역·월(매월 1일) 기준의 업종·고객·시간대별 비중 */
    public List<RegionConsumptionStat> findByRegionAndMonth(long sourceId, String regionCode, LocalDate statMonth) {
        return jdbc.query("SELECT " + COLUMNS + " FROM REGION_CONSUMPTION_STAT WHERE SOURCE_ID = ? AND REGION_CODE = ?"
                        + " AND STAT_MONTH = ? ORDER BY CATEGORY, SEGMENT, TIME_SLOT",
                ROW_MAPPER, sourceId, regionCode, JdbcSupport.date(statMonth));
    }

    public List<RegionConsumptionStat> findBySourceId(long sourceId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM REGION_CONSUMPTION_STAT WHERE SOURCE_ID = ?"
                + " ORDER BY REGION_CODE, STAT_MONTH, CATEGORY, SEGMENT, TIME_SLOT", ROW_MAPPER, sourceId);
    }

    public int update(RegionConsumptionStat stat) {
        return jdbc.update("UPDATE REGION_CONSUMPTION_STAT SET SHARE_RATE = ? WHERE CONSUMPTION_STAT_ID = ?",
                stat.shareRate(), stat.consumptionStatId());
    }

    public int deleteById(long consumptionStatId) {
        return jdbc.update("DELETE FROM REGION_CONSUMPTION_STAT WHERE CONSUMPTION_STAT_ID = ?", consumptionStatId);
    }

    /** 같은 자료를 다시 가져올 때 이전 행을 지움 */
    public int deleteBySourceId(long sourceId) {
        return jdbc.update("DELETE FROM REGION_CONSUMPTION_STAT WHERE SOURCE_ID = ?", sourceId);
    }

    private static RegionConsumptionStat mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new RegionConsumptionStat(
                rs.getLong("CONSUMPTION_STAT_ID"),
                rs.getLong("SOURCE_ID"),
                rs.getString("REGION_CODE"),
                JdbcSupport.localDate(rs, "STAT_MONTH"),
                rs.getString("CATEGORY"),
                rs.getString("SEGMENT"),
                rs.getString("TIME_SLOT"),
                rs.getBigDecimal("SHARE_RATE"));
    }
}
