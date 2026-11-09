# jeju-calendar (Spring Boot 서버)

제주 비수기 상권 마케팅 캘린더의 서버 프로젝트임 수업 일정 7주차(개발환경, DAO)부터 시작함

## 기술 스택

| 구분 | 내용 | 이유 |
| --- | --- | --- |
| Java | 17 | Spring Boot 4.x 최소 버전 |
| Spring Boot | 4.1.2 (Web MVC, JDBC, Thymeleaf) | 수업의 DAO·Service·Controller·View 구분을 그대로 따름 |
| DB 접근 | Spring JDBC `JdbcTemplate` 기반 DAO | SQL을 직접 보며 ERD와 대조하기 쉬움 |
| DBMS | Oracle Database 11g XE (팀 배정 계정, SID `xe`) | 수업 제공 서버 |
| JDBC 드라이버 | `ojdbc8` 19.27.0.0 | 19c 드라이버는 11.2 서버를 지원함 Spring Boot 기본 `ojdbc11` 23.x는 11g를 지원하지 않음 |
| 빌드 | Maven Wrapper (`mvnw`) | Maven을 따로 설치하지 않아도 됨 |

## DB 접속 정보 설정 (GitHub에 올리지 않음)

1. `src/main/resources/application-local.properties.example`을 같은 폴더에 `application-local.properties`로 복사함
2. 서버 주소·팀 계정·비밀번호를 채움
3. `application-local.properties`, `.env`는 `.gitignore`에 들어 있음 커밋 전에 `git status`에 나오지 않는지 확인함

환경변수 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`로 넣어도 됨

## 테이블·시퀀스 만들기

`db/` 폴더의 SQL을 읽고 검토한 뒤 순서대로 실행함 다른 팀 객체나 기존 데이터는 지우지 않음

| 파일 | 내용 |
| --- | --- |
| `db/00_check_objects.sql` | 현재 계정의 테이블·시퀀스·제약조건 확인 (조회만 함) |
| `db/01_create_tables.sql` | ERD SQL([Red_ERD_Oracle.sql](../../04_아키텍처/ERD/Red_ERD_Oracle.sql))의 1행부터 422행까지 그대로 옮김 테이블 13개, PK 13개, AK 4개, FK 17개 |
| `db/02_create_sequences.sql` | `SEQ_` 시퀀스 9개 |

- 테이블 이름은 ERD에 정의된 이름(`USERS`, `BUSINESS` 등)을 그대로 씀 `TBL_` 접두어로 바꿀지는 팀 확인 후 ERD와 함께 바꿈
- ERwin 기본 옵션으로 만들어진 트리거 30개는 FK 17개와 같은 규칙을 다시 검사하고, `tD_PROMO_PLAN`처럼 `ON DELETE CASCADE`와 겹치는 삭제도 있어 실행하지 않음
- 시퀀스는 숫자 하나로 된 PK를 가진 테이블에만 만듦 `REGION`(지역 코드 PK)과 복합 PK 테이블 3개는 제외함

SQL 클라이언트가 없으면 아래 명령으로 파일 하나씩 실행할 수 있음 (Windows PowerShell 기준)

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--db.script=db/01_create_tables.sql --spring.main.web-application-type=none"
```

## DAO 목록 (`src/main/java/com/red/jejucalendar/dao`)

테이블 13개마다 DAO 하나와 `domain` 패키지의 record 하나가 있음 시퀀스를 쓰는 테이블의 `insert`는 발급된 ID를 돌려줌

| DAO | 테이블 | 저장·수정·삭제 외에 있는 조회 |
| --- | --- | --- |
| `UserDao` | USERS | 이메일로 조회 |
| `RegionDao` | REGION | 전체 지역 |
| `BusinessDao` | BUSINESS | 등록한 사용자별, 권한이 있는 사업체 |
| `BusinessPermissionDao` | BUSINESS_PERMISSION | 사용자별, 사업체별 |
| `DataSourceFileDao` | DATA_SOURCE | 최근 등록순, 검증 상태별 |
| `DataValidationResultDao` | DATA_VALIDATION_RESULT | 자료별 검증 항목 |
| `RegionMonthlyStatDao` | REGION_MONTHLY_STAT | 자료·지역·지표·기간별, 일괄 저장 |
| `RegionConsumptionStatDao` | REGION_CONSUMPTION_STAT | 자료·지역·월별, 일괄 저장 |
| `RecommendationCriteriaDao` | RECOMMENDATION_CRITERIA | 전체 기준 |
| `PromoSuggestionDao` | PROMO_SUGGESTION | 지역·대상 월별, 자료별 |
| `PromoSuggestionItemDao` | PROMO_SUGGESTION_ITEM | 제안별 항목 |
| `PromoPlanDao` | PROMO_PLAN | 사업체별, 사업체·기간별(캘린더) |
| `ExecutionRecordDao` | EXECUTION_RECORD | 계획별, 사업체별 |

- 월 단위 컬럼(START_MONTH, END_MONTH, TARGET_MONTH, STAT_MONTH)은 매월 1일 날짜로 넣음
- `DATA_SOURCE`의 record 이름은 `javax.sql.DataSource`와 헷갈리지 않도록 `DataSourceFile`로 함
- 삭제 규칙은 DB의 FK를 따름 계획을 지우면 실행 결과 기록도 지워지고(CASCADE), 제안을 지우면 계획의 제안 번호가 비워짐(SET NULL) 제안 항목이 남은 제안, 사업체가 남은 지역처럼 자식 행이 있는 부모는 지울 수 없음

