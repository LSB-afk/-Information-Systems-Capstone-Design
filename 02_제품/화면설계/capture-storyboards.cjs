/* Capture numbered screen-design evidence from the local, fixture-only prototype. */
const fs = require('node:fs');
const path = require('node:path');
const {createHash} = require('node:crypto');
const {chromium} = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const root = path.resolve(__dirname, '../..');
const out = path.join(root,'06_증빙/화면설계/screens');
const base = process.env.PREVIEW_URL || 'http://127.0.0.1:8766/';
const common = ['사업체 운영자','서비스 운영자'];
const readers = ['사용자(조회 가정)',...common];
const item = (n, selector, title, action, process, result, target='현재 화면') => ({n,selector,title,action,process,result,target});
const scenes = [];
const add = (key,screenId,name,route,ucs,items,extra={}) => scenes.push({key,screenId,name,route,ucs,items,actors:common,path:`로그인 > 한눈에 보기 > ${name}`,scope:'필수 기능의 화면 시안',states:[],...extra});
add('login','SCR-C-001','로그인','login',[],[
  item(1,'.login-roles','로그인 역할','사업체 또는 서비스 운영자 선택','선택한 체험 역할과 계정 안내 갱신','이메일·비밀번호 입력 준비'),
  item(2,'#login-email','이메일','이메일 입력','형식·빈칸 검사','입력 오류는 해당 필드에 안내'),
  item(3,'.password-field','비밀번호','비밀번호 입력 또는 보기 선택','표시 상태만 전환하고 입력값 유지','값을 전송·저장하지 않음'),
  item(4,'#login-form button[type=submit]','로그인 실행','로그인 클릭','선택 역할의 공개 체험 계정 검사','성공 시 메인으로 이동','SCR-C-002'),
  item(5,'.demo-account','체험 계정','체험 계정 채우기 클릭','선택한 역할의 공개 예시 값 입력','로그인 버튼으로 확인'),
  item(6,'[data-action=enter]','시안 둘러보기','로그인 없이 시안 둘러보기 클릭','선택한 운영자 역할의 시안 시작','실제 서비스의 인증 우회 기능이 아님','SCR-C-002')
],{path:'최초 진입',scope:'최신 사용자 요청에 따른 공통 접근 화면 · 별도 UC ID 없음',states:['정상: 체험 계정 확인 후 메인 이동','오류: 입력 유지·이메일/비밀번호 메시지 표시','실제 서버 인증·권한 보호는 후속 구현']});
add('overview','SCR-C-002','한눈에 보기','overview',['UC01-1','UC02-4','UC03-4','UC10'],[
  item(1,'[data-action=sidebar-toggle]','메뉴 접기','접기·펼치기 클릭','메뉴를 76px 아이콘 형태 또는 저장된 폭으로 변경','본문 너비 재배치'),
  item(2,'#sidebar-resizer','메뉴 너비','경계선을 드래그 또는 방향키 입력','216px부터 360px까지 조절하고 기기에 저장','다시 열어도 너비 유지'),
  item(3,'#region','지역 선택','분석 지역 선택','선택 지역의 예시 결과 갱신','분석·제안에서 같은 지역 사용'),
  item(4,'.chart-box','월별 흐름','방문·소비 지표 선택','선택 지표의 그래프와 단위 갱신','자세한 비교는 지역 분석 메뉴','SCR-C-003'),
  item(5,'a[href="#recommendations"].text-button','제안 더 보기','모든 제안 클릭','선택 지역 유지','홍보 제안으로 이동','SCR-C-004')
],{states:['새로 방문: 기본 메뉴 248px','모바일: 너비 조절 대신 스크롤 가능한 서랍 메뉴','AI 계획 도우미는 확장 검토용 고정 예시 시연']});
add('analysis-visits','SCR-C-003','지역 분석 · 방문과 계절','analysis',['UC01-1','UC01-2','UC10'],[
  item(1,'#region','분석 조건','지역 선택','월별 방문 자료와 지역 비교 갱신','같은 기간의 결과 표시'),
  item(2,'#analysis-year','자료 기간','분석 연도 선택','자료 보유 여부 확인','자료가 없으면 조회 결과 없음 안내'),
  item(3,'[data-region-map="구좌읍"]','지역 지도','도식 지도에서 지역 선택','선택 지역과 월별 그래프 연결','지도는 정확한 행정 경계가 아닌 시안 도식'),
  item(4,'.chart-box','성수기·비수기','월별 추이와 비교 결과 확인','같은 집계 기준의 많은 달·적은 달 비교','지역 방문 집계로 해석'),
  item(5,'[data-analysis-mode="consumption"]','소비 비교','소비 비교 탭 클릭','같은 연도·지역 조건 유지','방문·소비 및 고객 구분 비교','SCR-C-003 / 소비 비교 상태')
],{actors:readers,states:['2025년: 인위적으로 만든 화면용 예시','2023년·2024년: 자료 없음','UC10 출처·기간·집계 기준은 결과와 함께 상시 표시']});
add('analysis-consumption','SCR-C-003','지역 분석 · 소비 비교','analysis',['UC02-1','UC02-2','UC02-3','UC10'],[
  item(1,'[data-analysis-mode="consumption"]','소비 비교 상태','소비 비교 탭 선택','분석 조건에 맞는 예시 표 조회','방문·소비를 단위별로 따로 표시'),
  item(2,'#consumption-comparison','방문·소비 비교','동일 월의 두 지표 확인','기간 겹침·누락 여부 표시','실제 구매전환율이나 1인당 소비액으로 계산하지 않음'),
  item(3,'#timeband-table','시간대별 소비','시간대별 표 확인','월별로 집계한 분포 제공','실시간 혼잡도 또는 개인 기록을 표시하지 않음')
],{actors:readers,prepare:async p=>p.locator('[data-analysis-mode=consumption]').click(),states:['내국인·외국인 비중: 예시 집계값과 단위 표시','소비 미수집: 0원으로 바꾸지 않음','공급사·집계 기준이 다르면 비교 제한']});
add('recommendations','SCR-C-004','홍보 제안','recommendations',['UC02-4','UC02-5','UC02-6','UC02-7','UC10'],[
  item(1,'.recommendation:first-child','시기·대상·상품·관광지','제안과 판단 이유 확인','사용한 출처·기간·조건을 함께 제시','상품 판매 가능성은 사업체가 확인'),
  item(2,'[data-evidence=autumn]','추천 근거','추천 근거 클릭','선택 제안의 이유와 미확인 운영 조건 조회','근거 상세 패널 표시','SCR-C-004-P01'),
  item(3,'[data-rec=autumn]','일정에 담기','일정에 담기 클릭','제안 제목·날짜를 편집 폼에 반영','수정 후 명시적으로 저장','SCR-B-001-P01'),
  item(4,'a[href="#ontology"].button','근거 연결','근거 연결 보기 클릭','자료·관측값·계획 간 관계 표시','관계 설명 화면으로 이동','SCR-C-005')
],{actors:readers,states:['일반 조회 Actor: 일정 저장·비공개 데이터 접근 없음(설계 가정)','F04 설명 초안: 선택·도입 검토 중, 비활성 상태','근거 부족·운영 조건 미확인은 해당 제안에 표시']});
add('calendar','SCR-B-001','3개월 일정','calendar',['UC03-1','UC03-2','UC03-3','UC03-4'],[
  item(1,'[data-action=new-plan]','직접 작성','직접 계획 추가 클릭','빈 계획 입력 폼 표시','제목·예정 날짜·채널·운영 조건 입력','SCR-B-001-P01'),
  item(2,'[data-edit=sample-oct]','계획 수정','계획 수정 클릭','저장된 값 불러오기','수정 폼에서 저장 또는 취소','SCR-B-001-P01'),
  item(3,'[data-record=sample-oct]','실행 기록','실행 기록 클릭','선택한 계획과 연결해 기록 폼 표시','실제 수행 내용과 결과 입력','SCR-B-002-P01'),
  item(4,'[data-month="12"]','빈 달','12월 계획 추가 클릭','해당 월로 날짜를 미리 설정','계획 없는 달에도 직접 작성 가능','SCR-B-001-P01')
],{states:['정상: 저장한 계획을 월별로 표시','빈 상태: 해당 월 계획 추가 제공','저장 실패: 입력 보존·재시도, 작성 완료로 표시하지 않음']});
add('plan-edit','SCR-B-001-P01','계획 작성·수정 팝업','calendar',['UC03-1','UC03-2','UC03-3'],[
  item(1,'#plan-title','활동 제목','제목 입력','공백·최대 길이 검사','오류는 입력 필드에 안내'),
  item(2,'#plan-date','예정 날짜','날짜 선택','3개월 계획 범위인지 확인','범위를 벗어나면 저장 차단'),
  item(3,'#plan-memo','상품·운영 조건','실행할 상품과 준비사항 입력','작성 내용을 계획에 포함','미확인 조건은 실제 적용 전에 확인'),
  item(4,'#plan-form button[type=submit]','저장','저장 클릭','입력 검사 후 브라우저 저장 시도','저장 성공 때만 팝업 닫고 목록 갱신','SCR-B-001'),
  item(5,'#plan-form [data-action=close-dialog]','취소','취소 클릭','변경을 저장하지 않고 닫기','이전 화면·호출 버튼으로 복귀','SCR-B-001')
],{prepare:async p=>{await p.locator('[data-edit=sample-oct]').click();},states:['필수값 오류: 폼 안에 원인 표시','저장 실패: 원래 저장본 유지, 입력한 수정본으로 재시도','저장 성공: 목록 갱신과 완료 안내']});
add('records','SCR-B-002','실행 기록·사용 결과','records',['UC04-1','UC04-2','UC04-3','UC04-4','UC04-5','UC04-6','UC05-1','UC05-2'],[
  item(1,'[data-record=sample-oct]','사용 기록 입력','기록 입력·수정 클릭','해당 계획에 연결된 기록 불러오기','소요 시간·채택·실행·결과 입력','SCR-B-002-P01'),
  item(2,'.record-row:first-child','계획과 실제 비교','계획별 기록 확인','예정 활동·날짜와 실제 활동·날짜 구분','미기록은 실행 실패로 간주하지 않음'),
  item(3,'[data-action=report]','필수 사용 결과 요약','사용 결과 요약 클릭','입력한 기록만 집계','소요 시간·채택·실행 및 확보한 결과 요약','SCR-B-002-P02')
],{states:['빈 상태: 먼저 계획을 만들도록 일정 이동 제공','숫자 0: 실제 확인한 0건, 빈칸: 미수집','F04 보고서 초안은 도입 검토 중, 필수 요약과 분리']});
add('record-edit','SCR-B-002-P01','사용·실행 기록 입력 팝업','records',['UC04-1','UC04-2','UC04-3','UC04-4','UC04-5','UC04-6'],[
  item(1,'#record-minutes','계획 소요 시간','소요 시간을 분 단위로 입력','음수·유효 범위 검사, 빈칸은 미측정','계획별 시간 보관'),
  item(2,'#record-adopted','추천 채택 여부','채택·미채택·확인 중 선택','선택 내용과 연결해 저장','채택 여부를 자동 추정하지 않음'),
  item(3,'#record-activity','실제 활동','실제 활동·실행 날짜 입력','실행했다고 선택한 경우 날짜 검사','예정 활동과 실제 활동을 따로 보관'),
  item(4,'#record-coupons','활동 결과','클릭·예약·쿠폰 사용 건수 입력','비음수 정수 검사, 빈칸은 null 유지','미수집과 실제 0건 구분'),
  item(5,'#record-note','수집 방법','결과 출처·확인 방법 입력','기록과 함께 보관','이후 사용 결과 해석에 활용'),
  item(6,'#record-form button[type=submit]','기록 저장','기록 저장 클릭','입력 검증 후 저장, 실패 시 입력 유지','성공 시 기록 비교 목록 갱신','SCR-B-002')
],{prepare:async p=>p.locator('[data-record=sample-oct]').click(),states:['미실행: 실제 실행 날짜 필수 아님','결과 미확보: 빈칸 허용·0 자동 입력 금지','수집값 오류·저장 오류: 입력 내용 유지']});
add('summary','SCR-B-002-P02','사용 결과 요약 팝업','records',['UC05-1','UC05-2'],[
  item(1,'#dialog .dialog-body','기록 기반 요약','사용 결과 요약 조회','저장한 계획·채택·시간·결과와 미확보 상태 집계','매출 증가나 인과 효과를 추정하지 않음'),
  item(2,'#dialog [data-action=close-dialog]','기록으로 돌아가기','닫기 클릭','요약 팝업 닫기','이전 기록 화면으로 복귀','SCR-B-002')
],{prepare:async p=>p.locator('[data-action=report]').click(),states:['기록 없음: 미기록 안내','미확보 결과: 요약에서도 미확보로 유지','규칙 기반 필수 요약이며 선택 AI 보고서와 구분']});
add('ontology','SCR-C-005','근거 연결','ontology',['UC10'],[
  item(1,'[data-node=source]','자료 버전','자료 버전 선택','해당 관계의 출처·기간 설명 표시','우측 설명 갱신'),
  item(2,'[data-node=recommendation]','제안과 관측값','홍보 제안 관계 선택','사용한 수치와 확인할 운영 조건 표시','상관관계를 인과효과로 해석하지 않음'),
  item(3,'.graph-explanation [data-evidence=winter]','근거 상세','추천 근거 자세히 보기 클릭','선택한 제안의 판단 과정 조회','추천 근거 패널','SCR-C-004-P01')
],{scope:'UC10의 보조 표현 · 새 독립 필수 UC를 추가하지 않음',states:['현재 관계는 고정 예시이며 실제 온톨로지 추론 아님','일반 조회 상태에는 비공개 상품·일정·기록 연결 제외','그래프와 동등한 글로 읽는 관계 제공']});
add('evidence','SCR-C-004-P01','추천 근거 상세 패널','recommendations',['UC02-4','UC02-5','UC02-6','UC02-7','UC10'],[
  item(1,'.evidence-title','선택한 제안','추천 근거 열기','선택한 제안의 제목·시기 표시','이유·조건을 같은 제안 단위로 확인'),
  item(2,'.evidence-row','근거·미확인 조건','자료와 운영 조건 확인','관측값·출처·상품 조건을 분리해 표시','확인하지 않은 상품·예산을 확정하지 않음'),
  item(3,'#dialog [data-rec=winter]','계획에 적용','일정에 담기 클릭','선택 제안을 수정 폼에 반영','명시적 저장 전까지 일정 미변경','SCR-B-001-P01')
],{prepare:async p=>p.locator('[data-evidence=winter]').click(),states:['중복 담기: 새 항목 대신 기존 계획 수정으로 연결','닫기/Escape: 이전 화면과 호출 버튼 복귀']});
add('data-read','SCR-A-001','자료 둘러보기 · 조회 상태','data',['UC10'],[
  item(1,'[data-tab=spend]','자료 종류','방문 또는 카드 소비 선택','선택 지표의 단위와 행 갱신','동일한 자료 기준 표시'),
  item(2,'#data-search','자료 검색','지역명·월 입력','현재 지표의 예시 행 필터링','검색 결과 또는 결과 없음 표시'),
  item(3,'#data-rows','조회 결과','값·단위·자료 상태 확인','누락값을 미수집으로 표시','0으로 임의 보정하지 않음')
],{scope:'자료·기준 관리의 조회 상태 · UC10 보조',states:['사업체 운영자: 읽기 화면, 등록·기준 변경 없음','검색 결과 없음: 검색 조건 수정 안내']});
add('data-admin','SCR-A-001','자료·기준 관리','data',['UC07','UC08-1','UC08-2','UC08-3','UC08-4','UC08-5','UC09-1','UC09-2'],[
  item(1,'#csv-file','자료 선택','CSV 파일 선택','파일·출처·기간·단위·제공 기준 확인 준비','시안은 실제 서버에 업로드하지 않음'),
  item(2,'#csv-provider','제공 기준','공급사·기준 정보 입력','기존 자료와 비교 가능성 검사에 사용','불일치는 정상 비교 자료에서 제외'),
  item(3,'[data-action=csv-sample]','검증 시연','통과 예시 확인','기간·집계·지역 코드·누락·제공 기준 5개 검사','검증 결과 표시','SCR-A-001-P01'),
  item(4,'[data-action=criteria]','추천 기준·근거','추천 기준 편집 클릭','현재 기준과 근거 불러오기','조건·출처·판단 이유 편집','SCR-A-001-P02')
],{actors:['서비스 운영자'],role:'service',states:['자료 검증은 등록의 필수 선행 처리','검증 실패·미확인 항목: 등록 차단','등록·기준 편집은 브라우저 시연이며 서버 처리 미연결']});
add('csv-validation','SCR-A-001-P01','CSV 검증 결과','data',['UC07','UC08-1','UC08-2','UC08-3','UC08-4','UC08-5'],[
  item(1,'#csv-validation','다섯 검증','통과 예시 검증 요청','기간·집계 기준·지역 코드·누락값·제공 기준을 각각 검사','각 결과와 비교 가능한 범위를 표시'),
  item(2,'[data-action=csv-register]','등록 시연','등록 시연 클릭','다섯 검증 완료 여부 확인','예시 등록 상태만 변경, 실자료 서버 저장 아님','SCR-A-001')
],{actors:['서비스 운영자'],role:'service',prepare:async p=>p.locator('[data-action=csv-sample]').click(),states:['일부 누락: 제외 여부와 사용 가능 범위 명시','실패·미확인: 등록 비활성','검증 이후 파일·기준 변경: 이전 통과 상태 무효화']});
add('csv-error','SCR-A-001-P01','CSV 검증 · 등록 불가 상태','data',['UC08-1','UC08-2','UC08-3','UC08-4','UC08-5'],[
  item(1,'#csv-validation','문제 확인','실패 예시 검증 요청','누락 정보·기준 불일치를 항목별로 표시','자료를 수정하거나 통과 예시로 다시 검증'),
  item(2,'[data-action=csv-register]','등록 차단','실패한 자료 등록 시도','필수 검증 통과 전 등록 불허','검증 오류를 수정한 후 다시 검사','SCR-A-001-P01')
],{actors:['서비스 운영자'],role:'service',prepare:async p=>p.locator('[data-action=csv-sample-invalid]').click(),states:['검증 오류를 성공으로 표시하지 않음','없는 수치를 만들어 보완하지 않음']});
add('criteria','SCR-A-001-P02','추천 기준·근거 편집','data',['UC09-1','UC09-2'],[
  item(1,'#dialog form','기준과 근거','추천 기준·조건·분석 결과 입력','출처·판단 이유를 함께 검사','근거가 빠지면 저장 차단'),
  item(2,'#dialog button[type=submit]','기준 저장','기준 저장 클릭','입력한 기준·근거를 브라우저에 저장','설정 시연이며 추천 엔진 자동 적용은 아님','SCR-A-001')
],{actors:['서비스 운영자'],role:'service',prepare:async p=>p.locator('[data-action=criteria]').click(),states:['실제 분석 결과·CSV 확보 후 운영 기준 확정','근거 필수값 누락: 오류 안내','저장 실패: 입력 보존·재시도']});
add('login-error','SCR-C-001','로그인 · 입력 오류 상태','login',[],[
  item(1,'#login-email','이메일 오류','빈 이메일로 로그인 클릭','필수값 검사 실패','이메일로 초점 이동, 오류 안내'),
  item(2,'#login-error','오류 메시지','오류 내용 확인 후 수정','입력한 다른 값은 유지','다시 로그인 시도')
],{path:'최초 진입 > 로그인 오류',prepare:async p=>p.locator('#login-form button[type=submit]').click(),scope:'공통 접근 보조 화면',states:['연결 오류·서버 오류는 실인증 도입 시 추가 검증 필요']});
add('analysis-empty','SCR-C-003','지역 분석 · 자료 없음 상태','analysis',['UC01-1','UC01-2','UC10'],[
  item(1,'#analysis-year','자료 없는 기간','2024년 선택','선택한 기간의 자료 보유 여부 확인','예시 수치로 임의 대체하지 않음'),
  item(2,'#analysis-empty','조회 결과 없음','안내 내용 확인','자료가 없다는 원인과 다음 행동 제시','2025년 예시 또는 다른 조건 선택')
],{actors:readers,prepare:async p=>p.locator('#analysis-year').selectOption('2024'),states:['자료 미확보는 실제 0명·0원과 다름']});
add('plan-save-error','SCR-B-001-P01','계획 작성 · 저장 실패 상태','calendar',['UC03-1','UC03-3'],[
  item(1,'#plan-title','입력 유지','계획을 입력하고 저장 클릭','브라우저 저장 거부 감지','입력값과 편집 폼 유지'),
  item(2,'#plan-error','저장 실패 안내','오류 확인','작성 완료로 처리하지 않고 원래 저장본 유지','저장 가능 상태에서 재시도'),
  item(3,'#plan-form button[type=submit]','재시도','저장 버튼 다시 클릭','같은 입력을 다시 검증·저장 시도','성공할 때만 목록 갱신','SCR-B-001')
],{prepare:async p=>{await p.locator('[data-action=new-plan]').click();await p.locator('#plan-title').fill('겨울 메뉴 사진 준비');await p.evaluate(()=>{Storage.prototype.setItem=function(){throw new DOMException('Fixture storage denial','SecurityError')};});await p.locator('#plan-form button[type=submit]').click();},states:['저장 실패 상태 재현은 테스트용 브라우저에서만 수행','일정 작성·수정의 완료 조건은 저장 성공']});
add('data-empty','SCR-A-001','자료 둘러보기 · 검색 결과 없음','data',['UC10'],[
  item(1,'#data-search','조회 조건','존재하지 않는 지역명 검색','일치하는 자료 행 없음 확인','검색어는 그대로 유지'),
  item(2,'#data-rows','빈 결과 안내','검색 결과 없음 확인','조건 수정 방법 표시','검색어 수정 후 결과 재조회')
],{prepare:async p=>p.locator('#data-search').fill('없는 지역'),scope:'조회 상태',states:['빈 표만 보여주지 않고 검색어 수정 안내 제공']});

