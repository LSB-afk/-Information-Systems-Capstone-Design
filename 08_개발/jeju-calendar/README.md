# jeju-calendar (Spring Boot 서버와 화면)

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
| `db/04_ijto_precision_and_snapshot.sql` | `STAT_VALUE`를 소수 저장이 가능한 `NUMBER`로 확장하고 원본 JSON 보존용 `IJTO_DATA_SNAPSHOT` 테이블을 추가함 최초 한 번 실행 |
| `db/05_normalize_ijto_registration_date.sql` | 최초 이관한 출처 2개의 등록일 정밀도를 기존 CSV 등록과 맞춤 같은 날에는 자료 번호로 최신 자료를 선택하며 반복 실행 가능 |

- 테이블 이름은 ERD에 정의된 이름(`USERS`, `BUSINESS` 등)을 그대로 씀 `TBL_` 접두어로 바꿀지는 팀 확인 후 ERD와 함께 바꿈
- ERwin 기본 옵션으로 만들어진 트리거 30개는 FK 17개와 같은 규칙을 다시 검사하고, `tD_PROMO_PLAN`처럼 `ON DELETE CASCADE`와 겹치는 삭제도 있어 실행하지 않음
- 시퀀스는 숫자 하나로 된 PK를 가진 테이블에만 만듦 `REGION`(지역 코드 PK)과 복합 PK 테이블 3개는 제외함
- 2026년 11월 11일 본인 배정 계정에 `db/04_ijto_precision_and_snapshot.sql`을 적용함 현재 테이블은 14개, 시퀀스는 9개임 원래 ERwin 생성 SQL은 보존하고 추가 변경은 별도 이관 SQL로 관리함

