package com.red.jejucalendar.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.red.jejucalendar.domain.PromoSuggestionItem;

/** PROMO_SUGGESTION_ITEM 테이블 DAO (복합 PK: SUGGESTION_ID + ITEM_TYPE) */
@Repository
public class PromoSuggestionItemDao {

    private static final String COLUMNS = "SUGGESTION_ID, ITEM_TYPE, ITEM_CONTENT, ITEM_REASON, CHECK_NOTE";
    private static final RowMapper<PromoSuggestionItem> ROW_MAPPER = PromoSuggestionItemDao::mapRow;

    private final JdbcTemplate jdbc;

    public PromoSuggestionItemDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(PromoSuggestionItem item) {
        jdbc.update("INSERT INTO PROMO_SUGGESTION_ITEM (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?)",
                item.suggestionId(), item.itemType(), item.itemContent(), item.itemReason(), item.checkNote());
    }

    public Optional<PromoSuggestionItem> find(long suggestionId, String itemType) {
        return jdbc.query("SELECT " + COLUMNS + " FROM PROMO_SUGGESTION_ITEM WHERE SUGGESTION_ID = ? AND ITEM_TYPE = ?",
                ROW_MAPPER, suggestionId, itemType).stream().findFirst();
    }

    /** 제안 카드의 네 항목(TIMING, AUDIENCE, PRODUCT, ATTRACTION) */
    public List<PromoSuggestionItem> findBySuggestionId(long suggestionId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM PROMO_SUGGESTION_ITEM WHERE SUGGESTION_ID = ? ORDER BY ITEM_TYPE",
                ROW_MAPPER, suggestionId);
    }

    public int update(PromoSuggestionItem item) {
        return jdbc.update("UPDATE PROMO_SUGGESTION_ITEM SET ITEM_CONTENT = ?, ITEM_REASON = ?, CHECK_NOTE = ?"
                        + " WHERE SUGGESTION_ID = ? AND ITEM_TYPE = ?",
                item.itemContent(), item.itemReason(), item.checkNote(), item.suggestionId(), item.itemType());
    }

    public int delete(long suggestionId, String itemType) {
        return jdbc.update("DELETE FROM PROMO_SUGGESTION_ITEM WHERE SUGGESTION_ID = ? AND ITEM_TYPE = ?",
                suggestionId, itemType);
    }

    /** 제안을 지우기 전에 항목을 먼저 지움 (ItemSuggestion FK는 부모 삭제를 막음) */
    public int deleteBySuggestionId(long suggestionId) {
        return jdbc.update("DELETE FROM PROMO_SUGGESTION_ITEM WHERE SUGGESTION_ID = ?", suggestionId);
    }

    private static PromoSuggestionItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PromoSuggestionItem(
                rs.getLong("SUGGESTION_ID"),
                rs.getString("ITEM_TYPE"),
                rs.getString("ITEM_CONTENT"),
                rs.getString("ITEM_REASON"),
                rs.getString("CHECK_NOTE"));
    }
}