async function run() {
  fs.mkdirSync(out,{recursive:true});
  const browser=await chromium.launch({headless:true,executablePath:process.env.CHROME_PATH||'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'});
  const boards=[], errors=[];
  try {
    for(const scene of scenes){
      const context=await browser.newContext({viewport:{width:1440,height:1120},reducedMotion:'reduce',deviceScaleFactor:1});
      const page=await context.newPage();page.setDefaultTimeout(5000);page.on('pageerror',e=>errors.push(`${scene.key}: ${e.message}`));
      if(scene.role==='service')await context.addInitScript(()=>sessionStorage.setItem('jeju-design-demo-session-v1','service'));
      await page.goto(base+'#'+scene.route);await page.evaluate(()=>document.fonts.ready);
      if(scene.prepare)await scene.prepare(page);
      await page.evaluate(()=>window.scrollTo(0,0));
      const positions=[];
      for(const it of scene.items){
        const loc=page.locator(it.selector).first();await loc.waitFor({state:'visible'});
        const box=await loc.boundingBox();
        if(!box || box.y>=1120 || box.y+box.height<=0)throw new Error(`${scene.key} annotation ${it.n} is outside viewport: ${it.selector}`);
        positions.push({n:it.n,x:Math.max(4,box.x-12),y:Math.max(4,Math.min(box.y+5,1090))});
      }
      await page.evaluate(points=>{
        const layer=document.createElement('div');layer.id='storyboard-callouts';layer.style.cssText='position:fixed;inset:0;pointer-events:none;z-index:2147483647';
        for(const p of points){const tag=document.createElement('span');tag.textContent=String(p.n);tag.style.cssText=`position:absolute;left:${p.x}px;top:${p.y}px;width:28px;height:28px;border-radius:50%;display:grid;place-items:center;color:white;background:#af3f20;border:2px solid white;box-shadow:0 1px 4px #0004;font:700 16px "Pretendard Variable",sans-serif`;layer.append(tag);}
        (document.querySelector('dialog[open]')||document.body).append(layer);
      },positions);
      const filename=`${scene.key}.png`;await page.screenshot({path:path.join(out,filename),animations:'disabled'});
      const {prepare,role,route,...entry}=scene;
      boards.push({...entry,annotated:true,image:`06_증빙/화면설계/screens/${filename}`,caption:'로컬 웹 시안 캡처 · 인위적 예시 자료 · Figma 캡처로 교체 전',items:entry.items.map(({selector,...rest})=>rest)});
      console.log(`CAPTURE ${scene.key} (${positions.length} callouts)`);await context.close();
    }
  }finally{await browser.close();}
  if(errors.length)throw new Error(errors.join('\n'));
  const requirements=JSON.parse(fs.readFileSync(path.join(__dirname,'requirements.json'),'utf8'));
  const figmaPath=path.join(__dirname,'figma-status.json');
  const figma=fs.existsSync(figmaPath)?JSON.parse(fs.readFileSync(figmaPath,'utf8')):{url:null,status:'플러그인 계정 연결·Figma 작성·공유 권한 확인 대기'};
  const doc={meta:{title:'제주 비수기 상권 마케팅 캘린더',subtitle:'제주 지역별 방문·소비 데이터를 활용한 소상공인 비수기 마케팅 의사결정 지원 및 적용 사례',team:'Red',date:'2026-09-26',version:'화면설계 v0.1 검토본',figma,captureSource:'로컬 웹 시안'},...requirements,boards};
  fs.writeFileSync(path.join(__dirname,'storyboards.json'),JSON.stringify(doc,null,2)+'\n');
  const hashes=Object.fromEntries(['app.js','styles.css'].map(f=>[f,createHash('sha256').update(fs.readFileSync(path.join(root,'02_제품/화면시안',f))).digest('hex')]));
  fs.writeFileSync(path.join(root,'06_증빙/화면설계/capture-result.json'),JSON.stringify({runAt:new Date().toISOString(),screens:boards.length,callouts:boards.reduce((n,b)=>n+b.items.length,0),source:'web-prototype',figmaCapture:false,hashes,errors},null,2)+'\n');
}
run().catch(e=>{console.error(e);process.exitCode=1});
