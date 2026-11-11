package com.red.jejucalendar.support.ijto;

import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 검증된 출처만 한 트랜잭션으로 추가함 기존 사용자·관측값·스냅샷을 덮어쓰지 않음 */
@Service
public class IjtoImporter {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    public IjtoImporter(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    public record Result(int snapshots, int regions, int observations, int shares, int insertedRows) {}

    @Transactional
    public Result importDataset(IjtoDataset dataset) {
        IjtoDataset.require(jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='IJTO_DATA_SNAPSHOT'", Integer.class) == 1,
                "먼저 db/04_ijto_precision_and_snapshot.sql 실행 필요");
        var columns = jdbc.queryForList("SELECT DATA_SCALE FROM USER_TAB_COLUMNS WHERE TABLE_NAME='REGION_MONTHLY_STAT' AND COLUMN_NAME='STAT_VALUE'");
        IjtoDataset.require(columns.size() == 1 && columns.get(0).get("DATA_SCALE") == null, "STAT_VALUE의 NUMBER 확장 필요");
        int before = totalRows();
        for (var snapshot : dataset.snapshots()) snapshot(dataset, snapshot);
        for (var area : dataset.areas()) {
            List<String> existing = jdbc.queryForList("SELECT REGION_NAME FROM REGION WHERE REGION_CODE=?", String.class, area.code());
            if (existing.isEmpty()) {
                jdbc.update("INSERT INTO REGION (REGION_CODE, REGION_NAME) VALUES (?, ?)", area.code(), area.name());
            } else {
                IjtoDataset.require(existing.get(0).equals(area.name()), "지역 코드·이름 충돌: " + area.code());
            }
        }
        long operator = user("operator@example.com", "SERVICE");
        long owner = user("cafe@example.com", "BUSINESS");
        String region = dataset.areas().stream().filter(a -> a.name().equals("구좌읍")).findFirst().orElseThrow().code();
        demoBusiness(owner, operator, region);
        for (String metric : new String[]{"VISIT", "SPEND"}) {
            var rows = dataset.observations().stream().filter(o -> o.metric().equals(metric)).toList();
            IjtoDataset.require(!rows.isEmpty(), "관측값 누락: " + metric);
            long source = source(dataset, operator, metric, rows);
            for (var row : rows) {
                List<BigDecimal> existing = jdbc.queryForList("SELECT STAT_VALUE FROM REGION_MONTHLY_STAT"
                        + " WHERE SOURCE_ID=? AND REGION_CODE=? AND STAT_MONTH=? AND METRIC_TYPE=?",
                        BigDecimal.class, source, row.region(), Date.valueOf(row.month()), metric);
                if (existing.isEmpty()) {
                    jdbc.update("INSERT INTO REGION_MONTHLY_STAT (STAT_ID,SOURCE_ID,REGION_CODE,STAT_MONTH,METRIC_TYPE,STAT_VALUE)"
                            + " VALUES (SEQ_REGION_MONTHLY_STAT.NEXTVAL,?,?,?,?,?)",
                            source, row.region(), Date.valueOf(row.month()), metric, row.value());
                } else {
                    IjtoDataset.require(existing.get(0) != null && existing.get(0).compareTo(row.value()) == 0, "기존 관측값 충돌");
                }
            }
            IjtoDataset.require(jdbc.queryForObject("SELECT COUNT(*) FROM REGION_MONTHLY_STAT WHERE SOURCE_ID=?", Integer.class, source) == rows.size(),
                    "자료별 관측 행 수 불일치");
            if (metric.equals("SPEND")) {
                for (var share : dataset.shares()) {
                    List<BigDecimal> existing = jdbc.queryForList("SELECT SHARE_RATE FROM REGION_CONSUMPTION_STAT"
                            + " WHERE SOURCE_ID=? AND REGION_CODE=? AND STAT_MONTH=? AND CATEGORY='RESTAURANT' AND SEGMENT=? AND TIME_SLOT='ALL'",
                            BigDecimal.class, source, share.region(), Date.valueOf(share.month()), share.segment());
                    if (existing.isEmpty()) {
                        jdbc.update("INSERT INTO REGION_CONSUMPTION_STAT (CONSUMPTION_STAT_ID,SOURCE_ID,REGION_CODE,STAT_MONTH,CATEGORY,SEGMENT,TIME_SLOT,SHARE_RATE)"
                                + " VALUES (SEQ_REGION_CONSUMPTION_STAT.NEXTVAL,?,?,?,'RESTAURANT',?,'ALL',?)",
                                source, share.region(), Date.valueOf(share.month()), share.segment(), share.rate());
                    } else {
                        IjtoDataset.require(existing.get(0) != null && existing.get(0).compareTo(share.rate()) == 0, "기존 소비 비중 충돌");
                    }
                }
                IjtoDataset.require(jdbc.queryForObject("SELECT COUNT(*) FROM REGION_CONSUMPTION_STAT WHERE SOURCE_ID=?", Integer.class, source) == dataset.shares().size(),
                        "자료별 소비 비중 행 수 불일치");
            }
        }
        return new Result(dataset.snapshots().size(), dataset.areas().size(), dataset.observations().size(),
                dataset.shares().size(), totalRows() - before);
    }

    private void snapshot(IjtoDataset data, IjtoDataset.Snapshot snapshot) {
        IjtoDataset.require(IjtoDataset.sha256(snapshot.payload().getBytes(StandardCharsets.UTF_8)).equals(snapshot.sha256()), "스냅샷 SHA-256 불일치");
        String key = data.snapshotDate() + ":" + snapshot.file();
        List<Map<String, Object>> existing = jdbc.queryForList("SELECT COMMIT_SHA,CONTENT_SHA256,FILE_PATH FROM IJTO_DATA_SNAPSHOT WHERE SNAPSHOT_KEY=?", key);
        if (existing.isEmpty()) {
            jdbc.update(connection -> {
                var ps = connection.prepareStatement("INSERT INTO IJTO_DATA_SNAPSHOT"
                        + " (SNAPSHOT_KEY,REPOSITORY,COMMIT_SHA,FILE_PATH,CONTENT_SHA256,SNAPSHOT_DATE,PAYLOAD) VALUES (?,?,?,?,?,?,?)");
                ps.setString(1, key); ps.setString(2, data.repository()); ps.setString(3, data.commit());
                ps.setString(4, snapshot.sourcePath()); ps.setString(5, snapshot.sha256());
                ps.setDate(6, Date.valueOf(data.snapshotDate()));
                ps.setCharacterStream(7, new StringReader(snapshot.payload()), snapshot.payload().length());
                return ps;
            });
        } else {
            var row = existing.get(0);
            IjtoDataset.require(data.commit().equals(row.get("COMMIT_SHA")) && snapshot.sha256().equals(row.get("CONTENT_SHA256"))
                    && snapshot.sourcePath().equals(row.get("FILE_PATH")), "기존 스냅샷 출처 충돌");
        }
        String payload = jdbc.queryForObject("SELECT PAYLOAD FROM IJTO_DATA_SNAPSHOT WHERE SNAPSHOT_KEY=?", String.class, key);
        IjtoDataset.require(snapshot.payload().equals(payload), "DB 스냅샷 원문 불일치");
    }

    private long user(String email, String role) {
        var existing = jdbc.queryForList("SELECT USER_ID,ROLE FROM USERS WHERE EMAIL=?", email);
        if (!existing.isEmpty()) {
            IjtoDataset.require(role.equals(existing.get(0).get("ROLE")), "시연 계정 역할 충돌");
            return ((Number) existing.get(0).get("USER_ID")).longValue();
        }
        long id = jdbc.queryForObject("SELECT SEQ_USERS.NEXTVAL FROM DUAL", Long.class);
        jdbc.update("INSERT INTO USERS (USER_ID,EMAIL,PASSWORD_HASH,ROLE,CREATED_AT) VALUES (?,?,?,?,SYSDATE)",
                id, email, encoder.encode("JejuDemo26!"), role);
        return id;
    }

    private void demoBusiness(long owner, long operator, String region) {
        var ids = jdbc.queryForList("SELECT BUSINESS_ID FROM BUSINESS WHERE USER_ID=? AND BUSINESS_NAME=? AND REGION_CODE=? AND CATEGORY='RESTAURANT'",
                Long.class, owner, "구좌 시연 음식점", region);
        long business;
        if (ids.isEmpty()) {
            business = jdbc.queryForObject("SELECT SEQ_BUSINESS.NEXTVAL FROM DUAL", Long.class);
            jdbc.update("INSERT INTO BUSINESS (BUSINESS_ID,USER_ID,BUSINESS_NAME,CATEGORY,REGION_CODE) VALUES (?,?,?,'RESTAURANT',?)",
                    business, owner, "구좌 시연 음식점", region);
        } else {
            IjtoDataset.require(ids.size() == 1, "시연 사업체 중복");
            business = ids.get(0);
        }
        if (jdbc.queryForObject("SELECT COUNT(*) FROM BUSINESS_PERMISSION WHERE USER_ID=? AND BUSINESS_ID=?", Integer.class, operator, business) == 0) {
            jdbc.update("INSERT INTO BUSINESS_PERMISSION (USER_ID,BUSINESS_ID,PERMISSION_TYPE,GRANTED_AT) VALUES (?,?,'MANAGER',SYSDATE)", operator, business);
        }
    }

    private long source(IjtoDataset data, long operator, String metric, List<IjtoDataset.Observation> rows) {
        String version = "ijto-" + data.snapshotDate().toString().replace("-", "") + "@" + data.commit().substring(0, 12) + "-" + metric;
        var ids = jdbc.queryForList("SELECT SOURCE_ID FROM DATA_SOURCE WHERE SOURCE_VERSION=?", Long.class, version);
        long id;
        if (ids.isEmpty()) {
            id = jdbc.queryForObject("SELECT SEQ_DATA_SOURCE.NEXTVAL FROM DUAL", Long.class);
            LocalDate from = rows.stream().map(IjtoDataset.Observation::month).min(LocalDate::compareTo).orElseThrow();
            LocalDate to = rows.stream().map(IjtoDataset.Observation::month).max(LocalDate::compareTo).orElseThrow();
            jdbc.update("INSERT INTO DATA_SOURCE (SOURCE_ID,USER_ID,FILE_NAME,SOURCE_VERSION,START_MONTH,END_MONTH,UNIT,VALIDATION_STATUS,PROVIDER_BASIS,ROW_COUNT,REGISTERED_AT)"
                    + " VALUES (?,?,?,?,?,?,?,'PASS',?,?,SYSDATE)", id, operator,
                    metric.equals("VISIT") ? "recent-data.json" : "case+recent-data.json", version, Date.valueOf(from), Date.valueOf(to),
                    metric.equals("VISIT") ? "PERSON_10K" : "KRW_100M",
                    metric.equals("VISIT") ? "제주관광공사/SKT 월별 읍면동" : "제주관광공사/BC카드 음식점업", rows.size());
        } else {
            IjtoDataset.require(ids.size() == 1, "출처 버전 중복");
            id = ids.get(0);
            IjtoDataset.require(jdbc.queryForObject("SELECT COUNT(*) FROM DATA_SOURCE WHERE SOURCE_ID=? AND VALIDATION_STATUS='PASS' AND UNIT=? AND ROW_COUNT=?",
                    Integer.class, id, metric.equals("VISIT") ? "PERSON_10K" : "KRW_100M", rows.size()) == 1, "기존 출처 메타데이터 충돌");
        }
        Map<String, String> checks = Map.of("PERIOD", "관측 연월 확인, 빠진 달을 0으로 채우지 않음",
                "AGGREGATION", "월별 구좌 집계, 명·원을 만 명·억 원으로 정확히 환산",
                "REGION_CODE", "JSON의 읍면동 방문 코드와 지역 이름 확인",
                "MISSING_VALUE", "추출 관측값의 누락·음수·고객군 합계 검사",
                "PROVIDER", "출처 커밋·파일 SHA-256 일치, 원천 경고는 원문 보존");
        for (var check : checks.entrySet()) {
            var statuses = jdbc.queryForList("SELECT RESULT FROM DATA_VALIDATION_RESULT WHERE SOURCE_ID=? AND VALIDATION_ITEM=?", String.class, id, check.getKey());
            if (statuses.isEmpty()) {
                jdbc.update("INSERT INTO DATA_VALIDATION_RESULT (SOURCE_ID,VALIDATION_ITEM,RESULT,REASON,VALIDATED_AT) VALUES (?,?,'PASS',?,SYSDATE)",
                        id, check.getKey(), check.getValue());
            } else IjtoDataset.require(statuses.get(0).equals("PASS"), "기존 자료 검증 결과 충돌");
        }
        return id;
    }

    private int totalRows() {
        int total = 0;
        for (String table : List.of("IJTO_DATA_SNAPSHOT", "REGION", "USERS", "BUSINESS", "BUSINESS_PERMISSION",
                "DATA_SOURCE", "DATA_VALIDATION_RESULT", "REGION_MONTHLY_STAT", "REGION_CONSUMPTION_STAT")) {
            total += jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        }
        return total;
    }
}
