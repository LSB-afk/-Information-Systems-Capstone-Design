package com.red.jejucalendar.support.ijto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IjtoDatasetTest {
    static final Path DATA = Path.of("../../02_제품/데이터/ijto_20261027");

    @Test
    void extractsExactObservedValuesAndPreservesWholeSnapshots() throws Exception {
        var data = IjtoDataset.load(DATA);
        assertThat(data.snapshots()).hasSize(3);
        assertThat(data.areas()).hasSize(43);
        assertThat(data.observations()).hasSize(60);
        assertThat(data.shares()).hasSize(88);
        assertThat(data.observations()).filteredOn(o -> o.region().equals("5011025600")
                        && o.month().equals(LocalDate.of(2025, 1, 1)) && o.metric().equals("VISIT"))
                .singleElement().satisfies(o -> assertThat(o.value()).isEqualByComparingTo("49.3209"));
        assertThat(data.observations()).filteredOn(o -> o.month().equals(LocalDate.of(2025, 1, 1))
                        && o.metric().equals("SPEND"))
                .singleElement().satisfies(o -> assertThat(o.value()).isEqualByComparingTo("45.46311310"));
        assertThat(data.observations()).filteredOn(o -> o.month().equals(LocalDate.of(2023, 1, 1))
                        && o.metric().equals("SPEND"))
                .singleElement().satisfies(o -> assertThat(o.value()).isEqualByComparingTo("37.61568382"));
    }

    @Test
    void keepsMissingMonthsAndMultiYearProfilesOutOfObservedRows() throws Exception {
        var data = IjtoDataset.load(DATA);
        assertThat(data.observations()).noneMatch(o -> o.month().isAfter(LocalDate.of(2026, 8, 1)));
        assertThat(data.observations()).filteredOn(o -> o.metric().equals("VISIT")).hasSize(16);
        assertThat(data.shares()).filteredOn(s -> s.month().equals(LocalDate.of(2025, 1, 1))
                        && s.segment().equals("FOREIGN"))
                .singleElement().satisfies(s -> assertThat(s.rate()).isEqualByComparingTo("3.57"));
        assertThat(data.shares()).filteredOn(s -> s.month().equals(LocalDate.of(2025, 1, 1))
                        && s.segment().equals("DOMESTIC"))
                .singleElement().satisfies(s -> assertThat(s.rate()).isEqualByComparingTo("96.43"));
    }

    @Test
    void refusesChangedInputBeforeAnyDatabaseWrite(@TempDir Path temp) throws Exception {
        Files.copy(DATA.resolve("manifest.json"), temp.resolve("manifest.json"));
        for (String file : new String[]{"ijto-demo-20261027.json", "case-data.json", "recent-data.json"}) {
            Files.copy(DATA.resolve(file), temp.resolve(file));
        }
        Files.writeString(temp.resolve("recent-data.json"), "{}");
        assertThatThrownBy(() -> IjtoDataset.load(temp)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SHA-256");
    }
}