## Service·Controller (8주차)

화면설계서의 화면 순서 중 로그인과 계획·기록 화면을 먼저 만듦 실제 CSV 없이도 동작하는 화면이기 때문임 응답은 JSON이며 9주차 View에서 이 API를 사용함

| 화면 | 요청 | 내용 |
| --- | --- | --- |
| SCR-C-001 로그인 | `POST /api/auth/login` | 역할·이메일·비밀번호를 함께 확인하고 세션에 로그인 사용자를 저장함 |
| SCR-C-000 로그아웃 | `POST /api/auth/logout` | 세션 해제 |
| SCR-C-000 계정 영역 | `GET /api/auth/me` | 현재 로그인 사용자 |
| 공통 | `GET /api/businesses` | 계획·기록을 처리할 수 있는 사업체 목록 |
| SCR-B-001 일정표 | `GET /api/businesses/{id}/calendar` | 3개월의 월별 계획 카드 |
| SCR-B-001-P01 계획 작성 | `POST /api/businesses/{id}/plans` | 직접 추가, 월별 추가, 개별 제안 담기 (`suggestionId`) |
| SCR-B-001 자동 채우기 | `POST /api/businesses/{id}/plans/from-suggestions` | 아직 담지 않은 제안만 저장, 실패 시 전체를 되돌림 |
| SCR-B-001-P01 계획 수정·삭제 | `GET`·`PUT`·`DELETE /api/plans/{planId}` | 삭제하면 연결된 실행 기록도 지워짐 |
| SCR-B-002 실행 결과 조회 | `GET /api/businesses/{id}/records` | 계획 수, 기록 수, 미기록 수와 목록 |
| SCR-B-002-P01 기록 입력·수정 | `GET`·`PUT /api/plans/{planId}/record` | 최초 작성은 계획 값으로 자동 채움, 저장하면 계획 하나에 기록 하나로 저장·수정 |
| SCR-B-002-P02 사용 결과 요약 | `GET /api/businesses/{id}/records/summary` | 입력한 값만 더하고 입력 개수를 함께 보여줌 |

- 오류는 `{ "message": 화면에 보여줄 문구, "field": 문제가 된 입력란 }`으로 돌려줌 (400 입력 오류, 401 로그인 필요·인증 실패, 403 권한 없음, 404 없음, 409 이미 담은 제안)
- 권한: 사업체 운영자는 본인 사업체와 권한을 받은 사업체, 서비스 운영자는 `BUSINESS_PERMISSION`으로 권한을 받은 사업체만 처리함
- 일정표 기간은 `application.properties`의 `calendar.start-month`로 정함 (기본값 2026-10, 즉 2026년 10월부터 12월까지)
- 비밀번호는 BCrypt 해시로만 저장하고 응답에 넣지 않음
- 숫자 칸의 빈값은 미확보, 0은 실제로 확인한 0건으로 구분함 요약에서 미확보 값을 0으로 바꾸지 않음

### 시연용 샘플 데이터 (선택)

`db/03_sample_data.sql`은 화면 시안과 같은 시연 계정 2개(`cafe@example.com`, `operator@example.com`, 비밀번호 `JejuDemo26!`), 지역 2곳, 시연 카페 1곳, 초기 계획 2개를 넣음 팀이 실행하기로 정한 경우에만 위의 실행 명령으로 한 번 실행함

### 확인이 필요한 점: 한글 입력 길이

팀 DB는 `AL32UTF8`이고 길이 기준이 `BYTE`라서 `VARCHAR(100)`에 한글이 33자까지만 들어감 (한글 1자 = 3바이트) 화면 시안은 계획 이름을 70자까지 받으므로 34자부터는 저장할 수 없음 지금은 서버가 저장 전에 검사해 "한글 기준 33자까지" 안내를 보여줌 글자 수 기준으로 바꾸려면 ERD에서 `VARCHAR2(100 CHAR)`처럼 바꾸는 방안을 팀에서 정해야 함

## 접속·CRUD 테스트

```powershell
.\mvnw.cmd test
```

- `DbConnectionTest`: 접속 계정, Oracle 버전, 현재 테이블·시퀀스 목록과 테이블별 행 수를 출력함 (조회만 함)
- `UserDaoCrudTest`: `USERS` 테이블에 저장, 조회, 수정, 삭제를 확인함
- `AllDaoCrudTest`: 나머지 12개 DAO의 저장, 조회, 수정, 삭제와 NULL 값, AK 중복 차단, FK 규칙(CASCADE, SET NULL, 삭제 차단)을 확인함
- `PlanRecordApiTest`: 로그인·권한·일정표·계획·제안 담기·실행 기록·요약 API를 실제 요청처럼 확인함
- `SampleDataScriptTest`: 샘플 데이터 SQL이 오류 없이 실행되고 시연 계정으로 로그인되는지 확인함 (이미 넣은 DB에서는 건너뜀)
- DB를 쓰는 테스트는 끝나면 되돌리므로 데이터가 남지 않음 시퀀스 번호만 올라감
- 로컬 접속 정보가 없으면 두 테스트는 건너뜀
