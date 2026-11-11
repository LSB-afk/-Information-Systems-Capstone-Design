# 실제 통계 적재를 위한 DB 확장

2026년 11월 11일 원본 통계 JSON을 보존하고 관측값의 소수 자릿수를 유지하기 위해 적용한 확장임

| 항목 | 기존 ERD | 적용한 DB |
| --- | --- | --- |
| 월별 값 | `REGION_MONTHLY_STAT.STAT_VALUE INTEGER` | `NUMBER` 소수 자릿수를 제한하지 않음 |
| 원본 JSON | 저장할 테이블 없음 | `IJTO_DATA_SNAPSHOT` 추가 CLOB 원문과 출처·커밋·해시·수집일 보관 |
| 테이블 수 | 13개 | 14개 |
| 시퀀스 수 | 9개 | 9개 스냅샷은 문자열 기본키 사용 |
| 기본키 수 | 13개 | 14개 |
| 기존 관계 | FK 17개 | 기존 FK 17개 유지 스냅샷은 독립된 출처 보관 테이블 |

적용 SQL은 [04_ijto_precision_and_snapshot.sql](../../08_개발/jeju-calendar/db/04_ijto_precision_and_snapshot.sql)임 기존 [Red_ERD_Oracle.sql](Red_ERD_Oracle.sql)과 [테이블 생성 SQL](../../08_개발/jeju-calendar/db/01_create_tables.sql)은 원래 ERwin 생성 결과로 유지함 새 DB를 만들 때는 기존 생성 SQL 이후 확장 SQL을 한 번 더 실행함

`Red_ERD.erwin` 편집 원본에는 이번 확장이 아직 반영되지 않았으므로 ERwin에서 모델을 다시 생성할 때 확장 테이블을 누락하거나 `STAT_VALUE`를 정수로 되돌리지 않도록 확인이 필요함 이번 작업에서 편집 원본을 수정한 것으로 보고하지 않음

## 통계의 단위와 코드

- `METRIC_TYPE=VISIT`는 만 명, `SPEND`는 억 원 단위를 유지함 Java DAO·응답·계산은 `BigDecimal` 사용
- 읍면동 방문 코드 10자리를 `REGION.REGION_CODE`에 문자열 그대로 저장함 카드·내비게이션 원천 코드는 JSON에 보존함
- 음식점업 전체의 소비 비중은 `CATEGORY=RESTAURANT`이며 카페 통계로 바꾸지 않음
- `DOMESTIC`은 내국인 관광객과 도민을 합친 비중이며 `FOREIGN`은 외국인 관광객 비중임 세 고객군의 원래 값은 JSON에 보존함
- `SHARE_RATE NUMBER(5,2)`는 유지하고 월별 실제 소비금액으로 계산한 비중만 소수 둘째 자리로 반올림함
- 계절지수·다년 시간대 평균·관광지 후보를 기존 실제 월별 총량 컬럼에 넣지 않음 원문 스냅샷에서 확인함

이관 범위와 검증 결과는 [실제 데이터 이관 기록](../../06_증빙/DB/20261111_실제데이터_이관.md)과 [데이터 묶음 안내](../../02_제품/데이터/ijto_20261027/README.md)에 기록함
