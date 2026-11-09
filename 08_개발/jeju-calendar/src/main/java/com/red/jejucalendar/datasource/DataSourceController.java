package com.red.jejucalendar.datasource;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.red.jejucalendar.auth.CurrentUser;
import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.datasource.DataSourceDtos.RegisterResponse;
import com.red.jejucalendar.datasource.DataSourceDtos.SourceListResponse;
import com.red.jejucalendar.datasource.DataSourceDtos.SourceMeta;
import com.red.jejucalendar.datasource.DataSourceDtos.ValidationResponse;

/** SCR-A-001 자료·기준 관리의 CSV 부분, SCR-A-001-P01 CSV 검증 결과 (서비스 운영자 전용) */
@RestController
@RequestMapping("/api/admin/data-sources")
public class DataSourceController {

    private final DataSourceService service;

    public DataSourceController(DataSourceService service) {
        this.service = service;
    }

    @GetMapping
    public SourceListResponse list(@CurrentUser LoginUser user) {
        return service.list(user);
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> template(@CurrentUser LoginUser user) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("지역_월별_집계_양식.csv", StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(service.template(user));
    }

    /** multipart/form-data: file + sourceVersion, startMonth, endMonth, unit, providerBasis */
    @PostMapping("/validate")
    public ValidationResponse validate(@CurrentUser LoginUser user, @RequestParam(required = false) MultipartFile file,
            SourceMeta meta) {
        return service.validate(user, file, meta);
    }

    @PostMapping
    public ResponseEntity<RegisterResponse> register(@CurrentUser LoginUser user,
            @RequestParam(required = false) MultipartFile file, SourceMeta meta) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(user, file, meta));
    }
}
