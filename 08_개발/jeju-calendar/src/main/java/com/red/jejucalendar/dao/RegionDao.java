package com.red.jejucalendar.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.red.jejucalendar.domain.Region;

/** REGION 테이블 DAO (PK는 지역 코드라 시퀀스를 쓰지 않음) */
@Repository
public class RegionDao {

    private static final String COLUMNS = "REGION_CODE, REGION_NAME";
    private static final RowMapper<Region> ROW_MAPPER =
            (rs, rowNum) -> new Region(rs.getString("REGION_CODE"), rs.getString("REGION_NAME"));

    private final JdbcTemplate jdbc;

    public RegionDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(Region region) {
        jdbc.update("INSERT INTO REGION (" + COLUMNS + ") VALUES (?, ?)",
                region.regionCode(), region.regionName());
    }

    public Optional<Region> findByCode(String regionCode) {
        return jdbc.query("SELECT " + COLUMNS + " FROM REGION WHERE REGION_CODE = ?", ROW_MAPPER, regionCode)
                .stream().findFirst();
    }

    public List<Region> findAll() {
        return jdbc.query("SELECT " + COLUMNS + " FROM REGION ORDER BY REGION_CODE", ROW_MAPPER);
    }

    public int update(Region region) {
        return jdbc.update("UPDATE REGION SET REGION_NAME = ? WHERE REGION_CODE = ?",
                region.regionName(), region.regionCode());
    }

    public int deleteByCode(String regionCode) {
        return jdbc.update("DELETE FROM REGION WHERE REGION_CODE = ?", regionCode);
    }
}
