package com.red.jejucalendar.support.ijto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@EnabledIf("com.red.jejucalendar.support.LocalDb#configured")
class IjtoImporterTest {
    @Autowired IjtoImporter importer;
    @Autowired JdbcTemplate jdbc;

    @Test
    void importsExactValuesAndSecondRunAddsNothing() throws Exception {
        var data = IjtoDataset.load(Path.of("../../02_제품/데이터/ijto_20261027"));
        var first = importer.importDataset(data);
        assertThat(first.snapshots()).isEqualTo(3);
        assertThat(first.regions()).isEqualTo(43);
        assertThat(first.observations()).isEqualTo(60);
        assertThat(first.shares()).isEqualTo(88);
        BigDecimal value = jdbc.queryForObject("SELECT S.STAT_VALUE FROM REGION_MONTHLY_STAT S JOIN DATA_SOURCE D"
                + " ON D.SOURCE_ID=S.SOURCE_ID WHERE D.SOURCE_VERSION=? AND S.REGION_CODE='5011025600'"
                + " AND S.STAT_MONTH=DATE '2025-01-01' AND S.METRIC_TYPE='VISIT'", BigDecimal.class,
                "ijto-20261027@68246a41c765-VISIT");
        assertThat(value).isEqualByComparingTo("49.3209");
        var again = importer.importDataset(data);
        assertThat(again.insertedRows()).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM IJTO_DATA_SNAPSHOT", Integer.class)).isGreaterThanOrEqualTo(3);
    }

    @Test
    void sourceRegistrationUsesTheSameDatePrecisionAsCsvUploads() throws Exception {
        var original = IjtoDataset.load(Path.of("../../02_제품/데이터/ijto_20261027"));
        var data = new IjtoDataset(original.repository(), original.commit(), original.snapshotDate().minusDays(1),
                original.snapshots(), original.areas(), original.observations(), original.shares());
        importer.importDataset(data);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM DATA_SOURCE WHERE SOURCE_VERSION LIKE 'ijto-20261026@%'"
                + " AND REGISTERED_AT = TRUNC(REGISTERED_AT)", Integer.class)).isEqualTo(2);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void failedImportRollsBackEarlierInserts() throws Exception {
        int before = jdbc.queryForObject("SELECT COUNT(*) FROM IJTO_DATA_SNAPSHOT", Integer.class);
        var data = IjtoDataset.load(Path.of("../../02_제품/데이터/ijto_20261027"));
        var areas = new ArrayList<>(data.areas());
        // 새 스냅샷을 먼저 넣은 뒤 지역 이름 충돌에서 실패하도록 구성함
        areas.add(new IjtoDataset.Area("5011025600", "같은 코드의 다른 지역"));
        var snapshots = data.snapshots().stream().map(s -> new IjtoDataset.Snapshot(
                "rollback-" + s.file(), s.sourcePath(), s.sha256(), s.payload())).toList();
        var invalid = new IjtoDataset(data.repository(), data.commit(), data.snapshotDate(), snapshots,
                areas, data.observations(), data.shares());
        assertThatThrownBy(() -> importer.importDataset(invalid)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지역");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM IJTO_DATA_SNAPSHOT", Integer.class)).isEqualTo(before);
    }

    @Test
    void refusesChangedSourceMetadataInsteadOfReusingIt() throws Exception {
        var data = IjtoDataset.load(Path.of("../../02_제품/데이터/ijto_20261027"));
        importer.importDataset(data);
        jdbc.update("UPDATE DATA_SOURCE SET PROVIDER_BASIS='changed' WHERE SOURCE_VERSION=?", "ijto-20261027@68246a41c765-VISIT");
        assertThatThrownBy(() -> importer.importDataset(data)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("메타데이터");
    }

    @Test
    void refusesChangedSnapshotProvenance() throws Exception {
        var data = IjtoDataset.load(Path.of("../../02_제품/데이터/ijto_20261027"));
        importer.importDataset(data);
        jdbc.update("UPDATE IJTO_DATA_SNAPSHOT SET REPOSITORY='changed' WHERE SNAPSHOT_KEY=?",
                "2026-10-27:recent-data.json");
        assertThatThrownBy(() -> importer.importDataset(data)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("출처");
    }
}
