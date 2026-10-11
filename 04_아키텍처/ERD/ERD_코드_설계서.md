# ERD 코드 설계서

- 작성일: 2026-10-11
- 서식: 「[8주차] 정보시스템 프로젝트 설계 Data Modeling」 강의자료 55쪽 '1.6 코드 설계서' (코드명 · 관련 테이블 · 관련 컬럼 · 코드 정보)
- 대상: [Red_ERD.erwin](Red_ERD.erwin)의 코드값 컬럼 검토서 ⑬의 11개와 3차 수정에서 생긴 컬럼 3개 ([ERD 수정 내역 0장](ERD_수정_내역.md#0-3차-수정-검토-보고서-반영))
- 근거: 화면 시안 [app.js](../../02_제품/화면시안/app.js)와 [화면설계서](../../00_제출/Red_SCREEN.md)

코드는 DB에 저장하는 값, 뜻은 화면에 보여주는 말임 코드는 영문 대문자로 정했고, 가장 긴 코드도 컬럼 길이 안에 들어가는지 확인함

## 코드 목록

| 번호 | 코드명 | 관련 테이블 | 관련 컬럼 | 자료형 | 가장 긴 코드 |
| --- | --- | --- | --- | --- | --- |
| 1 | 역할 구분 | USERS | ROLE | VARCHAR(20) | BUSINESS (8자) |
| 2 | 실행 상태 | EXECUTION_RECORD | EXEC_STATUS | VARCHAR(10) | NOT_DONE (8자) |
| 3 | 추천 채택 여부 | EXECUTION_RECORD | ADOPTION_STATUS | VARCHAR(10) | UNKNOWN (7자) |
| 4 | 홍보 채널 | PROMO_PLAN, PROMO_SUGGESTION | CHANNEL | VARCHAR(50) | INSTAGRAM (9자) |
| 5 | 지표 구분 | REGION_MONTHLY_STAT | METRIC_TYPE | VARCHAR(10) | VISIT (5자) |
| 6 | 검증 항목 | DATA_VALIDATION_RESULT | VALIDATION_ITEM | VARCHAR(20) | MISSING_VALUE (13자) |
| 7 | 검증 결과 | DATA_VALIDATION_RESULT | RESULT | VARCHAR(10) | PASS (4자) |
| 8 | 자료 검증 상태 | DATA_SOURCE | VALIDATION_STATUS | VARCHAR(20) | PENDING (7자) |
| 9 | 항목 유형 | PROMO_SUGGESTION_ITEM | ITEM_TYPE | VARCHAR(20) | ATTRACTION (10자) |
| 10 | 권한 유형 | BUSINESS_PERMISSION | PERMISSION_TYPE | VARCHAR(10) | MANAGER (7자) |
| 11 | 단위 | DATA_SOURCE | UNIT | VARCHAR(50) | PERSON_10K (10자) |
| 12 | 고객 구분 | REGION_CONSUMPTION_STAT | SEGMENT | VARCHAR(10) | DOMESTIC (8자) |
| 13 | 시간대 | REGION_CONSUMPTION_STAT | TIME_SLOT | VARCHAR(10) | T0611 (5자) |
| 14 | 업종 | BUSINESS, REGION_CONSUMPTION_STAT | CATEGORY | VARCHAR(50) | RESTAURANT (10자) |

검토서 ⑬에서 확인을 요청한 VALIDATION_ITEM VARCHAR(20)은 다섯 검증 항목이 모두 들어감 가장 긴 MISSING_VALUE가 13자임

## 1. 역할 구분

| 코드명 | 역할 구분 |
| --- | --- |
| 관련 테이블 | USERS |
| 관련 컬럼 | ROLE |

| 코드 | 뜻 | 근거 |
| --- | --- | --- |
| BUSINESS | 사업체 운영자 | 로그인 역할 선택 ([app.js 289줄](../../02_제품/화면시안/app.js#L289)) |
| SERVICE | 서비스 운영자 | 같음 |

## 2. 실행 상태

| 코드명 | 실행 상태 |
| --- | --- |
| 관련 테이블 | EXECUTION_RECORD |
| 관련 컬럼 | EXEC_STATUS |

| 코드 | 뜻 | 근거 |
| --- | --- | --- |
| DONE | 실행함 | 사용 기록 입력의 실행 여부 ([app.js 361줄](../../02_제품/화면시안/app.js#L361)) |
| NOT_DONE | 미실행 | 같음 |
| CHECKING | 확인 중 | 같음 |

## 3. 추천 채택 여부

| 코드명 | 추천 채택 여부 |
| --- | --- |
| 관련 테이블 | EXECUTION_RECORD |
| 관련 컬럼 | ADOPTION_STATUS |

| 코드 | 뜻 | 근거 |
| --- | --- | --- |
| YES | 채택 | 사용 기록 입력의 추천 채택 여부 ([app.js 361줄](../../02_제품/화면시안/app.js#L361)) |
| NO | 미채택 | 같음 |
| UNKNOWN | 미확인 | 같음 |

## 4. 홍보 채널

| 코드명 | 홍보 채널 |
| --- | --- |
| 관련 테이블 | PROMO_PLAN, PROMO_SUGGESTION |
| 관련 컬럼 | CHANNEL |

| 코드 | 뜻 | 근거 |
| --- | --- | --- |
| INSTAGRAM | 인스타그램 | 계획 작성의 홍보 채널 ([app.js 355줄](../../02_제품/화면시안/app.js#L355)) |
| STORE | 매장 안내 | 같음 |
| BLOG | 블로그 | 같음 |
| ETC | 기타 | 같음 |

## 5. 지표 구분

| 코드명 | 지표 구분 |
| --- | --- |
| 관련 테이블 | REGION_MONTHLY_STAT |
| 관련 컬럼 | METRIC_TYPE |

| 코드 | 뜻 | 근거 |
| --- | --- | --- |
| VISIT | 방문 (만 명) | 지역 분석의 방문·계절 비교 ([app.js 210줄](../../02_제품/화면시안/app.js#L210)) |
| SPEND | 소비 (억 원) | 지역 분석의 소비 비교 (같음) |

## 6. 검증 항목

| 코드명 | 검증 항목 |
| --- | --- |
| 관련 테이블 | DATA_VALIDATION_RESULT |
| 관련 컬럼 | VALIDATION_ITEM |

| 코드 | 뜻 | 근거 |
| --- | --- | --- |
| PERIOD | 자료 기간 | CSV 검증 항목 ([app.js 263줄](../../02_제품/화면시안/app.js#L263)) |
| AGGREGATION | 집계 기준 | 같음 (264줄) |
| REGION_CODE | 지역 코드 | 같음 (265줄) |
| MISSING_VALUE | 누락값 | 같음 (266줄) |
| PROVIDER | 제공 기준 일관성 | CSV 등록 화면의 검증 목록 ([app.js 238줄](../../02_제품/화면시안/app.js#L238)) |

## 7. 검증 결과

| 코드명 | 검증 결과 |
| --- | --- |
| 관련 테이블 | DATA_VALIDATION_RESULT |
| 관련 컬럼 | RESULT |

| 코드 | 뜻 |
| --- | --- |
| PASS | 통과 |
| FAIL | 실패 (실패 사유는 REASON에 적음) |

## 8. 자료 검증 상태

| 코드명 | 자료 검증 상태 |
| --- | --- |
| 관련 테이블 | DATA_SOURCE |
| 관련 컬럼 | VALIDATION_STATUS |

자료 한 건 전체의 결과임 항목별 결과는 DATA_VALIDATION_RESULT에 저장함

| 코드 | 뜻 |
| --- | --- |
| PASS | 다섯 항목 모두 통과 |
| FAIL | 한 항목 이상 실패 |
| PENDING | 아직 검증하지 않음 |

## 9. 항목 유형

| 코드명 | 항목 유형 |
| --- | --- |
| 관련 테이블 | PROMO_SUGGESTION_ITEM |
| 관련 컬럼 | ITEM_TYPE |

| 코드 | 뜻 | 근거 |
| --- | --- | --- |
| TIMING | 홍보 시기 | 홍보 제안 카드의 네 항목 ([app.js 194줄](../../02_제품/화면시안/app.js#L194)) |
| AUDIENCE | 홍보 대상 | 같음 |
| PRODUCT | 상품 후보 | 같음 |
| ATTRACTION | 주변 관광지 | 같음 |

## 10. 권한 유형

| 코드명 | 권한 유형 |
| --- | --- |
| 관련 테이블 | BUSINESS_PERMISSION |
| 관련 컬럼 | PERMISSION_TYPE |

| 코드 | 뜻 |
| --- | --- |
| OWNER | 소유 |
| MANAGER | 관리 |

## 11. 단위

| 코드명 | 단위 |
| --- | --- |
| 관련 테이블 | DATA_SOURCE |
| 관련 컬럼 | UNIT |

| 코드 | 뜻 | 근거 |
| --- | --- | --- |
| PERSON_10K | 만 명 | CSV 등록의 단위 선택 ([app.js 238줄](../../02_제품/화면시안/app.js#L238)) |
| KRW_100M | 억 원 | 같음 |

## 12. 고객 구분

| 코드명 | 고객 구분 |
| --- | --- |
| 관련 테이블 | REGION_CONSUMPTION_STAT |
| 관련 컬럼 | SEGMENT |

| 코드 | 뜻 | 근거 |
| --- | --- | --- |
| DOMESTIC | 내국인 | 화면설계서 SCR-C-003 소비 비교 ④ 내·외국인 비교 |
| FOREIGN | 외국인 | 같음 |
| ALL | 구분 없음 | 시간대별 비중처럼 고객을 나누지 않는 값 |

## 13. 시간대

| 코드명 | 시간대 |
| --- | --- |
| 관련 테이블 | REGION_CONSUMPTION_STAT |
| 관련 컬럼 | TIME_SLOT |

| 코드 | 뜻 | 근거 |
| --- | --- | --- |
| T0611 | 06시부터 11시 | 화면설계서 SCR-C-003 소비 비교 ⑤ 시간대별 비중, 시간대 구간은 [app.js 207줄](../../02_제품/화면시안/app.js#L207) |
| T1114 | 11시부터 14시 | 같음 |
| T1418 | 14시부터 18시 | 같음 |
| T1806 | 18시부터 다음 날 06시 | 같음 |
| ALL | 구분 없음 | 업종별 내·외국인 비중처럼 시간대를 나누지 않는 값 |

## 14. 업종

| 코드명 | 업종 |
| --- | --- |
| 관련 테이블 | BUSINESS, REGION_CONSUMPTION_STAT |
| 관련 컬럼 | CATEGORY |

| 코드 | 뜻 | 근거 |
| --- | --- | --- |
| CAFE | 카페 | 프로젝트 정의서의 대상 업종 ([Red_PROJECT.md](../../00_제출/Red_PROJECT.md)), 소비 비교 업종 ([app.js 207줄](../../02_제품/화면시안/app.js#L207)) |
| EXPERIENCE | 체험 활동 | 프로젝트 정의서의 대상 업종 |
| RESTAURANT | 음식점 | 소비 비교 업종 ([app.js 207줄](../../02_제품/화면시안/app.js#L207)) |

## 참고: 지역은 코드가 아니라 기준 데이터임

지역은 코드 설계서가 아니라 REGION 테이블의 행으로 관리함 화면 시안에 나온 지역은 아래 두 곳임 ([app.js 238줄](../../02_제품/화면시안/app.js#L238) CSV 안내 문구)

| REGION_CODE | REGION_NAME |
| --- | --- |
| 50110253 | 애월읍 |
| 50110256 | 구좌읍 |