SQL 클라이언트가 없으면 아래 명령으로 파일 하나씩 실행할 수 있음 (Windows PowerShell 기준)

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--db.script=db/01_create_tables.sql --spring.main.web-application-type=none"
```

## DAO 목록 (`src/main/java/com/red/jejucalendar/dao`)

원래 ERD의 테이블 13개마다 DAO 하나와 `domain` 패키지의 record 하나가 있음 시퀀스를 쓰는 테이블의 `insert`는 발급된 ID를 돌려줌 추가한 `IJTO_DATA_SNAPSHOT`은 실제 데이터 이관기가 원문·출처를 저장하는 테이블임

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
- 월별 통계 값은 Java `BigDecimal`과 Oracle `NUMBER`로 읽고 저장함 방문은 만 명·소비는 억 원 단위를 유지하며 소수를 정수로 잘라 저장하지 않음
- `DATA_SOURCE`의 record 이름은 `javax.sql.DataSource`와 헷갈리지 않도록 `DataSourceFile`로 함
- 삭제 규칙은 DB의 FK를 따름 계획을 지우면 실행 결과 기록도 지워지고(CASCADE), 제안을 지우면 계획의 제안 번호가 비워짐(SET NULL) 제안 항목이 남은 제안, 사업체가 남은 지역처럼 자식 행이 있는 부모는 지울 수 없음

## Service·Controller (8주차)

화면설계서의 14개 Screen ID에 맞춘 API임 응답은 JSON이며 아래 '화면(View)'이 이 API를 사용함

| 화면 | 요청 | 내용 |
| --- | --- | --- |
| SCR-C-001 로그인 | `POST /api/auth/login` | 역할·이메일·비밀번호를 함께 확인하고 세션에 로그인 사용자를 저장함 |
| SCR-C-000 로그아웃 | `POST /api/auth/logout` | 세션 해제 |
| SCR-C-000 계정 영역 | `GET /api/auth/me` | 현재 로그인 사용자 |
| SCR-C-002 한눈에 보기 | `GET /api/overview?regionCode=&businessId=&metric=` | 지역 방문 흐름, 3개월 계획 요약, 홍보 제안 요약, 자료 준비 현황(UC12)을 한 번에 줌 |
| 공통 | `GET /api/regions` | 지역 선택 목록 |
| 공통 | `GET /api/businesses` | 계획·기록을 처리할 수 있는 사업체 목록 |
| SCR-B-001 일정표 | `GET /api/businesses/{id}/calendar` | 3개월의 월별 계획 카드 |
| SCR-B-001-P01 계획 작성 | `POST /api/businesses/{id}/plans` | 직접 추가, 월별 추가, 개별 제안 담기 (`suggestionId`) |
| SCR-B-001 자동 채우기 | `POST /api/businesses/{id}/plans/from-suggestions` | 아직 담지 않은 제안만 저장, 실패 시 전체를 되돌림 |
| SCR-B-001-P01 계획 수정·삭제 | `GET`·`PUT`·`DELETE /api/plans/{planId}` | 삭제하면 연결된 실행 기록도 지워짐 |
| SCR-B-002 실행 결과 조회 | `GET /api/businesses/{id}/records` | 계획 수, 기록 수, 미기록 수와 목록 |
| SCR-B-002-P01 기록 입력·수정 | `GET`·`PUT /api/plans/{planId}/record` | 최초 작성은 계획 값으로 자동 채움, 저장하면 계획 하나에 기록 하나로 저장·수정 |
| SCR-B-002-P02 사용 결과 요약 | `GET /api/businesses/{id}/records/summary` | 입력한 값만 더하고 입력 개수를 함께 보여줌 |

| SCR-C-003 방문·계절 비교 | `GET /api/analysis/trend?regionCode=&year=&metric=` | 12개월 값, 최다·최저 달, 지역별 비교 표, 자료 출처 (metric: `VISIT` 또는 `SPEND`) |
| SCR-C-003 소비 비교 | `GET /api/analysis/consumption?regionCode=&year=&category=` | 같은 달의 방문·소비, 업종별 내·외국인 비중, 시간대별 비중 |
| SCR-C-004 홍보 제안 | `GET /api/suggestions?regionCode=&businessId=` | 3개월 제안을 월별로 묶음, 카드마다 네 항목·판단 이유·출처·이미 담은 계획 번호 |
| SCR-C-004-P01 추천 근거 | `GET /api/suggestions/{id}` | 기준 연도의 월별 관측값, 확인된 조건·확인할 조건, 계획 행동 |
| SCR-C-004 담기 | 계획 API 사용 | 개별 담기는 `POST /api/businesses/{id}/plans`(`suggestionId`), 남은 제안 한 번에 담기는 `.../plans/from-suggestions` |
| 제안 다시 계산 | `POST /api/admin/suggestions/recalculate` | 서비스 운영자 전용 (기준 저장·방문 자료 등록 때는 자동으로 다시 계산함) |
| SCR-A-001 CSV 양식 | `GET /api/admin/data-sources/template` | 필수 열 6개와 예시 행 (UTF-8, 엑셀용 BOM 포함) |
| SCR-A-001 등록 자료 | `GET /api/admin/data-sources` | 등록한 자료 수와 목록 |
| SCR-A-001-P01 자료 검증 | `POST /api/admin/data-sources/validate` | 저장하지 않고 다섯 항목만 판정 |
| SCR-A-001 자료 등록 | `POST /api/admin/data-sources` | 받은 파일로 다시 검증한 뒤 모두 통과하면 자료·검증 결과 5개·월별 통계를 한 번에 저장 |
| SCR-A-001-P02 추천 기준 | `GET`·`PUT /api/admin/criteria` | 현재 기준 조회와 저장 (0부터 100까지의 감소 기준, 출처, 기준 기간, 판단 이유, 적용 조건) |

- 오류는 `{ "message": 화면에 보여줄 문구, "field": 문제가 된 입력란 }`으로 돌려줌 (400 입력 오류, 401 로그인 필요·인증 실패, 403 권한 없음, 404 없음, 409 이미 담은 제안)
- 권한: 사업체 운영자는 본인 사업체와 권한을 받은 사업체, 서비스 운영자는 `BUSINESS_PERMISSION`으로 권한을 받은 사업체만 처리함
- 일정표 기간은 `application.properties`의 `calendar.start-month`로 정함 (기본값 2026-10, 즉 2026년 10월부터 12월까지)
- 비밀번호는 BCrypt 해시로만 저장하고 응답에 넣지 않음
- 숫자 칸의 빈값은 미확보, 0은 실제로 확인한 0건으로 구분함 요약에서 미확보 값을 0으로 바꾸지 않음

### 지역 분석 규칙

- 지표·연도마다 검증을 통과한 자료 중 가장 최근에 등록한 자료 하나를 골라 모든 지역에 같은 기준으로 씀 응답의 `source`에 출처·버전·제공자·단위·기간을 함께 돌려줌
- 지역·연도를 비우면 내 사업체의 지역과 자료가 있는 가장 최근 연도를 씀
- 자료가 없으면 `hasData: false`와 조회할 수 있는 연도를 안내하고 그래프 값을 만들지 않음 없는 달은 `null`(미확보)이며 0으로 바꾸지 않음
- 지역 비교 표의 변화율은 계획 시작 월과 다음 달 기준 (기본 10월 → 11월), 계산식 (다음 달 − 시작 달) ÷ 시작 달 × 100, 소수 첫째 자리 반올림
- 소비 비교는 방문(만 명)과 소비(억 원)를 단위별로 나란히 보여주기만 하고 전환율·1인당 소비액을 계산하지 않음 내·외국인 중 한쪽 값이 없는 업종은 비교를 제한함
- 업종·내외국인·시간대별 비중은 계획 시작 월과 같은 달(기본 10월)의 `REGION_CONSUMPTION_STAT` 자료를 씀 이 자료를 등록하는 CSV는 아직 없음

### 홍보 제안 규칙

- 계획 대상 월(예: 2026년 11월)과 같은 달의 기준 연도 값(2025년 11월)을 직전 달(2025년 10월)과 비교해, 방문 감소율이 추천 기준(%) 이상이면 그 달의 제안을 만듦 (화면 시안과 같은 규칙)
- 기준 연도는 검증을 통과한 방문 자료 중 가장 최근 연도이고, 그 연도에서 가장 최근에 등록한 자료 하나를 모든 지역에 씀
- 추천 기준을 저장하거나 방문 자료를 등록하면 같은 처리 안에서 다시 계산함 지역·월마다 제안은 하나이며, 다시 계산해도 남는 제안은 같은 번호로 고쳐서 계획과의 연결을 유지함
- 기준을 넘지 않게 된 제안은 지움 그 제안으로 만든 계획은 남고 제안 연결만 비워짐 (FK ON DELETE SET NULL)
- 제안 문구는 화면 시안의 10월·11월·12월 예시를 쓰고, 다른 달은 기본 문구를 씀 판단 이유는 DB 길이(200바이트)에 맞춘 짧은 문장으로 저장하고, 추천 기준의 판단 이유·적용 조건은 응답에 함께 넣음
- 홍보 대상 항목은 같은 달 카페 업종의 내·외국인 소비 비중이 있을 때만 근거를 채움 주변 관광지는 인기 장소 자료를 등록하기 전까지 '미확보'로 표시함
- 일정 담기는 처리할 수 있는 사업체가 있고 그 사업체 지역의 제안일 때만 가능함 (`canAddToPlan`)

### CSV 등록 규칙 (자료·기준 관리)

- 자료·기준 관리 API는 서비스 운영자만 사용함 사업체 운영자는 403
- 파일: `.csv`, 최대 1MB, UTF-8 (엑셀은 "CSV UTF-8" 형식으로 저장) 열은 `region_code,month,value,unit,provider,aggregation` 순서
- multipart 폼 칸: `file`, `sourceVersion`(출처·버전), `startMonth`·`endMonth`(2025-01 형식), `unit`(`PERSON_10K` 만 명 또는 `KRW_100M` 억 원), `providerBasis`(제공자·집계 기준)

| 검증 항목 | 통과 조건 |
| --- | --- |
| PERIOD 자료 기간 | 시작 월이 종료 월보다 늦지 않고, 모든 행의 월(2025-01 형식)이 기준기간 안에 있음 |
| AGGREGATION 집계 기준 | 모든 행이 `월별 지역 집계`이고 선택한 단위와 같음, 같은 지역·월이 두 번 나오지 않음 |
| REGION_CODE 지역 코드 | 모든 지역 코드가 REGION 테이블에 있음 |
| MISSING_VALUE 누락값 | 출처·버전과 모든 칸이 채워져 있고, 값이 0 이상의 정수임 빈 값을 0으로 바꾸지 않음 |
| PROVIDER 제공 기준 일관성 | 모든 행의 provider가 입력한 제공자·집계 기준과 같음 |

- 등록하면 만 명 자료는 지표 `VISIT`, 억 원 자료는 `SPEND`로 `REGION_MONTHLY_STAT`에 저장되어 지역 분석에서 사용함
- DB와 조회 API는 소수를 지원하지만 이 CSV 등록 양식은 기존처럼 0 이상의 정수만 받음 실제 통계 JSON 이관은 별도 이관기로 처리하며 명·원 단위 정수 총량을 만 명·억 원으로 정확히 환산함
- 업종·내외국인·시간대별 소비 비중(`REGION_CONSUMPTION_STAT`) CSV는 실제 자료의 열 구성을 확인한 뒤 추가함

### 시연용 샘플 데이터 (선택)

`db/03_sample_data.sql`은 화면 시안과 같은 시연 계정 2개(`cafe@example.com`, `operator@example.com`, 비밀번호 `JejuDemo26!`), 지역 2곳, 시연 카페 1곳, 초기 계획 2개를 넣음 2026-11-10에 팀원의 배정 계정에 한 번 넣었음 본인 배정 계정의 2026-11-11 14:25 검증에서는 넣지 않았음 다시 실행하면 중복으로 실패하며 아래 실제 데이터 이관 절차와 구분함

### 실제 통계 JSON 이관

[데이터 묶음](../../02_제품/데이터/ijto_20261027/README.md)은 `nenongkim/jeju_marketing`의 커밋 `68246a41c76581ae930de576b347df2aab188aa0`에서 가져옴 원본 JSON 3개와 `manifest.json`을 같은 Obsidian 보관함에 저장하고 Git으로 관리함 원본 CSV와 설문 개별 응답은 포함하지 않음

| 구분 | 이관기가 저장하는 내용 |
| --- | --- |
| 원본 보존 | JSON 3개 전체를 `IJTO_DATA_SNAPSHOT.PAYLOAD`의 CLOB에 저장 출처 저장소·커밋·파일 경로·SHA-256·수집일을 함께 보존 |
| 지역 | 43개 읍면동의 원천 방문 코드 10자리를 문자열 그대로 `REGION`에 저장 |
| 실제 월별 관측값 | 구좌 방문 16행과 음식점 소비 44행, 합계 60행을 `REGION_MONTHLY_STAT`에 저장 |
| 소비 비중 | 구좌 음식점업의 월별 내국인·외국인 비중 88행을 `REGION_CONSUMPTION_STAT`에 저장 |
| 출처·검증 | `DATA_SOURCE` 2개와 자료마다 검증 항목 5개, 합계 10개 |
| 시연용 접근 | 기존과 같은 시연 계정 2개, 구좌 시연 음식점과 관리 권한을 준비함 가상의 계획·실행 기록·추천 기준·홍보 제안은 만들지 않음 |

- 방문 관측값은 2025년과 2026년의 1월부터 8월까지임 음식점 소비는 2023년부터 2025년까지의 36개월과 2026년 1월부터 8월까지임
- 493,209명은 49.3209만 명, 4,546,311,310원은 45.46311310억 원으로 저장함 원래 총량을 정수 만 명·억 원으로 반올림하지 않음
- 소비 비중의 업종은 카페가 아닌 `RESTAURANT`임 내국인에는 내국인 관광객과 도민을 합치고 외국인과 비교함 비중은 소수 둘째 자리 반올림하며 원래 금액과 세 고객군은 원본 JSON에 남김
- 43개 지역의 계절지수, 여러 해를 합친 시간대 비중, 관광지 후보와 카드 이용건수는 원본 스냅샷으로 보존함 계절지수를 실제 인원·금액으로 바꾸거나 다년 평균을 특정 연도의 관측값으로 저장하지 않음
- 수집 기준일은 2026년 10월 27일임 원문의 결측값·기간·출처·이용 조건 미확인 표시·원천 경고를 유지함 지역 방문 집계는 개별 가게의 손님 수가 아님
- 입력 파일의 SHA-256과 구조·관측값을 확인한 후 한 트랜잭션으로 적재함 같은 자료를 다시 실행하면 기존 값과 대조하고 중복 행을 만들지 않음 충돌이 있으면 기존 값을 덮어쓰지 않고 전체 적재를 되돌림
- 현재 일정은 2026년 10월부터 12월까지지만 2026년 9월 이후의 관측값은 이 묶음에 없음 없는 달은 `null`로 표시함 추천 기준이나 4분기 관측값을 임의로 만들지 않음

`db/04_ijto_precision_and_snapshot.sql`을 아직 적용하지 않은 계정은 먼저 한 번 실행함 이미 적용한 계정에서 다시 실행하면 테이블 중복 오류가 나므로 반복하지 않음

초기 이관본의 등록 시각을 가진 계정에는 `db/05_normalize_ijto_registration_date.sql`도 적용함 현재 이관기는 기존 CSV와 동일하게 날짜만 저장하므로 새 적재에는 보정할 행이 없음 원본 스냅샷의 적재 시각은 보존함

macOS에서는 서버 프로젝트 폴더에서 실행함 `JAVA_HOME`은 설치된 JDK 17 이상 경로로 지정하고 접속 정보는 로컬 설정에 준비함

```sh
sh mvnw spring-boot:run '-Dspring-boot.run.arguments=--db.script=db/04_ijto_precision_and_snapshot.sql --spring.main.web-application-type=none'
sh mvnw spring-boot:run '-Dspring-boot.run.arguments=--ijto.import=../../02_제품/데이터/ijto_20261027 --spring.main.web-application-type=none'
```

Windows PowerShell에서는 `sh mvnw` 대신 `.\mvnw.cmd`를 사용함 이관 결과·재실행 결과·API 확인 범위는 [실제 데이터 이관 검증 기록](../../06_증빙/DB/20261111_실제데이터_이관.md)에 별도로 기록함 시연용 접근 정보는 실제 사용자나 사업체 정보가 아님

### 확인이 필요한 점: 한글 입력 길이

팀 DB는 `AL32UTF8`이고 길이 기준이 `BYTE`라서 `VARCHAR(100)`에 한글이 33자까지만 들어감 (한글 1자 = 3바이트) 화면 시안은 계획 이름을 70자까지 받으므로 34자부터는 저장할 수 없음 지금은 서버가 저장 전에 검사해 "한글 기준 33자까지" 안내를 보여줌 글자 수 기준으로 바꾸려면 ERD에서 `VARCHAR2(100 CHAR)`처럼 바꾸는 방안을 팀에서 정해야 함

## 화면(View, 9주차)

`src/main/resources/static`의 `index.html`, `styles.css`, `app.js`가 화면임 서버를 켜면 같은 주소에서 화면과 API가 함께 열림

```powershell
.\mvnw.cmd spring-boot:run
```

브라우저에서 `http://localhost:8080`을 열고 시연 계정으로 로그인함 (사업체 운영자 `cafe@example.com`, 서비스 운영자 `operator@example.com`, 비밀번호 `JejuDemo26!`)

