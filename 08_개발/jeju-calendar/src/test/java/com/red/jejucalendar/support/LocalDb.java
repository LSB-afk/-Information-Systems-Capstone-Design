package com.red.jejucalendar.support;

import java.nio.file.Files;
import java.nio.file.Path;

/** 로컬 DB 접속 정보가 있을 때만 DB 테스트를 실행하기 위한 조건 */
public final class LocalDb {

    private LocalDb() {
    }

    public static boolean configured() {
        return Files.exists(Path.of("src/main/resources/application-local.properties"))
                || System.getenv("DB_PASSWORD") != null;
    }
}
