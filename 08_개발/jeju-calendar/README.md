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

## 접속·CRUD 테스트

```powershell
.\mvnw.cmd test
```

- `DbConnectionTest`: 접속 계정, Oracle 버전, 현재 테이블·시퀀스 목록을 출력함 (조회만 함)
- `UserDaoCrudTest`: `USERS` 테이블에 저장, 조회, 수정, 삭제를 확인함 테스트가 끝나면 되돌리므로 데이터가 남지 않음
- 로컬 접속 정보가 없으면 두 테스트는 건너뜀
