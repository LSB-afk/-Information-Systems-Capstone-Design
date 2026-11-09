package com.red.jejucalendar.common;

import java.util.Set;

/** ERD 코드 설계서의 코드값 (04_아키텍처/ERD/ERD_코드_설계서.md) */
public final class Codes {

    private Codes() {
    }

    /** 1. 역할 구분 */
    public static final String ROLE_BUSINESS = "BUSINESS";
    public static final String ROLE_SERVICE = "SERVICE";
    public static final Set<String> ROLES = Set.of(ROLE_BUSINESS, ROLE_SERVICE);

    /** 2. 실행 상태 */
    public static final String EXEC_DONE = "DONE";
    public static final Set<String> EXEC_STATUSES = Set.of(EXEC_DONE, "NOT_DONE", "CHECKING");

    /** 3. 추천 채택 여부 */
    public static final String ADOPTION_YES = "YES";
    public static final String ADOPTION_UNKNOWN = "UNKNOWN";
    public static final Set<String> ADOPTION_STATUSES = Set.of(ADOPTION_YES, "NO", ADOPTION_UNKNOWN);

    /** 4. 홍보 채널 */
    public static final String CHANNEL_INSTAGRAM = "INSTAGRAM";
    public static final Set<String> CHANNELS = Set.of(CHANNEL_INSTAGRAM, "STORE", "BLOG", "ETC");
}