- 화면 구성과 `styles.css`는 [화면 시안](../../02_제품/화면시안/README.md)을 그대로 가져옴 `styles.css` 맨 끝에 추천 근거 패널용 규칙 한 줄만 더함
- `app.js`는 시안의 브라우저 저장 대신 서버 API로 읽고 저장하도록 새로 작성함 브라우저 저장소에는 메뉴 너비 설정만 둠
- 로그인하지 않으면 로그인 화면으로, 사업체 운영자가 자료·기준 관리 주소로 들어오면 한눈에 보기로 이동함
- 활용 사례(UC11)는 화면에 고정한 검증 전 예시이며 DB에 저장하지 않음 (화면설계서 기준)
- 화면 파일을 고쳐 다시 배포해도 브라우저가 옛 파일을 쓰지 않도록 `Cache-Control: no-cache`와 내용 기반 ETag를 씀 (jar 안 파일 시각이 1980년으로 고정되어 시각 비교를 끔)

### 화면 확인 결과 (2026-11-10, Playwright 브라우저)

| 확인한 흐름 | 결과 |
| --- | --- |
| 로그인 | 틀린 비밀번호 안내·초점 이동, 시연 계정 로그인, 로그아웃 확인 후 로그인 화면 |
| 한눈에 보기 | 자료 없음 안내, 3개월 요약(10월·11월 1개씩), 참고 사례, 자료 준비 현황 0% |
| 일정표·계획 | 12월 계획 추가(날짜 2026-12-15 자동 입력), 빈 제목 오류·초점, 저장 후 카드·메뉴 수 갱신, 삭제 확인 후 삭제 |
| 실행 기록·요약 | 계획 값 자동 채움, 클릭 0건과 예약 미확보 구분 표시 |
| 자료·기준 관리 | CSV 선택·다섯 검증 통과 → 등록 버튼 활성, 입력 변경 시 비활성·재검증 안내, 추천 기준 범위 오류 |
| 권한 | 사업체 운영자에게 자료·기준 관리 메뉴 없음, 주소로 들어오면 한눈에 보기로 이동 |
| 자료가 있는 화면 | 지역 분석 그래프·지도·비교 표, 홍보 제안 카드·추천 근거·일정 담기 자동 입력 (공유 DB에 시험 자료를 남기지 않도록 API와 같은 형식의 확인용 응답으로 표시만 확인) |
| 좁은 화면(390px) | 한눈에 보기·지역 분석·홍보 제안·일정표·사용 기록에서 가로 넘침 없음, 추천 근거 패널 표는 표 안에서만 가로로 움직임 |

