package com.red.jejucalendar.support.ijto;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/** 출처 JSON에서 실제 관측 총량만 추출함 다년 평균과 원본은 스냅샷으로 보존함 */
public record IjtoDataset(String repository, String commit, LocalDate snapshotDate,
        List<Snapshot> snapshots, List<Area> areas, List<Observation> observations, List<Share> shares) {
    public record Snapshot(String file, String sourcePath, String sha256, String payload) {}
    public record Area(String code, String name) {}
    public record Observation(String region, LocalDate month, String metric, BigDecimal value) {}
    public record Share(String region, LocalDate month, String segment, BigDecimal rate) {}

    public static IjtoDataset load(Path directory) throws IOException {
        JsonMapper mapper = JsonMapper.builder().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build();
        JsonNode manifest = mapper.readTree(Files.readString(directory.resolve("manifest.json")));
        String repository = manifest.path("repository").asString();
        String commit = manifest.path("commit").asString();
        require("nenongkim/jeju_marketing".equals(repository) && commit.matches("[a-f0-9]{40}"), "출처 저장소·커밋 오류");
        LocalDate date = LocalDate.parse(manifest.path("snapshotDate").asString());
        List<Snapshot> snapshots = new ArrayList<>();
        Map<String, JsonNode> trees = new LinkedHashMap<>();
        Path root = directory.toAbsolutePath().normalize();
        for (JsonNode file : manifest.path("files")) {
            String name = file.path("file").asString();
            Path path = root.resolve(name).normalize();
            require(path.getParent().equals(root), "데이터 경로 오류");
            byte[] bytes = Files.readAllBytes(path);
            String hash = sha256(bytes);
            require(hash.equals(file.path("sha256").asString()), "SHA-256 불일치: " + name);
            String payload = new String(bytes, StandardCharsets.UTF_8);
            JsonNode tree = mapper.readTree(payload);
            require("1.0.0".equals(tree.path("schema_version").asString()), "스키마 버전 오류: " + name);
            require(trees.putIfAbsent(name, tree) == null, "중복 파일: " + name);
            snapshots.add(new Snapshot(name, file.path("sourcePath").asString(), hash, payload));
        }
        require(trees.keySet().equals(Set.of("ijto-demo-20261027.json", "case-data.json", "recent-data.json")), "필수 JSON 3개 확인 필요");
        JsonNode bundle = trees.get("ijto-demo-20261027.json");
        JsonNode cases = trees.get("case-data.json");
        JsonNode recent = trees.get("recent-data.json");
        require(date.toString().equals(bundle.path("snapshot_date").asString())
                && date.toString().equals(cases.path("snapshot").asString())
                && date.toString().equals(recent.path("snapshot").asString()), "수집일 불일치");
        List<Area> areas = new ArrayList<>();
        Map<String, String> names = new LinkedHashMap<>();
        for (JsonNode area : bundle.path("regions")) {
            String name = area.path("name").asString();
            String code = area.path("codes").path("visit_code").asString();
            require(code.matches("[0-9]{10}") && !name.isBlank() && !names.containsValue(code), "지역 코드 오류");
            require(names.putIfAbsent(name, code) == null, "중복 지역 이름");
            areas.add(new Area(code, name));
        }
        String region = names.get("구좌읍");
        require(region != null && "구좌읍".equals(cases.path("region").asString())
                && "구좌읍".equals(recent.path("region").asString())
                && "음식점업 전체".equals(cases.path("industry").asString()), "지역·업종 불일치");

        Map<String, Observation> observations = new LinkedHashMap<>();
        Map<LocalDate, Map<String, BigDecimal>> spending = new LinkedHashMap<>();
        for (JsonNode month : cases.path("months")) {
            int m = month.path("month").intValue();
            require(month.path("segments").size() == 3, "고객군 누락");
            for (JsonNode segment : month.path("segments")) {
                String customer = segment.path("customer").asString();
                require(segment.path("annual").size() == 3, "관측 연도 누락");
                for (JsonNode annual : segment.path("annual")) {
                    int year = annual.path("year").intValue();
                    require(year >= 2023 && year <= 2025, "관측 연도 오류");
                    addCustomer(spending, LocalDate.of(year, m, 1), customer, integer(annual.path("소비금액(원)")));
                }
            }
        }
        for (JsonNode month : recent.path("monthly")) {
            int m = month.path("month").intValue();
            require(m >= 1 && m <= 8 && month.path("months").size() == 1
                    && month.path("months").get(0).intValue() == m, "동기간 월 오류");
            for (int year : new int[]{2025, 2026}) {
                LocalDate when = LocalDate.of(year, m, 1);
                String key = "value_" + year;
                addObservation(observations, new Observation(region, when, "VISIT", integer(month.path("visits").path(key)).movePointLeft(4)));
                BigDecimal total = integer(month.path("spend").path(key));
                BigDecimal sum = BigDecimal.ZERO;
                require(month.path("segments").size() == 3, "동기간 고객군 누락");
                for (JsonNode segment : month.path("segments")) {
                    BigDecimal value = integer(segment.path("spend").path(key));
                    addCustomer(spending, when, segment.path("customer").asString(), value);
                    sum = sum.add(value);
                }
                require(total.compareTo(sum) == 0, "소비 총계·고객군 합계 불일치");
            }
        }
        List<Share> shares = new ArrayList<>();
        for (var entry : spending.entrySet()) {
            var customers = entry.getValue();
            require(customers.keySet().equals(Set.of("내국인 관광객", "외국인 관광객", "도민")), "고객군 정의 불일치");
            BigDecimal total = customers.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            addObservation(observations, new Observation(region, entry.getKey(), "SPEND", total.movePointLeft(8)));
            if (total.signum() > 0) {
                BigDecimal foreign = customers.get("외국인 관광객").multiply(BigDecimal.valueOf(100))
                        .divide(total, 2, RoundingMode.HALF_UP);
                shares.add(new Share(region, entry.getKey(), "FOREIGN", foreign));
                shares.add(new Share(region, entry.getKey(), "DOMESTIC", new BigDecimal("100.00").subtract(foreign)));
            }
        }
        return new IjtoDataset(repository, commit, date, List.copyOf(snapshots), List.copyOf(areas),
                List.copyOf(observations.values()), List.copyOf(shares));
    }

    private static void addCustomer(Map<LocalDate, Map<String, BigDecimal>> spending, LocalDate month,
            String customer, BigDecimal value) {
        var byCustomer = spending.computeIfAbsent(month, k -> new LinkedHashMap<>());
        BigDecimal previous = byCustomer.putIfAbsent(customer, value);
        require(previous == null || previous.compareTo(value) == 0, "고객군 중복 자료 값 불일치");
    }

    private static void addObservation(Map<String, Observation> rows, Observation value) {
        String key = value.region() + ":" + value.month() + ":" + value.metric();
        require(rows.putIfAbsent(key, value) == null, "관측값 중복");
    }

    private static BigDecimal integer(JsonNode node) {
        require(node.isNumber(), "관측 총량은 누락 없는 숫자여야 함");
        BigDecimal value = node.decimalValue();
        require(value.signum() >= 0 && value.stripTrailingZeros().scale() <= 0, "관측 총량은 0 이상의 정수여야 함");
        return value;
    }

    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
