package com.red.jejucalendar.datasource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.common.ApiException;
import com.red.jejucalendar.common.Inputs;
import com.red.jejucalendar.dao.DataSourceFileDao;
import com.red.jejucalendar.dao.DataValidationResultDao;
import com.red.jejucalendar.dao.RegionDao;
import com.red.jejucalendar.dao.RegionMonthlyStatDao;
import com.red.jejucalendar.datasource.DataSourceDtos.CheckResult;
import com.red.jejucalendar.datasource.DataSourceDtos.RegisterResponse;
import com.red.jejucalendar.datasource.DataSourceDtos.SourceItem;
import com.red.jejucalendar.datasource.DataSourceDtos.SourceListResponse;
import com.red.jejucalendar.datasource.DataSourceDtos.SourceMeta;
import com.red.jejucalendar.datasource.DataSourceDtos.ValidationResponse;
import com.red.jejucalendar.domain.DataSourceFile;
import com.red.jejucalendar.domain.DataValidationResult;
import com.red.jejucalendar.domain.Region;
import com.red.jejucalendar.domain.RegionMonthlyStat;
import com.red.jejucalendar.suggestion.SuggestionService;

/**
 * UC07 CSV 자료 등록, UC08 자료 기준·누락값 검증 (SCR-A-001, SCR-A-001-P01)
 * 등록할 때 받은 파일과 입력값으로 다섯 검증을 다시 실행함
 * 그래서 검증 뒤 파일이나 기준을 바꿔도 검증을 건너뛰고 등록할 수 없음
 */
@Service
public class DataSourceService {

    public static final long MAX_FILE_BYTES = 1024 * 1024;
    private static final String PASS = "PASS";

    private final DataSourceFileDao sourceDao;
    private final DataValidationResultDao validationDao;
    private final RegionMonthlyStatDao monthlyStatDao;
    private final RegionDao regionDao;
    private final SuggestionService suggestionService;

    public DataSourceService(DataSourceFileDao sourceDao, DataValidationResultDao validationDao,
            RegionMonthlyStatDao monthlyStatDao, RegionDao regionDao, SuggestionService suggestionService) {
        this.sourceDao = sourceDao;
        this.validationDao = validationDao;
        this.monthlyStatDao = monthlyStatDao;
        this.regionDao = regionDao;
        this.suggestionService = suggestionService;
    }

    /** SCR-A-001 ⑨: 저장하지 않고 다섯 항목만 검사 */
    public ValidationResponse validate(LoginUser user, MultipartFile file, SourceMeta meta) {
        user.requireServiceOperator();
        return toResponse(run(file, meta));
    }

    /** SCR-A-001 ⑩, P01 ③: 다시 검증한 뒤 모두 통과하면 자료·검증 결과·월별 통계를 한 번에 저장 (방문 자료면 제안도 다시 계산) */
    @Transactional
    public RegisterResponse register(LoginUser user, MultipartFile file, SourceMeta meta) {
        user.requireServiceOperator();
        Checked checked = run(file, meta);
        ValidationResponse validation = toResponse(checked);
        if (!validation.registrable()) {
            throw ApiException.badRequest(null, "검증을 통과하지 못해 등록할 수 없습니다. 오류를 수정한 뒤 다시 검증해 주세요.");
        }
        YearMonth from = VisitCsvValidator.month(meta.startMonth());
        YearMonth to = VisitCsvValidator.month(meta.endMonth());
        LocalDate today = LocalDate.now();
        List<VisitCsvValidator.Row> rows = checked.result().rows();

        long sourceId = sourceDao.insert(new DataSourceFile(null, user.userId(), checked.fileName(),
                meta.sourceVersion().strip(), from.atDay(1), to.atDay(1), meta.unit(), PASS,
                meta.providerBasis().strip(), rows.size(), today));
        for (CheckResult check : checked.result().checks()) {
            validationDao.insert(new DataValidationResult(sourceId, check.item(), PASS, null, today));
        }
        String metricType = "PERSON_10K".equals(meta.unit()) ? "VISIT" : "SPEND";
        int saved = monthlyStatDao.insertAll(rows.stream()
                .map(r -> new RegionMonthlyStat(null, sourceId, r.regionCode(), r.month().atDay(1), metricType, r.value()))
                .toList());
        if ("VISIT".equals(metricType)) {
            suggestionService.recalculate();
        }
        return new RegisterResponse(sourceId, saved, "자료를 등록했습니다. (" + saved + "행)", validation);
    }

    /** SCR-A-001 ⑬: 등록한 자료 수와 목록 */
    public SourceListResponse list(LoginUser user) {
        user.requireServiceOperator();
        List<SourceItem> items = sourceDao.findAll().stream()
                .map(s -> new SourceItem(s.sourceId(), s.fileName(), s.sourceVersion(), s.startMonth(), s.endMonth(),
                        s.unit(), s.validationStatus(), s.providerBasis(), s.rowCount(), s.registeredAt()))
                .toList();
        return new SourceListResponse(items.size(), items);
    }

    /** SCR-A-001 ①: CSV 양식 (엑셀에서 한글이 깨지지 않도록 BOM을 붙임) */
    public byte[] template(LoginUser user) {
        user.requireServiceOperator();
        String csv = "﻿" + String.join(",", VisitCsvValidator.HEADER) + "\r\n"
                + "50110253,2025-01,35,만 명,제공자·집계 기준과 같은 값,월별 지역 집계\r\n";
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    private record Checked(String fileName, VisitCsvValidator.Result result) {
    }

    private Checked run(MultipartFile file, SourceMeta meta) {
        String fileName = checkFile(file);
        Inputs.maxBytes("sourceVersion", "자료 출처·버전", Inputs.trimToNull(meta.sourceVersion()), 120);
        Inputs.maxBytes("providerBasis", "제공자·집계 기준", Inputs.trimToNull(meta.providerBasis()), 80);
        Set<String> regionCodes = regionDao.findAll().stream().map(Region::regionCode).collect(Collectors.toSet());
        return new Checked(fileName, VisitCsvValidator.validate(readUtf8(file), meta, regionCodes));
    }

    /** SCR-A-001 ②: 확장자·크기 검사 */
    private static String checkFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("file", "먼저 CSV 파일을 선택해 주세요.");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().strip();
        if (!name.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw ApiException.badRequest("file", "CSV 파일(.csv)만 등록할 수 있어요.");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw ApiException.badRequest("file", "파일이 너무 큽니다. 최대 1MB까지 등록할 수 있어요.");
        }
        Inputs.maxBytes("file", "파일 이름", name, 100);
        return name;
    }

    private static String readUtf8(MultipartFile file) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(file.getBytes()))
                    .toString();
        } catch (CharacterCodingException e) {
            throw ApiException.badRequest("file", "CSV를 UTF-8로 저장한 뒤 다시 선택해 주세요. (엑셀: CSV UTF-8 형식)");
        } catch (IOException e) {
            throw ApiException.badRequest("file", "파일을 읽지 못했습니다. 다시 선택해 주세요.");
        }
    }

    private static ValidationResponse toResponse(Checked checked) {
        VisitCsvValidator.Result r = checked.result();
        boolean ok = r.passed();
        String message = r.parseError() != null
                ? r.parseError()
                : checked.fileName() + " · " + r.parsedRowCount() + "행 · "
                        + (ok ? "다섯 검증을 통과했습니다. 자료를 등록할 수 있습니다."
                                : "등록할 수 없습니다. 오류를 수정한 뒤 다시 검증해 주세요.");
        return new ValidationResponse(checked.fileName(), r.parsedRowCount(), ok, message, r.checks());
    }
}