- 확인하면서 만든 계획·기록은 화면에서 삭제했고, 등록·기준 저장은 실행하지 않아 공유 DB는 샘플 데이터 그대로임
- 실제 CSV를 등록한 상태의 화면은 실제 자료를 받은 뒤 다시 확인해야 함

## 접속·CRUD 테스트

```powershell
.\mvnw.cmd test
```

- `DbConnectionTest`: 접속 계정, Oracle 버전, 현재 테이블·시퀀스 목록과 테이블별 행 수를 출력함 (조회만 함)
- `UserDaoCrudTest`: `USERS` 테이블에 저장, 조회, 수정, 삭제를 확인함
- `AllDaoCrudTest`: 나머지 12개 DAO의 저장, 조회, 수정, 삭제와 NULL 값, AK 중복 차단, FK 규칙(CASCADE, SET NULL, 삭제 차단)을 확인함
- `VisitCsvValidatorTest`: CSV 다섯 검증 규칙을 DB 없이 확인함
- `OverviewApiTest`: 한눈에 보기의 분석·제안·계획 요약·자료 현황과 지역 목록을 확인함
- `SuggestionRulesTest`: 제안 판단 규칙(감소율·기준 비교·누락 달)과 문구 길이를 DB 없이 확인함
- `SuggestionApiTest`: 기준 저장·CSV 등록 → 제안 계산 → 조회·담기·재계산 흐름을 확인함
- `AnalysisApiTest`: 지역 분석의 12개월 값, 최다·최저 달, 지역 비교, 자료 없음, 소비 비교를 확인함
- `AdminApiTest`: 자료·기준 관리 API의 권한, 검증, 등록, 기준 저장을 확인함
- `PlanRecordApiTest`: 로그인·권한·일정표·계획·제안 담기·실행 기록·요약 API를 실제 요청처럼 확인함
- `SampleDataScriptTest`: 샘플 데이터 SQL이 오류 없이 실행되고 시연 계정으로 로그인되는지 확인함 (이미 넣은 DB에서는 건너뜀)
- `AnalysisDecimalTest`: 소수 월별 값의 순위·변화율과 미확보 값 처리를 DB 없이 확인함 `AnalysisApiTest`는 소수의 DAO 저장·일괄 저장·수정·조회 응답도 확인함
- `IjtoDatasetTest`: 실제 JSON의 해시·관측값 환산·누락 달·고객군 비중을 확인함
- `IjtoImporterTest`: 실제 DB 적재의 정확성·재실행 중복 방지·실패 시 되돌리기를 확인함
- DB를 쓰는 테스트는 끝나면 되돌리므로 데이터가 남지 않음 시퀀스 번호만 올라감
- 지역 분석·한눈에 보기·홍보 제안 API 테스트는 기존 자료를 테스트 트랜잭션 안에서만 `PENDING`으로 바꾸고 시험 자료로 검증함 종료 시 원래 상태로 되돌려 실제 자료의 최신 연도와 시험 기준 연도가 섞이지 않게 함
- 로컬 접속 정보가 없으면 `LocalDb` 조건을 쓰는 DB 테스트는 건너뜀 서버 시작 테스트는 별도로 DB 접속 설정이 필요함
