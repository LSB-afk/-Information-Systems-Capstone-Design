package com.red.jejucalendar.support;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

/**
 * 검토를 마친 SQL 파일 하나를 팀 계정에 실행하는 도구
 * db.script 값을 주지 않으면 이 클래스는 동작하지 않음 (README의 실행 명령 참고)
 */
@Component
@ConditionalOnProperty(name = "db.script")
public class DbScriptRunner implements ApplicationRunner {

    private final DataSource dataSource;

    public DbScriptRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String path = args.getOptionValues("db.script").get(0);
        try (Connection con = dataSource.getConnection()) {
            System.out.println("[DB] 접속 계정: " + con.getMetaData().getUserName() + ", 실행 파일: " + path);
            ScriptUtils.executeSqlScript(con,
                    new EncodedResource(new FileSystemResource(path), StandardCharsets.UTF_8));
            System.out.println("[DB] 실행 완료: " + path);
        }
    }
}
