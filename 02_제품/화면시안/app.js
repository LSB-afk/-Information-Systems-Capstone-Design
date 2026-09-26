/* Standalone interaction prototype. All statistics and AI outputs are fixtures. */
'use strict';
const $ = (selector, root = document) => root.querySelector(selector);
const app = $('#app');
const dialog = $('#dialog');
const escapeHTML = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const icons = {
  grid:'<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>',
  chart:'<path d="M4 4v16h17M8 15l4-5 4 2 5-7"/>',
  spark:'<path d="m12 3 2.5 6.5L21 12l-6.5 2.5L12 21l-2.5-6.5L3 12l6.5-2.5L12 3ZM20 2v4M18 4h4"/>',
  calendar:'<rect x="3" y="5" width="18" height="16" rx="3"/><path d="M7 3v4m10-4v4M3 11h18m-14 4h3m4 0h3m-10 3h3"/>',
  check:'<path d="m5 12 4 4L19 6"/>',
  clipboard:'<rect x="5" y="5" width="14" height="16" rx="2"/><rect x="9" y="3" width="6" height="4" rx="1"/><path d="m9 14 2 2 4-4"/>',
  network:'<circle cx="12" cy="5" r="3"/><circle cx="5" cy="18" r="3"/><circle cx="19" cy="18" r="3"/><path d="m10.5 8-4 7m7-7 4 7M8 18h8"/>',
  database:'<ellipse cx="12" cy="5" rx="8" ry="3"/><path d="M4 5v14c0 4 16 4 16 0V5M4 12c0 4 16 4 16 0"/>',
  chevron:'<path d="m9 5 7 7-7 7"/>',
  down:'<path d="m6 9 6 6 6-6"/>',
  arrow:'<path d="M4 12h16m-6-6 6 6-6 6"/>',
  plus:'<path d="M12 5v14M5 12h14"/>',
  close:'<path d="m6 6 12 12M6 18 18 6"/>',
  pin:'<path d="M19 10c0 5-7 11-7 11S5 15 5 10a7 7 0 1 1 14 0Z"/><circle cx="12" cy="10" r="2.5"/>',
  shop:'<path d="M3 9h18l-2-5H5L3 9Zm1 5v7h16v-7M8 21v-6h5v6M3 9v2a3 3 0 0 0 6 0 3 3 0 0 0 6 0 3 3 0 0 0 6 0V9"/>',
  cup:'<path d="M5 8h12v6a6 6 0 0 1-12 0V8Zm12 1h2a3 3 0 0 1 0 6h-2M7 3v2m4-2v2m4-2v2M3 21h17"/>',
  leaf:'<path d="M20 4C7 1 1 12 7 18S23 18 20 4ZM4 21 16 9"/>',
  sun:'<circle cx="12" cy="12" r="4"/><path d="M12 2v2m0 16v2M2 12h2m16 0h2M5 5l1 1m12 12 1 1M5 19l1-1M18 6l1-1"/>',
  clock:'<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
  help:'<circle cx="12" cy="12" r="9"/><path d="M9.5 9a2.5 2.5 0 0 1 5 0c0 2-2.5 2-2.5 4m0 3v.1"/>',
  info:'<circle cx="12" cy="12" r="9"/><path d="M12 11v6m0-10v.1"/>',
  search:'<circle cx="10" cy="10" r="6"/><path d="m15 15 6 6"/>',
  download:'<path d="M12 3v12m-5-5 5 5 5-5M4 16v5h16v-5"/>',
  upload:'<path d="M12 16V4m-5 5 5-5 5 5M4 16v5h16v-5"/>',
  menu:'<path d="M4 6h16M4 12h16M4 18h16"/>',
  panel:'<rect x="3" y="4" width="18" height="16" rx="2"/><path d="M9 4v16m7-12-3 4 3 4"/>',
  eye:'<path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/>',
  logout:'<path d="M10 4H5v16h5m4-12 4 4-4 4m-6-4h13"/>',
  people:'<circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M16 5a3 3 0 0 1 0 6m2 4a5 5 0 0 1 3 4v2"/>',
  edit:'<path d="m15 4 5 5M4 20l5-1L21 7a2 2 0 0 0-5-5L4 14l-1 7M12 21h9"/>',
  book:'<path d="M3 4h6a3 3 0 0 1 3 3v14a3 3 0 0 0-3-3H3V4Zm18 0h-6a3 3 0 0 0-3 3m0 14a3 3 0 0 1 3-3h6V4"/>',
  lock:'<rect x="5" y="10" width="14" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3m-4 5v2"/>'
};
const icon = name => `<svg class="icon" viewBox="0 0 24 24" aria-hidden="true">${icons[name] || icons.grid}</svg>`;
const logo = () => '<svg class="logo" viewBox="0 0 40 40" aria-hidden="true"><rect width="40" height="40" rx="12" fill="#eaf3ec"/><path d="M12 27c3-6 6-8 9-11 4 5 8 9 8 13H12Z" fill="#236447"/><circle cx="14" cy="14" r="4" fill="#eba849"/><path d="M20 10q2-6 9-4-1 6-9 4" fill="#82a777"/><path d="M9 31h22" stroke="#236447" stroke-width="1.5" stroke-linecap="round"/></svg>';
const button = (label, action, extra = '', symbol = '') => `<button class="button ${extra}" data-action="${action}">${symbol ? icon(symbol) : ''}${label}</button>`;
const STORAGE_KEY = 'jeju-design-prototype-v1';
const LAYOUT_KEY = 'jeju-design-layout-v1';
const DEMO_SESSION_KEY = 'jeju-design-demo-session-v1';
// Public fixtures for the login preview; these are not real account credentials.
const demoAccounts = {business:'cafe@example.com',service:'operator@example.com'};
const demoPassword = 'JejuDemo26!';
let demoSessionRole = null;
try { const role=sessionStorage.getItem(DEMO_SESSION_KEY);if(['business','service'].includes(role))demoSessionRole=role; } catch {}
const sidebarLayout = {width:248,collapsed:false};
try {
  const layout=JSON.parse(localStorage.getItem(LAYOUT_KEY)||'null');
  if(Number.isFinite(layout?.width))sidebarLayout.width=Math.min(360,Math.max(216,layout.width));
  sidebarLayout.collapsed=layout?.collapsed===true;
} catch {}
const initialPlans = () => [
  {id:'sample-oct',title:'가을 메뉴 콘텐츠 준비',date:'2026-10-05',channel:'인스타그램',memo:'계절 음료 사진과 소개 문구 준비',region:'애월읍',recId:null},
  {id:'sample-nov',title:'평일 티타임 세트 안내',date:'2026-11-09',channel:'매장 안내',memo:'판매 가능한 메뉴 구성 먼저 확인',region:'애월읍',recId:null}
];
let storageIssue = false;
let recoveryIssue = false;
let saved = {};
try {
  const raw = localStorage.getItem(STORAGE_KEY);
  try { saved = raw === null ? {} : JSON.parse(raw); } catch { recoveryIssue = true; }
} catch { storageIssue = true; }
if (!saved || typeof saved !== 'object' || Array.isArray(saved)) { saved = {}; recoveryIssue = true; }
const validDate = value => typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value) && !Number.isNaN(Date.parse(value)) && new Date(value).toISOString().slice(0,10) === value;
const validPlan = p => p && typeof p.id === 'string' && typeof p.title === 'string' && validDate(p.date) && p.date >= '2026-10-01' && p.date <= '2026-12-31' && typeof p.channel === 'string' && typeof p.memo === 'string' && typeof p.region === 'string';
const validCount = value => value === null || (Number.isInteger(value) && value >= 0 && value <= 99999999);
const validRecord = r => r && typeof r === 'object' && !Array.isArray(r) && ['실행함','미실행','확인 중'].includes(r.status) && (validDate(r.date) || (r.status !== '실행함' && r.date === '')) && validCount(r.clicks) && validCount(r.bookings) && typeof r.note === 'string' && (r.coupons === undefined || validCount(r.coupons)) && (r.planningMinutes === undefined || validCount(r.planningMinutes)) && (r.adopted === undefined || ['yes','no','unknown'].includes(r.adopted)) && (r.activity === undefined || typeof r.activity === 'string');
const restoredPlans = Array.isArray(saved.plans) ? saved.plans.filter(validPlan) : initialPlans();
if (saved.plans !== undefined && (!Array.isArray(saved.plans) || restoredPlans.length !== saved.plans.length)) recoveryIssue = true;
const recordEntries = saved.records && typeof saved.records === 'object' && !Array.isArray(saved.records) ? Object.entries(saved.records) : [];
const restoredRecords = recordEntries.filter(([id,record]) => restoredPlans.some(p => p.id === id) && validRecord(record));
if (saved.records !== undefined && (!saved.records || typeof saved.records !== 'object' || Array.isArray(saved.records) || restoredRecords.length !== recordEntries.length)) recoveryIssue = true;
let state = {
  role:demoSessionRole || (saved.role === 'service' ? 'service' : 'business'),
  region:['애월읍','구좌읍'].includes(saved.region) ? saved.region : '애월읍',
  plans:restoredPlans,
  records:Object.fromEntries(restoredRecords.map(([id,r])=>[id,{planningMinutes:null,adopted:'unknown',activity:'',coupons:null,...r}])),
  metric:'visits',dataTab:'visits',search:'',ontologyNode:'recommendation',analysisYear:'2025',analysisMode:'visits'
};
const regionData = {
  '애월읍':{visits:[35,32,44,49,55,47,65,72,57,51,37,34],spend:[18,19,23,24,30,28,35,42,32,29,null,21]},
  '구좌읍':{visits:[27,25,33,38,42,36,51,55,43,39,29,26],spend:[13,12,18,20,22,21,28,31,25,22,null,17]}
};
const recs = [
  {id:'autumn',month:10,date:'2026-10-19',title:'가을 산책길, 우리 가게 알리기',description:'방문이 이어지는 가을에 대표 메뉴와 가게 위치를 소개해 보세요.',tag:'방문 계기 만들기',icon:'leaf',color:'green',channel:'인스타그램',reason:'예시 방문 자료의 10월 값은 11월보다 높습니다. 겨울을 앞두고 가게를 알릴 시점으로 제안합니다.',task:'대표 메뉴 사진 3장과 찾아오는 길 안내를 준비합니다.'},
  {id:'winter',month:11,date:'2026-11-16',title:'따뜻한 음료로 채우는 평일 오후',description:'실내에서 머물 이유를 제안하세요. 판매 가능한 음료 세트부터 확인합니다.',tag:'비수기 준비',icon:'cup',color:'',channel:'매장 안내',reason:'예시 방문 자료에서 11월은 10월보다 방문이 적습니다. 실내 상품을 알리는 방안이며, 오후 수요는 추가 확인이 필요합니다.',task:'판매 가능한 음료·디저트 구성을 정하고 매장 안내물을 만듭니다.'},
  {id:'return',month:12,date:'2026-12-07',title:'다시 찾고 싶은 연말의 작은 혜택',description:'다음 방문에 쓸 혜택을 설계하고 비용과 운영 가능 여부를 확인하세요.',tag:'재방문 제안',icon:'people',color:'blue',channel:'매장 안내',reason:'예시 자료에서 12월 방문은 10월보다 적습니다. 재방문 혜택의 효과는 확인되지 않았으며 실제 운영 결과를 기록해야 합니다.',task:'운영 가능한 혜택과 이용 조건을 정하고 안내합니다.'}
];
const titles = {overview:'한눈에 보기',analysis:'지역 분석',recommendations:'홍보 제안',calendar:'3개월 일정',records:'실행 기록',ontology:'근거 연결',data:'자료 둘러보기',login:'로그인'};
let currentView = 'overview';
let opener = null;
let toastTimer;
let roleChoice = state.role;
function persist() {
  try { localStorage.setItem(STORAGE_KEY,JSON.stringify({role:state.role,region:state.region,plans:state.plans,records:state.records}));storageIssue=false;return true; }
  catch { storageIssue=true;return false; }
}
function notify(message) {
  clearTimeout(toastTimer);$('#toast').textContent=message;$('#toast').classList.add('visible');
  toastTimer=setTimeout(()=>$('#toast').classList.remove('visible'),4000);
}
function applySidebarLayout() {
  document.documentElement.style.setProperty('--sidebar-width',`${sidebarLayout.collapsed?76:sidebarLayout.width}px`);
  document.documentElement.classList.toggle('sidebar-collapsed',sidebarLayout.collapsed);
  const toggle=$('[data-action="sidebar-toggle"]');
  if(toggle){
    const label=sidebarLayout.collapsed?'메뉴 펼치기':'메뉴 접기';
    toggle.setAttribute('aria-label',label);toggle.title=label;
    toggle.setAttribute('aria-expanded',String(!sidebarLayout.collapsed));
  }
  const handle=$('#sidebar-resizer');
  if(handle){handle.setAttribute('aria-valuenow',String(sidebarLayout.width));handle.setAttribute('aria-valuetext',`${sidebarLayout.width}px`);}
}
function saveSidebarLayout() {
  try {localStorage.setItem(LAYOUT_KEY,JSON.stringify(sidebarLayout));}
  catch {notify('메뉴 설정을 저장하지 못했어요. 현재 화면에는 적용했습니다.');}
}
function closeMobileMenu(restoreFocus=false) {
  $('.sidebar')?.classList.remove('open');$('.mobile-scrim')?.classList.remove('open');
  if($('.workspace'))$('.workspace').inert=false;
  document.documentElement.classList.remove('menu-open');
  $('.mobile-menu')?.setAttribute('aria-expanded','false');
  if(restoreFocus)$('.mobile-menu')?.focus();
}
function navigate(view) {
  if (!titles[view]) view='overview';
  closeDialog();
  if(location.hash.slice(1) === view) render(); else location.hash=view;
}
function navItem(view,symbol) {
  const label=view==='data' && state.role==='service'?'자료·기준 관리':titles[view];
  return `<a class="nav-link ${currentView===view?'active':''}" href="#${view}" aria-label="${label}" title="${label}" ${currentView===view?'aria-current="page"':''}>${icon(symbol)}<span>${label}</span>${view==='calendar'?`<span class="count">${state.plans.length}</span>`:''}</a>`;
}
function shell(content) {
  return `<button class="mobile-scrim" data-action="close-menu" aria-label="메뉴 닫기" tabindex="-1"></button>
  <aside class="sidebar" id="sidebar" aria-label="워크스페이스 메뉴"><a class="brand" href="#overview" aria-label="제주 마케팅 캘린더 홈" title="제주 마케팅 캘린더 홈">${logo()}<span><strong>제주 마케팅<br>캘린더</strong><small>다음 계절을 준비하는 공간</small></span></a>
    <div class="business-card"><span class="avatar">${icon(state.role==='service'?'database':'shop')}</span><span><b>${state.role==='service'?'자료 운영 워크스페이스':'제주 예시 카페'}</b><small>${state.role==='service'?'서비스 운영자 시안':'사업체 운영자 시안'}</small></span></div>
    <div class="sidebar-controls"><span>나에게 맞는 메뉴 너비</span><button class="text-button" data-action="sidebar-reset" title="메뉴 너비 초기화" aria-label="메뉴 너비 초기화">초기화</button></div>
    <p class="nav-label">마케팅 워크스페이스</p><nav aria-label="주요 메뉴" class="nav-group">${navItem('overview','grid')}${navItem('analysis','chart')}${navItem('recommendations','spark')}${navItem('calendar','calendar')}${navItem('records','clipboard')}</nav>
    <p class="nav-label">자료와 근거</p><nav aria-label="자료 메뉴" class="nav-group">${navItem('ontology','network')}${navItem('data','database')}</nav>
    <div class="sidebar-bottom"><div class="sidebar-note">${icon('leaf')}<b>작은 계획부터 차근차근</b><p>우리 가게에 맞는 제안을 고르고<br>실행할 수 있는 일정으로 바꿔요.</p></div><button class="profile" data-action="roles" aria-label="계정과 역할 선택" title="계정과 역할 선택"><span class="avatar">${state.role==='service'?'운':'가'}</span><span><b>${state.role==='service'?'서비스 운영자':'사업체 운영자'}</b><small>계정과 역할 선택</small></span>${icon('chevron')}</button>${demoSessionRole?`<button class="sidebar-session" data-action="logout" title="로그아웃" aria-label="로그아웃">${icon('logout')}<span>로그아웃</span></button>`:`<a class="sidebar-session" href="#login" title="로그인" aria-label="로그인">${icon('lock')}<span>로그인</span></a>`}</div>
  </aside><div id="sidebar-resizer" class="sidebar-resizer" role="separator" aria-label="메뉴 너비 조절" aria-controls="sidebar" aria-orientation="vertical" aria-valuemin="216" aria-valuemax="360" aria-valuenow="${sidebarLayout.width}" tabindex="0" title="끌어서 너비 조절 · 방향키로도 조절할 수 있어요"><span></span></div><div class="workspace"><header class="topbar"><div class="row"><button class="icon-button desktop-menu" data-action="sidebar-toggle" aria-label="메뉴 접기" aria-controls="sidebar" aria-expanded="true">${icon('panel')}</button><button class="icon-button mobile-menu" data-action="menu" aria-label="메뉴 열기" aria-expanded="false" aria-controls="sidebar">${icon('menu')}</button><div class="breadcrumb"><span>워크스페이스</span>${icon('chevron')}<strong>${titles[currentView]}</strong></div></div><div class="topbar-actions"><span class="prototype-label">예시 데이터 · 화면 시안</span><button class="icon-button" data-action="help" aria-label="시안 사용 안내">${icon('help')}</button><span class="avatar" aria-label="예시 계정">${state.role==='service'?'운':'가'}</span></div></header><main id="main" class="content" tabindex="-1">${storageIssue?'<p class="notice error">브라우저 저장을 사용할 수 없습니다. 변경 내용은 현재 화면에서만 유지됩니다.</p>':''}${recoveryIssue?'<p class="notice">저장된 자료에 복원하지 못한 항목이 있습니다. 정상 항목은 유지했으니 계획과 기록을 확인해 주세요.</p>':''}${content}<footer class="footer-note"><span>화면 검토용 예시 자료입니다. 지역 방문 집계는 가게의 실제 고객 수가 아닙니다.</span><button data-action="help">시안 사용 안내</button></footer></main></div>`;
}
function heading(title,description,action='') {
  return `<div class="page-heading"><div><div class="eyebrow">${icon('pin')}제주 ${state.region} · 카페</div><h1>${title}</h1><p>${description}</p></div>${action?`<div class="heading-action">${action}</div>`:''}</div>`;
}
function filters() {
  return `<div class="filter-strip"><label class="filter-pill">${icon('pin')}분석 지역 <select aria-label="분석 지역" id="region"><option ${state.region==='애월읍'?'selected':''}>애월읍</option><option ${state.region==='구좌읍'?'selected':''}>구좌읍</option></select></label>${currentView==='analysis'?`<label class="filter-pill">${icon('calendar')}분석 연도 <select id="analysis-year" aria-label="분석 연도">${['2025','2024','2023'].map(y=>`<option value="${y}" ${state.analysisYear===y?'selected':''}>${y}년${y==='2025'?' 예시':' · 자료 없음'}</option>`).join('')}</select></label>`:`<span class="filter-pill">${icon('calendar')}분석 자료 <b>2025년 예시</b></span>`}<span class="filter-divider"></span><span class="filter-note">계획 대상 <b>2026년 10월부터 12월까지</b></span></div>`;
}
function chartSVG(metric=state.metric) {
  const values=regionData[state.region][metric==='visits'?'visits':'spend'];
  const max=metric==='visits'?80:50, width=620, top=24, bottom=186, left=36, step=50;
  const points=values.map((v,i)=>[left+i*step,v===null?null:bottom-v/max*(bottom-top)]);
  let segments=[], segment=[];
  points.forEach(p=>{if(p[1]===null){if(segment.length)segments.push(segment);segment=[];}else segment.push(p);});if(segment.length)segments.push(segment);
  const grid=Array.from({length:5},(_,i)=>{const val=max-i*max/4,y=top+i*(bottom-top)/4;return `<line class="gridline" x1="36" y1="${y}" x2="600" y2="${y}"/><text x="24" y="${y+4}" text-anchor="end">${val}</text>`;}).join('');
  const paths=segments.map(s=>`<path d="M${s.map(p=>p.join(',')).join('L')}" fill="none" stroke="#528451" stroke-width="2.8" stroke-linecap="round" stroke-linejoin="round"/>`).join('');
  const area=metric==='visits'?`<path d="M${left},${bottom}L${points.map(p=>p.join(',')).join('L')}L586,${bottom}Z" fill="#eaf2e5" opacity=".7"/>`:'';
  return `<svg class="chart" viewBox="0 0 ${width} 221" role="img" aria-label="${state.region} 2025년 ${metric==='visits'?'월별 방문 집계, 단위 만 명':'월별 카드 소비, 단위 억 원'}. 예시 자료이며 수치는 아래 표에서 확인할 수 있습니다."><rect x="511" y="12" width="92" height="183" rx="7" fill="#fff6e5"/>${grid}${area}${paths}${points.map((p,i)=>p[1]===null?`<text x="${p[0]}" y="${bottom-9}" text-anchor="middle" fill="#8a4b16">누락</text>`:`<circle cx="${p[0]}" cy="${p[1]}" r="${i>=10?4.5:3.5}" fill="${i>=10?'#eba849':'#528451'}" stroke="white" stroke-width="2"><title>${i+1}월: ${values[i]}${metric==='visits'?'만 명':'억 원'} (예시)</title></circle>`).join('')}${values.map((_,i)=>`<text x="${left+i*step}" y="213" text-anchor="middle">${i+1}월</text>`).join('')}</svg>`;
}
function chartTable() {
  const key=state.metric==='visits'?'visits':'spend';
  return `<details class="chart-table"><summary>월별 수치 표로 보기</summary><div class="table-wrap"><table><caption class="sr-only">2025년 ${state.region} 월별 예시 ${key==='visits'?'방문 집계, 만 명':'카드 소비, 억 원'}</caption><thead><tr>${Array.from({length:12},(_,i)=>`<th scope="col">${i+1}월</th>`).join('')}</tr></thead><tbody><tr>${regionData[state.region][key].map(v=>`<td>${v===null?'미수집':v}</td>`).join('')}</tr></tbody></table></div></details>`;
}
function trendPanel() {
  const visit=state.metric==='visits';
  return `<section class="panel"><div class="chart-box"><div class="panel-heading"><div><h2>우리 지역의 계절 흐름</h2><p>${state.region} · 2025년 1월부터 12월 예시 자료</p></div><div class="segmented" aria-label="차트 지표"><button data-metric="visits" aria-pressed="${visit}" class="${visit?'selected':''}">방문</button><button data-metric="spend" aria-pressed="${!visit}" class="${!visit?'selected':''}">소비</button></div></div><div class="chart-summary"><strong>${visit?'겨울을 앞두고 줄어드는 방문':'방문과 구분해 살펴보는 소비'}</strong></div>${chartSVG()}<div class="chart-note"><span><i class="legend-dot"></i>${visit?'지역 방문 집계 · 만 명':'카드 소비 · 억 원'}</span><span><i class="legend-dot orange"></i>${visit?'11·12월 참고 구간':'11월 자료 미수집'}</span></div>${chartTable()}</div><div class="chart-foot">${icon('info')}<span>출처: 화면 시안 v1 · 2025년 월별 지역 집계 · ${visit?'만 명':'억 원, 11월 미수집'} · 실제 통계 아님</span><button class="text-button" data-action="source">자료 기준 ${icon('chevron')}</button></div></section>`;
}
function assistantCard() {
  return `<section class="assistant-card"><span class="assistant-tag">${icon('spark')}AI 계획 도우미 <span class="badge amber">검토 중 · 시연</span></span><h2>다음 3개월,<br>함께 준비해 볼까요?</h2><p>지역의 흐름과 가게의 조건을 연결해<br>실행할 계획으로 정리해요.</p><div class="assistant-illustration" aria-hidden="true">${[10,11,12].map(m=>`<div class="month-leaf"><small>2026</small><strong>${m}</strong></div>`).join('')}</div>${button('계획 초안 살펴보기','agent','primary full','spark')}<span class="demo-sub">정해진 예시로 체험 · 실제 AI 미연결</span></section>`;
}
function recommendationCard(r) {
  const added=state.plans.some(p=>p.recId===`${state.region}:${r.id}`);
  const target=r.id==='return'?'내국인 중심 재방문 안내 후보':'내국인 중심 안내 + 외국인용 위치 설명 후보';
  const place=state.region==='애월읍'?'한담해안산책로':'세화해변';
  return `<article class="recommendation"><div class="recommendation-top"><div class="rec-symbol ${r.color}">${icon(r.icon)}</div><span class="badge ${r.month===11?'amber':'green'}">${r.month}월 · ${r.tag}</span></div><h3>${r.title}</h3><p>${r.description}</p>${currentView==='recommendations'?`<dl class="proposal-fields"><dt>홍보 시기</dt><dd>2026년 ${r.month}월 · 과거 계절 흐름 참고</dd><dt>홍보 대상</dt><dd>${target}<small>이유: 카페 소비 인공 예시의 내국인 비중이 더 높음 · 실제 고객 확인 필요</small></dd><dt>상품 후보</dt><dd>${r.id==='winter'?'따뜻한 음료·디저트 세트':'대표 계절 음료와 재방문 안내'}<small>이유: 계절과 실내 이용을 고려한 후보 · 판매·원가 확인 필요</small></dd><dt>주변 관광지</dt><dd>${place} 방문 전후 위치 안내 · 제휴 아님<small>이유: 같은 지역의 방문 동선을 함께 소개하는 가정 · 거리·운영시간 확인 필요</small></dd></dl>`:''}<p class="proposal-reason"><b>판단 이유</b> ${r.reason}${currentView==='recommendations'?' 내국인·외국인 소비 비중은 별도의 인공 예시이며 실제 대상 적합성·거리·판매 조건은 운영자가 확인해야 합니다.':''}</p><p class="source-inline">출처: 화면 시안 방문 v1·소비 구성 v1<br>방문: 2025년 1월부터 12월 · 소비 구성: 10월<br>집계 기준: 월별 지역 방문(만 명), 업종 카드 소비 비중(%) · 인공 예시</p><div class="rec-meta"><span>${icon('pin')}${state.region}</span><span>${icon('calendar')}2026년 ${r.month}월</span></div><div class="rec-bottom"><button class="text-button" data-evidence="${r.id}">${icon('network')}추천 근거</button><button class="button small ${added?'secondary':''}" data-rec="${r.id}">${icon(added?'check':'plus')}${added?'일정에 추가됨':'일정에 담기'}</button></div></article>`;
}
function timeline() {
  return `<section class="panel timeline-panel"><div class="timeline-intro"><h3>우리 가게의 3개월</h3><p>${state.plans.length}개 계획을 준비하고 있어요</p></div><div class="timeline-months">${[10,11,12].map(m=>{const n=state.plans.filter(p=>Number(p.date.slice(5,7))===m).length;return `<div class="timeline-item"><b>${m}월</b><span>${n?`${n}개 계획 준비 중`:'새 계획을 담아보세요'}</span></div>`;}).join('')}</div><a class="button small" href="#calendar">일정 보기 ${icon('chevron')}</a></section>`;
}
function overview() {
  return `${heading('우리 가게의 다음 계절','지역의 흐름을 읽고, 실행할 수 있는 홍보 계획을 세워보세요.',button('AI로 계획 세우기','agent','primary','spark')+'<span class="badge amber">확장 검토 · 시연</span>')}${filters()}<div class="overview-grid">${trendPanel()}${assistantCard()}</div><div class="section-header"><div><h2>작은 실행으로 이어지는 홍보 제안</h2><p>우리 가게에 맞는지 확인하고 일정에 담아보세요.</p></div><a class="text-button" href="#recommendations">모든 제안 ${icon('chevron')}</a></div><div class="recommendation-grid">${recs.map(recommendationCard).join('')}</div>${timeline()}`;
}
function regionMap() {
  return `<section class="panel panel-padding"><div class="panel-heading"><div><h2>지역을 선택해 비교해요</h2><p>분석 지역 위치 개념도 · 실제 경계·축척과 다름</p></div><span class="badge">예시 2개 지역</span></div><div class="region-sketch" role="group" aria-label="제주 분석 지역 위치 개념도"><span class="sketch-north">북 ↑</span><div class="island-shape" aria-hidden="true"></div>${['애월읍','구좌읍'].map((name,i)=>`<button class="map-region region-${i} ${state.region===name?'selected':''}" data-region-map="${name}" aria-pressed="${state.region===name}">${icon('pin')}<b>${name}</b><small>${regionData[name].visits[9]}만 명 · 10월 예시</small></button>`).join('')}<span class="sketch-caption">제주도 위치 개념도</span></div><p class="source-inline">출처: 화면 시안 v1 · 2025년 10월 · 월별 지역 방문 집계, 만 명 · 인공 예시</p></section>`;
}
function consumptionAnalysis() {
  const v=regionData[state.region], domestic=state.region==='애월읍'?70:65;
  return `<section class="panel panel-padding" id="consumption-comparison"><div class="panel-heading"><div><h2>방문과 소비를 함께 살펴봐요</h2><p>${state.region} · 2025년 10월 인공 예시</p></div><span class="badge amber">실제 통계 아님</span></div><div class="table-wrap"><table><thead><tr><th>기준월</th><th>지역 방문</th><th>카드 소비</th><th>비교 가능 여부</th></tr></thead><tbody><tr><td>2025년 10월</td><td>${v.visits[9]}만 명</td><td>${v.spend[9]}억 원</td><td>각 지표의 변화만 비교</td></tr><tr><td>2025년 11월</td><td>${v.visits[10]}만 명</td><td>미수집</td><td>소비 비교 불가 · 0으로 대체하지 않음</td></tr></tbody></table></div><p class="source-inline">출처: 화면 시안 v1 · 2025년 월별 지역 집계 · 방문(만 명)·카드 소비(억 원)는 다른 모집단 · 가게 전환율·1인당 소비액 계산 불가</p><div class="two-col consumption-tables"><section><h3>내국인·외국인 소비 비중</h3><p class="small muted">2025년 10월 · 업종별 소비액 안의 비중</p><div class="table-wrap"><table><thead><tr><th>업종</th><th>내국인</th><th>외국인</th></tr></thead><tbody><tr><td>음식점</td><td>${domestic}%</td><td>${100-domestic}%</td></tr><tr><td>카페</td><td>${domestic+5}%</td><td>${95-domestic}%</td></tr></tbody></table></div><p class="source-inline">출처: 화면 시안 소비 구성 v1 · 인공 예시 · 월별 업종 카드 소비액 비중(%) · 실제 대상 선정에는 원자료 확인 필요</p></section><section id="timeband-table"><h3>시간대별 소비</h3><p class="small muted">2025년 10월 · 카페 · 월별로 모은 예시</p><div class="table-wrap"><table><thead><tr><th>시간대</th><th>소비 비중</th></tr></thead><tbody>${[['06시부터 11시','15'],['11시부터 14시','25'],['14시부터 18시','40'],['18시부터 다음 날 06시','20']].map(([t,n])=>`<tr><td>${t}</td><td>${n}%</td></tr>`).join('')}</tbody></table></div><p class="source-inline">출처: 화면 시안 소비 구성 v1 · 인공 예시 · 해당 월 카페 카드 소비액의 시간대별 비중(%) · 실시간 자료 아님</p></section></div></section>`;
}
function analysis() {
  const header=`${heading('지역의 흐름을 이해하는 시간','방문과 소비를 각각 비교하고, 자료의 기준부터 확인하세요.')}${filters()}<div class="source-tabs" aria-label="분석 항목"><button data-analysis-mode="visits" aria-pressed="${state.analysisMode==='visits'}" class="${state.analysisMode==='visits'?'selected':''}">방문·계절 비교</button><button data-analysis-mode="consumption" aria-pressed="${state.analysisMode==='consumption'}" class="${state.analysisMode==='consumption'?'selected':''}">소비 비교</button></div>`;
  if(state.analysisYear!=='2025')return `${header}<section class="panel empty-state" id="analysis-empty" role="status"><div class="empty-icon">${icon('database')}</div><h2>${state.analysisYear}년 자료가 없습니다</h2><p>선택한 연도의 비교 가능한 자료가 없어 그래프와 제안을 만들지 않습니다. 2025년 예시를 선택해 화면 흐름을 확인하세요.</p></section>`;
  if(state.analysisMode==='consumption')return header+consumptionAnalysis();
  const v=regionData[state.region].visits;
  return `${header}<div class="two-col analysis-map-grid">${regionMap()}${trendPanel()}</div><div class="section-header"><h2>성수기·비수기 비교</h2><span class="badge">2025년 인공 예시</span></div><section class="panel"><div class="table-wrap"><table><thead><tr><th scope="col">지역</th><th scope="col">가장 많은 달</th><th scope="col">가장 적은 달</th><th scope="col">10월 → 11월</th></tr></thead><tbody>${Object.entries(regionData).map(([name,d])=>`<tr><td><b>${name}</b></td><td>8월 · ${Math.max(...d.visits)}만 명</td><td>2월 · ${Math.min(...d.visits)}만 명</td><td>${((d.visits[10]-d.visits[9])/d.visits[9]*100).toFixed(1)}%</td></tr>`).join('')}</tbody></table></div><p class="table-caption">출처: 화면 시안 v1 · 2025년 1월부터 12월 · 월별 지역 방문 집계, 만 명 · 변화율 = (11월 − 10월) ÷ 10월 × 100 · 가게 손님 수와 구분</p></section>`;
}
function recommendations() {
  return `${heading('우리 가게에 맞는 홍보 아이디어','근거를 살펴보고, 운영할 수 있는 계획을 골라보세요.',button('AI로 계획 세우기','agent','primary','spark')+'<span class="badge amber">확장 검토 · 시연</span>')}${filters()}<section class="intro-callout">${icon('network')}<div><h3>왜 이 제안인지, 근거까지 함께</h3><p>지역·시기·운영 조건의 연결을 확인할 수 있어요. 상품과 비용은 적용 전 확인해 주세요.</p></div><a class="button small" href="#ontology">근거 연결 보기</a></section><div class="section-header"><h2>가을부터 겨울까지 제안 3개</h2><span class="badge amber">예시 추천</span></div><div class="recommendation-grid">${recs.map(recommendationCard).join('')}</div>${optionalFeature('추천 이유 설명 초안 · UC06-1')}${timeline()}`;
}
function planCard(p) {
  const recorded=!!state.records[p.id];
  return `<article class="plan-item"><div class="plan-date"><span>${Number(p.date.slice(5,7))}월 ${Number(p.date.slice(8))}일 · ${escapeHTML(p.channel)}</span>${recorded?'<span class="accent">기록 완료</span>':''}</div><h3>${escapeHTML(p.title)}</h3><p>${escapeHTML(p.memo || '운영할 수 있는 조건을 확인해 주세요.')}</p><span class="badge">${escapeHTML(p.region)}</span><div class="row between"><button class="text-button" data-edit="${escapeHTML(p.id)}">${icon('edit')}계획 수정</button><button class="text-button" data-record="${escapeHTML(p.id)}">${recorded?'기록 보기':'실행 기록'} ${icon('chevron')}</button></div></article>`;
}
function calendar() {
  return `${heading('생각을 실행할 일정으로','홍보 제안을 담거나, 우리 가게의 계획을 직접 작성하세요.',button('직접 계획 추가','new-plan','primary','plus'))}<div class="toolbar"><div class="row"><h2>2026년 10월 — 12월</h2><span class="badge green">${state.plans.length}개 계획</span></div><div class="row">${button('AI 초안 보기','agent','','spark')}<a class="button" href="#recommendations">홍보 제안 보기</a></div></div><div class="calendar-grid">${[10,11,12].map(m=>{const list=state.plans.filter(p=>Number(p.date.slice(5,7))===m).sort((a,b)=>a.date.localeCompare(b.date));return `<section class="month-column"><header class="month-heading"><div><small>2026</small><b>${m}월</b></div><span class="badge ${m===11?'amber':'green'}">${['가을 준비','겨울 시작','연말 운영'][m-10]}</span></header><div class="month-content">${list.length?list.map(planCard).join(''):`<div class="month-empty">${icon('calendar')}<p>이달의 계획을 채워보세요.</p></div>`}<button class="plan-add" data-month="${m}">${icon('plus')}${m}월 계획 추가</button></div></section>`;}).join('')}</div><div class="steps-line"><b>실행 가능한 계획 만들기</b><span>근거 확인</span>${icon('chevron')}<span>상품·일정 수정</span>${icon('chevron')}<span>이 브라우저에 저장</span>${icon('chevron')}<span>실행 기록 남기기</span></div>`;
}
function records() {
  const done=state.plans.filter(p=>state.records[p.id]);
  return `${heading('실행한 만큼, 다음 계획이 선명해져요','계획별 실행 여부와 실제로 수집한 결과를 기록하세요.',button('사용 결과 요약','report','','book'))}<div class="mini-stats"><div class="mini-stat"><p>준비한 계획</p><strong>${state.plans.length}<small>개</small></strong><span>2026년 10월부터 12월</span></div><div class="mini-stat"><p>실행 기록 작성</p><strong>${done.length}<small>개</small></strong><span>입력한 예시 기록 기준</span></div><div class="mini-stat"><p>아직 기록하지 않은 계획</p><strong>${state.plans.length-done.length}<small>개</small></strong><span>기록 없음은 실행 실패를 뜻하지 않아요</span></div></div><section class="panel">${state.plans.length?state.plans.map(p=>{const record=state.records[p.id];return `<div class="record-row"><div class="date-box"><small>${Number(p.date.slice(5,7))}월</small><strong>${Number(p.date.slice(8))}</strong></div><div><h3>${escapeHTML(p.title)}</h3><p>${escapeHTML(p.channel)} · ${record?`${escapeHTML(record.status)} / 클릭 ${record.clicks===null?'미수집':escapeHTML(record.clicks)+'건'}`:'실행 결과를 아직 입력하지 않았어요'}</p></div><span class="badge ${record?'green':''}">${record?'기록 있음':'미기록'}</span><button class="button small" data-record="${escapeHTML(p.id)}">${record?'기록 수정':'기록 입력'}</button></div>`;}).join(''):`<div class="empty-state"><div class="empty-icon">${icon('clipboard')}</div><h3>먼저 작은 계획을 만들어 볼까요?</h3><p>일정에서 직접 계획을 추가하면 이곳에서 실행 여부와 결과를 기록할 수 있어요.</p><a class="button primary" href="#calendar">일정 작성하기</a></div>`}</section><p class="notice" style="margin-top:20px">수집하지 않은 클릭·예약·쿠폰 사용 수는 빈칸으로 남겨 주세요. 0건과 미수집은 다르게 기록됩니다.</p>${optionalFeature('보고서 초안 · UC06-2')}`;
}
const ontologyInfo = {
  recommendation:['홍보 제안','따뜻한 음료로 채우는 평일 오후','11월 방문 흐름을 참고하는 예시 제안입니다. 실내 상품 판매 가능 여부와 오후 수요는 별도로 확인해야 합니다.'],
  observation:['관측값','지역 방문 · 2025년 11월','지역과 기준기간, 단위, 자료 버전을 함께 보존합니다. 가게의 실제 손님 수를 의미하지 않습니다.'],
  product:['운영 조건','예시 카페의 음료 세트','판매 가능 상품과 휴무일을 확인하는 연결입니다. 실제 사업체 자료가 입력된 상태는 아닙니다.'],
  source:['자료 버전','화면 시안 방문 자료 v1','디자인 검토용으로 만든 월별 수치입니다. 공공기관이 제공한 실제 통계로 표시하지 않습니다.'],
  plan:['일정 항목','2026년 11월의 홍보 계획','추천을 채택하거나 직접 작성할 수 있습니다. 수정한 내용과 실행 기록을 연결합니다.']
};
function ontology() {
  const info=ontologyInfo[state.ontologyNode];
  const node=(id,label,sub,main=false)=>`<button class="graph-node ${main?'main':''}" data-node="${id}" aria-pressed="${state.ontologyNode===id}"><small>${sub}</small><strong>${label}</strong></button>`;
  return `${heading('제안의 이유를 따라가 보세요','지역, 자료, 상품, 계획이 어떻게 연결되는지 확인하는 온톨로지 화면입니다.')}<div class="intro-callout">${icon('network')}<div><h3>무엇을 근거로, 어떤 계획을 제안했을까요?</h3><p>관계를 선택하면 의미와 확인할 조건을 볼 수 있어요. 현재는 예시 관계를 연결한 화면 시안입니다.</p></div></div><div class="ontology-layout"><section class="panel"><div class="panel-padding"><div class="row between"><h2>추천 근거 연결</h2><span class="badge amber">예시 관계</span></div></div><div class="graph"><div class="graph-tier">${node('source','방문 자료 v1','출처 · 예시 자료')}</div><div class="graph-link">에서 가져온 ${icon('down')}</div><div class="graph-tier">${node('observation',`${state.region} · 11월 방문`,'관측값')}${node('product','판매 가능한 음료 세트','운영 조건 · 확인 필요')}</div><div class="graph-link">를 근거·조건으로 ${icon('down')}</div><div class="graph-tier">${node('recommendation','평일 음료 세트 홍보','홍보 제안',true)}</div><div class="graph-link">선택해 반영 ${icon('down')}</div><div class="graph-tier">${node('plan','11월 홍보 일정','일정 항목')}</div></div></section><section class="panel graph-explanation" aria-live="polite"><span class="badge green">${info[0]}</span><h2 style="margin-top:15px">${info[1]}</h2><p class="muted" style="font-size:14px;margin-top:12px">${info[2]}</p><dl><dt>분석 기준기간</dt><dd>2025년 1월부터 12월</dd><dt>계획 대상기간</dt><dd>2026년 10월부터 12월</dd><dt>해석 범위</dt><dd>지역의 과거 흐름을 참고하는 제안</dd></dl><div class="notice">연결이 있다는 사실만으로 인과관계나 매출 효과를 뜻하지 않아요.</div><button class="text-button" data-evidence="winter" style="margin-top:14px">추천 근거 자세히 보기 ${icon('arrow')}</button></section></div><section class="panel panel-padding" style="margin-top:22px"><h2>글로 읽는 관계</h2><ul class="relation-list"><li><b>방문 관측값 → 자료 버전</b>관측값이 어느 자료와 기준에서 왔는지 확인합니다.</li><li><b>홍보 제안 → 관측값 · 운영 조건</b>사용한 수치와 아직 확인하지 못한 상품 조건을 구분합니다.</li><li><b>일정 → 선택한 제안 → 실행 기록</b>어떤 제안을 어떻게 바꾸고 실행했는지 이어서 살펴봅니다.</li></ul></section>`;
}
function dataRows() {
  return Object.entries(regionData).flatMap(([region,data])=>Array.from({length:12},(_,i)=>({region,month:i+1,value:data[state.dataTab==='visits'?'visits':'spend'][i]}))).filter(r=>`${r.region} ${r.month}월`.includes(state.search)).map(r=>`<tr><td>${r.region}</td><td>2025년 ${r.month}월</td><td>${state.dataTab==='visits'?'지역 방문 집계':'카드 소비 집계'}</td><td>${r.value===null?'미수집':r.value}</td><td>${state.dataTab==='visits'?'만 명':'억 원'}</td><td><span class="badge ${r.value===null?'amber':''}">${r.value===null?'자료 누락':'화면용 예시'}</span></td></tr>`).join('') || '<tr><td colspan="6">검색 결과가 없습니다. 지역명 또는 월을 입력해 주세요.</td></tr>';
}
const ADMIN_KEY='jeju-design-admin-v1';
const defaultCriteria={threshold:20,source:'화면 시안 v1 · 2025년 월별 지역 방문 집계(만 명)',reason:'10월 대비 11월 예시 방문 감소를 참고하며 판매 가능 상품·휴무일을 별도로 확인',condition:'상품 판매 가능 여부·실제 예산·관광지 접근 경로 확인 필요'};
let adminState={criteria:{...defaultCriteria},registrations:[]};
try{const a=JSON.parse(localStorage.getItem(ADMIN_KEY)||'null');if(a?.criteria&&Number.isFinite(a.criteria.threshold)&&a.criteria.threshold>=0&&a.criteria.threshold<=100&&['source','reason','condition'].every(k=>typeof a.criteria[k]==='string'))adminState.criteria=a.criteria;if(Array.isArray(a?.registrations))adminState.registrations=a.registrations.filter(x=>x&&typeof x.source==='string'&&Number.isInteger(x.count)&&x.count>0);}catch{}
let csvText='',csvFilename='',csvValidated=false;
function saveAdmin(next){try{localStorage.setItem(ADMIN_KEY,JSON.stringify(next));adminState=next;return true;}catch{return false;}}
function sourceManager() {
  return `<section class="panel panel-padding source-manager"><div class="panel-heading"><div><h2>CSV 자료 등록</h2><p>운영 지원 · 파일은 현재 브라우저에서 검사하며 서버에 전송하지 않습니다.</p></div><span class="badge amber">시연</span></div><form id="csv-form"><div class="metadata-grid"><div><label class="field-label" for="csv-file">CSV 파일</label><input class="field" id="csv-file" type="file" accept=".csv,text/csv"><p class="hint">최대 1MB · 원본 내용은 저장하지 않음</p></div><div><label class="field-label" for="csv-source">자료 출처·버전</label><input class="field" id="csv-source" value="화면 시안 방문 자료 v1" maxlength="120" required></div><div><label class="field-label" for="csv-from">기준기간 시작</label><input class="field" id="csv-from" type="month" value="2025-01" required></div><div><label class="field-label" for="csv-to">기준기간 종료</label><input class="field" id="csv-to" type="month" value="2025-12" required></div><div><label class="field-label" for="csv-unit">단위</label><select class="field" id="csv-unit"><option>만 명</option><option>억 원</option></select></div><div><label class="field-label" for="csv-provider">제공자·집계 기준</label><input class="field" id="csv-provider" value="화면 시안 제작팀" maxlength="80" required><p class="hint">비교 기준: 화면 시안 제작팀 · 월별 지역 집계</p></div></div><details class="csv-format"><summary>CSV 열과 예시 형식 보기</summary><code>region_code,month,value,unit,provider,aggregation</code><p class="hint">지역 코드: 애월읍 50110253, 구좌읍 50110256 · 월: 2025-10 · 집계: 월별 지역 집계</p></details><div class="row wrap source-actions"><button type="button" class="button small" data-action="csv-sample">정상 예시 불러오기</button><button type="button" class="button small" data-action="csv-sample-invalid">누락 예시 검증</button><button type="button" class="button small" data-action="validate">선택 자료 검증</button><button type="button" class="button primary small" data-action="csv-register" disabled>검증한 예시 등록</button></div><p class="notice" id="csv-status" role="status">자료를 선택하고 메타데이터를 확인한 뒤 검증해 주세요. 비교 기준은 2025년 월별 방문 예시입니다.</p><ol class="validation-grid" id="csv-validation"><li>자료 기간 · 대기</li><li>집계 기준 · 대기</li><li>지역 코드 · 대기</li><li>누락값 · 대기</li><li>제공 기준 일관성 · 대기</li></ol></form><div class="row between criteria-toolbar"><div><h3>추천 기준·근거 관리</h3><p class="small muted">방문 감소 기준 ${escapeHTML(adminState.criteria.threshold)}% · 브라우저에 등록한 예시 ${adminState.registrations.length}개</p></div>${button('기준·근거 수정','criteria','','edit')}</div><p class="source-inline">CSV 등록과 기준 저장은 UI 시연입니다. 검증된 원자료의 운영 DB 반영과 추천 엔진은 연결되지 않았습니다.</p></section>`;
}
function parseCSV(text) {
  const rows=[];let row=[],value='',quoted=false;
  for(let i=0;i<text.length;i++){
    const c=text[i];
    if(c==='"'){if(quoted&&text[i+1]==='"'){value+='"';i++;}else quoted=!quoted;}
    else if(c===','&&!quoted){row.push(value.trim());value='';}
    else if((c==='\n'||c==='\r')&&!quoted){if(c==='\r'&&text[i+1]==='\n')i++;row.push(value.trim());if(row.some(Boolean))rows.push(row);row=[];value='';}
    else value+=c;
  }
  if(quoted)throw new Error('닫히지 않은 따옴표가 있습니다.');
  row.push(value.trim());if(row.some(Boolean))rows.push(row);
  const header=rows.shift()||[],expected=['region_code','month','value','unit','provider','aggregation'];
  if(header.length!==expected.length||expected.some((key,i)=>header[i].replace(/^\uFEFF/,'')!==key))throw new Error('필수 열 6개의 이름과 순서를 확인해 주세요.');
  if(!rows.length||rows.some(r=>r.length!==6))throw new Error('자료 행이 없거나 열 개수가 다릅니다.');
  return rows.map(r=>Object.fromEntries(expected.map((key,i)=>[key,r[i]])));
}
function invalidateCSV(){csvValidated=false;$('[data-action="csv-register"]')?.setAttribute('disabled','');if($('#csv-status'))$('#csv-status').textContent='입력이 바뀌었습니다. 다섯 검증을 다시 실행해 주세요.';if($('#csv-validation'))$('#csv-validation').innerHTML=['자료 기간','집계 기준','지역 코드','누락값','제공 기준 일관성'].map(label=>`<li>${label} · 재검증 필요</li>`).join('');}
function validateCSV() {
  if(state.role!=='service')return;
  csvValidated=false;const from=$('#csv-from').value,to=$('#csv-to').value,unit=$('#csv-unit').value,provider=$('#csv-provider').value.trim(),source=$('#csv-source').value.trim();
  let rows=[],parseError='';try{if(!csvText)throw new Error('먼저 CSV 파일 또는 예시를 선택해 주세요.');rows=parseCSV(csvText);}catch(error){parseError=error.message;}
  const exists=rows.length>0;
  const checks=[
    ['자료 기간',exists&&/^2025-\d{2}$/.test(from)&&/^2025-\d{2}$/.test(to)&&from<=to&&rows.every(r=>/^2025-(0[1-9]|1[0-2])$/.test(r.month)&&r.month>=from&&r.month<=to),'기준기간과 비교 가능 기간(2025년)이 겹치지 않거나 월 형식 불일치'],
    ['집계 기준',exists&&rows.every(r=>r.aggregation==='월별 지역 집계'&&r.unit===unit)&&unit==='만 명','월별 지역 집계·선택 단위·방문 비교 기준(만 명) 불일치'],
    ['지역 코드',exists&&rows.every(r=>['50110253','50110256'].includes(r.region_code)),'예시 대상 지역 코드에 없는 값'],
    ['누락값',exists&&!!source&&rows.every(r=>Object.values(r).every(v=>v!=='')&&Number.isFinite(Number(r.value))&&Number(r.value)>=0),'출처 또는 필수 값 누락·음수·숫자 형식 오류 · 0으로 대체하지 않음'],
    ['제공 기준 일관성',exists&&provider==='화면 시안 제작팀'&&rows.every(r=>r.provider===provider),'제공 기준 불일치 · 예시 비교 제공자와 다른 자료']
  ];
  csvValidated=checks.every(([,ok])=>ok);
  $('#csv-validation').innerHTML=checks.map(([name,ok,reason])=>`<li data-result="${ok?'pass':'fail'}"><b>${name} · ${ok?'통과':'확인 필요'}</b><span>${ok?'예시 비교 기준과 일치':reason}</span></li>`).join('');
  $('[data-action="csv-register"]').disabled=!csvValidated;
  $('#csv-status').textContent=parseError||`${csvFilename} · ${rows.length}행 · ${csvValidated?'다섯 검증을 통과했습니다. 현재 브라우저에 예시 등록을 할 수 있습니다.':'등록할 수 없습니다. 오류를 수정한 뒤 다시 검증해 주세요.'}`;
}
function loadCSVExample(invalid=false){
  $('#csv-file').value='';$('#csv-source').value='화면 시안 방문 자료 v1';$('#csv-from').value='2025-01';$('#csv-to').value='2025-12';$('#csv-unit').value='만 명';$('#csv-provider').value='화면 시안 제작팀';
  csvFilename=invalid?'누락값 포함 예시.csv':'정상 예시.csv';csvText=`region_code,month,value,unit,provider,aggregation\n50110253,2025-10,51,만 명,화면 시안 제작팀,월별 지역 집계\n50110256,2025-10,${invalid?'':'39'},만 명,화면 시안 제작팀,월별 지역 집계`;validateCSV();
}
function criteriaDialog(){
  if(state.role!=='service')return;const c=adminState.criteria;
  openDialog('추천 기준과 근거 수정','UC09 · 검증한 자료와 판단 이유를 함께 기록',`<form id="criteria-form"><p class="notice">고정 예시의 기준 편집 시연입니다. 저장해도 실제 추천 엔진이나 기존 제안 카드가 자동 변경되지 않습니다.</p><label class="field-label" for="criteria-threshold">직전 월 대비 방문 감소 기준 · %</label><input class="field" id="criteria-threshold" name="threshold" type="number" min="0" max="100" step="1" required value="${escapeHTML(c.threshold)}"><label class="field-label" for="criteria-source">분석 결과·출처·기준기간·단위</label><input class="field" id="criteria-source" name="source" required maxlength="250" value="${escapeHTML(c.source)}"><label class="field-label" for="criteria-reason">추천 판단 이유</label><textarea class="field" id="criteria-reason" name="reason" required maxlength="500">${escapeHTML(c.reason)}</textarea><label class="field-label" for="criteria-condition">적용 조건·확인이 필요한 항목</label><textarea class="field" id="criteria-condition" name="condition" required maxlength="500">${escapeHTML(c.condition)}</textarea><p id="criteria-error" class="notice error" role="alert" hidden></p><div class="dialog-actions">${button('취소','close-dialog')}<button class="button primary" type="submit">브라우저에 기준 저장</button></div></form>`);
}
function optionalFeature(label){return `<section class="optional-scope optional-feature" id="${label.includes('UC06-1')?'optional-explanation':'optional-report'}"><span class="badge amber">선택 기능 · 도입 검토 중</span><button class="button small" disabled>${label}</button><p>AI 사용 여부와 검증 기준을 정한 뒤 제공할 예정 여부를 결정합니다. 현재는 실행할 수 없습니다.</p></section>`;}

function dataView() {
  return `${heading(state.role==='service'?'자료와 기준을 관리하는 공간':'추천에 사용한 자료를 살펴보세요','시안에 포함된 예시 자료이며, 실제 통계와 연결되지 않았습니다.',button('예시 CSV 내려받기','export','','download'))}${state.role==='service'?sourceManager():''}<div class="source-tabs" aria-label="자료 종류"><button data-tab="visits" class="${state.dataTab==='visits'?'selected':''}" aria-pressed="${state.dataTab==='visits'}">지역 방문 자료</button><button data-tab="spend" class="${state.dataTab==='spend'?'selected':''}" aria-pressed="${state.dataTab==='spend'}">카드 소비 자료</button></div><div class="toolbar"><label class="search">${icon('search')}<input class="field" id="data-search" placeholder="지역명 또는 월로 검색" aria-label="자료 검색" value="${escapeHTML(state.search)}"></label><span class="badge">예시 자료 v1 · 2025년</span></div><section class="panel"><div class="table-wrap"><table><thead><tr><th scope="col">지역</th><th scope="col">기준기간</th><th scope="col">지표</th><th scope="col">값</th><th scope="col">단위</th><th scope="col">자료 상태</th></tr></thead><tbody id="data-rows">${dataRows()}</tbody></table></div><p class="table-caption">출처: 화면 시안 내 예시 자료 v1 · 다운로드 파일에도 예시 자료임을 표시합니다.</p></section>`;
}
function login() {
  return `<main class="login-page" id="main" tabindex="-1">
    <section class="login-story"><a class="brand" href="#overview">${logo()}<span><strong>제주 마케팅 캘린더</strong><small>다음 계절을 준비하는 공간</small></span></a>
      <div class="login-story-copy"><span class="login-kicker">우리 가게의 다음 계절</span><h1>준비한 오늘이<br>다음 방문으로.</h1><p>지역의 흐름을 읽고, 우리 가게에 맞는<br>3개월 홍보 계획을 이어가세요.</p></div>
      <div class="season-art" aria-hidden="true"><div class="season-arch"><b>10</b><span>가을을 준비하고</span></div><div class="season-arch"><b>11</b><span>겨울을 맞이하고</span></div><div class="season-arch"><b>12</b><span>다음 방문을 잇는</span></div></div>
      <p class="login-story-foot">제주에서 시작하는, 작고 꾸준한 계획</p>
    </section>
    <section class="login-form" aria-labelledby="login-title"><span class="badge green">제주 마케팅 캘린더</span><h2 id="login-title">다시 만나 반가워요</h2><p>로그인하고 준비하던 계획을 이어가세요.</p>
      <div class="login-roles" aria-label="로그인 역할">${['business','service'].map(role=>`<button type="button" data-role="${role}" class="${roleChoice===role?'selected':''}" aria-pressed="${roleChoice===role}">${icon(role==='business'?'shop':'database')}${role==='business'?'사업체 운영자':'서비스 운영자'}</button>`).join('')}</div>
      <form id="login-form" novalidate>
        <label class="field-label" for="login-email">이메일</label><input class="field" id="login-email" name="email" type="email" autocomplete="username" placeholder="이메일 주소를 입력하세요" required maxlength="254" aria-describedby="login-error login-demo-note">
        <label class="field-label" for="login-password">비밀번호</label><div class="password-field"><input class="field" id="login-password" name="password" type="password" autocomplete="current-password" placeholder="비밀번호를 입력하세요" required maxlength="128" aria-describedby="login-error login-demo-note"><button type="button" class="password-toggle" data-action="toggle-password" aria-label="비밀번호 보기" aria-pressed="false">${icon('eye')}</button></div>
        <p id="login-error" class="notice error" role="alert" hidden></p>
        <button class="button primary full login-submit" type="submit">로그인 ${icon('arrow')}</button>
      </form>
      <div class="demo-account"><div><b>체험용 계정</b><span>${demoAccounts[roleChoice]}</span></div><button type="button" class="text-button" data-action="fill-demo">체험 계정 채우기</button></div>
      <p class="login-demo-note" id="login-demo-note">화면 시안입니다. 실제 계정 대신 체험 계정을 사용해 주세요. 입력한 비밀번호는 저장하거나 전송하지 않습니다.</p>
      <div class="login-divider"><span>먼저 살펴보고 싶다면</span></div>
      ${button('로그인 없이 시안 둘러보기','enter','full secondary')}
      <p class="login-footnote">예시 자료로 지역 분석부터 홍보 일정까지 살펴보세요.</p>
    </section></main>`;
}
function loginError(message,field) {
  const error=$('#login-error');error.textContent=message;error.hidden=false;
  field.setAttribute('aria-invalid','true');field.focus();
}
function startDemoLogin(form) {
  const email=$('#login-email'),password=$('#login-password');
  if(!email.value.trim()||!email.validity.valid){loginError('올바른 이메일 주소를 입력해 주세요.',email);return;}
  if(!password.value){loginError('비밀번호를 입력해 주세요.',password);return;}
  if(email.value.trim().toLowerCase()!==demoAccounts[roleChoice]||password.value!==demoPassword){loginError('체험용 계정 정보를 확인해 주세요. 아래 버튼으로 이메일과 비밀번호를 채울 수 있어요.',password);return;}
  demoSessionRole=roleChoice;state.role=roleChoice;
  let sessionSaved=true;
  try {sessionStorage.setItem(DEMO_SESSION_KEY,demoSessionRole);} catch {sessionSaved=false;}
  form.reset();persist();navigate('overview');
  notify(sessionSaved?'체험 계정으로 로그인했습니다.':'체험 화면을 열었습니다. 로그인 상태는 새로고침 후 유지되지 않습니다.');
}
function render() {
  const hash=location.hash.slice(1);currentView=titles[hash]?hash:(demoSessionRole?'overview':'login');
  document.title=`${titles[currentView]} · 제주 마케팅 캘린더 시안`;
  const views={overview,analysis,recommendations,calendar,records,ontology,data:dataView};
  closeMobileMenu();
  app.innerHTML=currentView==='login'?login():shell(views[currentView]());
  applySidebarLayout();
}
function openDialog(title,subtitle,body,drawer=false) {
  if(!dialog.open) opener=document.activeElement;
  dialog.className=drawer?'drawer':'';
  dialog.innerHTML=`<header class="dialog-header"><div><h2 id="dialog-title">${title}</h2>${subtitle?`<p>${subtitle}</p>`:''}</div><button class="icon-button" data-action="close-dialog" aria-label="닫기">${icon('close')}</button></header><div class="dialog-body">${body}</div>`;
  if(!dialog.open)dialog.showModal();
  else $('[data-action="close-dialog"]',dialog).focus();
  document.body.style.overflow='hidden';
}
function closeDialog() {if(dialog.open)dialog.close();}
dialog.addEventListener('close',()=>{
  document.body.style.overflow='';
  let target=opener;
  if(target && !target.isConnected){
    const attribute=target.getAttributeNames().find(name=>name.startsWith('data-'));
    target=attribute?$(`[${attribute}="${CSS.escape(target.getAttribute(attribute))}"]`,app):null;
  }
  (target||$('#main'))?.focus({preventScroll:true});
});
function sourceDialog() {
  openDialog('자료의 출처와 기준','화면 시안에 포함된 예시 자료',`<span class="badge amber">실제 통계 아님</span><h3 style="margin-top:18px">2025년 월별 방문·카드 소비 예시</h3><ul class="relation-list"><li><b>제공 범위</b>애월읍·구좌읍, 2025년 1월부터 12월</li><li><b>방문 집계</b>단위 만 명 · 12개월 예시 값 · 개별 가게의 고객 수와 구분</li><li><b>카드 소비</b>단위 억 원 · 11월 값은 미수집으로 처리</li><li><b>자료 버전</b>화면 시안 v1 · 화면 배치와 동작 검토를 위해 작성</li></ul><p class="notice">실제 적용에는 원본 CSV, 이용 조건, 집계 기준 확인이 필요합니다. 과거 예시 수치를 미래 전망으로 사용하지 않습니다.</p><div class="dialog-actions">${button('확인','close-dialog','primary')}</div>`);
}
function evidenceDialog(id) {
  const r=recs.find(r=>r.id===id);if(!r)return;
  openDialog('이 제안의 근거','자료에서 일정까지 연결해 살펴봐요',`<span class="badge amber">예시 추천 · ${r.month}월</span><h3 class="evidence-title">${r.title}</h3><div class="evidence-row"><span class="evidence-number">1</span><div><h3>지역의 과거 흐름</h3><p>${r.reason}</p><span class="badge">${state.region} · 2025년 예시 방문 자료</span></div></div><div class="evidence-row"><span class="evidence-number">2</span><div><h3>확인할 운영 조건</h3><p>실제 판매 상품, 휴무일, 홍보에 쓸 수 있는 예산을 확인하세요. 이 조건은 아직 입력되지 않았습니다.</p><span class="badge amber">운영자 확인 필요</span></div></div><div class="evidence-row"><span class="evidence-number">3</span><div><h3>계획으로 옮길 행동</h3><p>${r.task}</p><span class="badge green">2026년 ${r.month}월 계획 후보</span></div></div><div class="notice">추천은 선택할 수 있는 아이디어입니다. 매출이나 방문 증가가 확인된 결과는 아닙니다.</div><div class="dialog-actions">${button('자료 기준','source')}<button class="button primary" data-rec="${r.id}">${icon('plus')}일정에 담기</button></div><button class="text-button" data-action="ontology" style="margin-top:14px">온톨로지 관계 화면 보기 ${icon('arrow')}</button>`,true);
}
function planDialog({id=null,recId=null,month=10}={}) {
  const old=state.plans.find(p=>p.id===id),rec=recs.find(r=>r.id===recId);
  if(rec){const existing=state.plans.find(p=>p.recId===`${state.region}:${rec.id}`);if(existing){notify('이미 담은 제안입니다. 저장된 계획을 수정할 수 있어요.');return planDialog({id:existing.id});}}
  const p=old || {title:rec?.title||'',date:rec?.date||`2026-${month}-15`,channel:rec?.channel||'인스타그램',memo:rec?.task||'',region:state.region};
  openDialog(old?'계획 수정':'일정에 담을 계획','2026년 10월부터 12월 · 이 브라우저에만 저장',`<form id="plan-form" data-id="${escapeHTML(old?.id||'')}" data-rec-id="${rec?.id||''}"><label class="field-label" for="plan-title">어떤 활동을 할까요?</label><input class="field" id="plan-title" name="title" required maxlength="70" value="${escapeHTML(p.title)}" placeholder="예: 겨울 메뉴 사진 촬영"><div class="form-row"><div><label class="field-label" for="plan-date" style="margin-top:18px">예정 날짜</label><input class="field" id="plan-date" name="date" type="date" min="2026-10-01" max="2026-12-31" required value="${p.date}"></div><div><label class="field-label" for="plan-channel" style="margin-top:18px">홍보 채널</label><select class="field" id="plan-channel" name="channel">${['인스타그램','매장 안내','블로그','기타'].map(c=>`<option ${c===p.channel?'selected':''}>${c}</option>`).join('')}</select></div></div><label class="field-label" for="plan-memo">준비할 내용</label><textarea class="field" id="plan-memo" name="memo" maxlength="500" placeholder="상품, 준비물, 확인할 조건을 적어보세요.">${escapeHTML(p.memo)}</textarea><p class="hint">지역: ${escapeHTML(p.region)} · 상품 판매 가능 여부와 휴무일은 직접 확인해 주세요.</p><p id="plan-error" class="notice error" hidden></p><div class="dialog-actions">${old?`<button type="button" class="text-button danger" data-delete="${escapeHTML(old.id)}">계획 삭제</button>`:''}<button type="button" class="button" data-action="close-dialog">취소</button><button class="button primary" type="submit">${icon('check')}이 브라우저에 저장</button></div></form>`);
}
function recordDialog(id) {
  const p=state.plans.find(p=>p.id===id);if(!p)return;
  const record=state.records[id]||{status:'실행함',date:p.date,clicks:null,bookings:null,coupons:null,planningMinutes:null,adopted:'unknown',activity:'',note:''};
  const count=(name,label)=>`<div><label class="field-label" for="record-${name}">${label}</label><input class="field" id="record-${name}" name="${name}" type="number" min="0" max="99999999" step="1" placeholder="미확보" value="${escapeHTML(record[name]??'')}"></div>`;
  openDialog('실행 기록 남기기',escapeHTML(p.title),`<form id="record-form" data-id="${escapeHTML(id)}"><div class="form-row"><div><label class="field-label" for="record-minutes">계획 소요 시간 · 분</label><input class="field" id="record-minutes" name="planningMinutes" type="number" min="0" max="99999999" step="1" placeholder="미확보" value="${escapeHTML(record.planningMinutes??'')}"></div><div><label class="field-label" for="record-adopted">추천 채택 여부</label><select class="field" id="record-adopted" name="adopted">${[['unknown','미확인'],['yes','채택'],['no','미채택']].map(([v,l])=>`<option value="${v}" ${record.adopted===v?'selected':''}>${l}</option>`).join('')}</select></div></div><label class="field-label" for="record-status">실행 여부</label><select class="field" id="record-status" name="status">${['실행함','미실행','확인 중'].map(s=>`<option ${record.status===s?'selected':''}>${s}</option>`).join('')}</select><label class="field-label" for="record-date">실제 실행 날짜</label><input class="field" name="date" id="record-date" type="date" value="${escapeHTML(record.date||'')}" ${record.status==='실행함'?'required':''}><label class="field-label" for="record-activity">실제로 실행한 활동 · 선택한 내용</label><textarea class="field" id="record-activity" name="activity" maxlength="500" placeholder="예: 추천의 음료 세트를 선택하고 매장 안내물을 게시함">${escapeHTML(record.activity||'')}</textarea><div class="form-row record-counts">${count('clicks','확인한 클릭 수')}${count('bookings','확인한 예약 수')}${count('coupons','확인한 쿠폰 사용 수')}</div><p class="hint">빈칸은 미확보, 숫자 0은 실제로 확인한 0건입니다. 광고·예약·쿠폰 발행은 대신 실행하지 않습니다.</p><label class="field-label" for="record-note">결과와 수집 방법</label><textarea class="field" id="record-note" name="note" maxlength="500" placeholder="수치의 출처와 다음에 바꿀 점을 적어주세요.">${escapeHTML(record.note||'')}</textarea><p id="record-error" class="notice error" role="alert" hidden></p><div class="dialog-actions"><button type="button" class="button" data-action="close-dialog">취소</button><button class="button primary" type="submit">기록 저장</button></div></form>`);
}
let agentDraft = null;
function agentDialog() {
  agentDraft=null;
  openDialog('AI 계획 도우미','우리 가게에 맞는 3개월을 준비해요',`<div class="notice green">검토 중인 추가 흐름의 시연입니다. 정해진 예시이며 실제 AI 모델·필수 구현 기능이 아닙니다.</div><div class="agent-steps"><span class="done">1. 운영 조건</span><span>2. 초안 검토</span><span>3. 선택해 적용</span></div><form id="agent-form"><label class="field-label" for="agent-goal">어떤 목표로 준비할까요?</label><select class="field" id="agent-goal" name="goal"><option value="prepare">겨울 비수기 미리 준비하기</option><option value="weekday">평일 방문 계기 만들기</option></select><label class="field-label" for="agent-product">홍보할 상품</label><input class="field" id="agent-product" name="product" maxlength="35" placeholder="예: 귤차와 구움과자 세트"><p class="hint">입력하지 않으면 상품 확인이 필요한 초안으로 남겨요.</p><label class="field-label" for="agent-day">매주 쉬는 날</label><select class="field" id="agent-day" name="closed"><option value="unknown">아직 확인하지 않음</option><option value="none">정기 휴무 없음</option>${['일','월','화','수','목','금','토'].map((d,i)=>`<option value="${i}">${d}요일</option>`).join('')}</select><div class="notice" style="margin-top:20px">자료 기준: ${state.region} 2025년 예시 방문 자료<br>계획 대상: 2026년 10월부터 12월<br>11월 소비 자료는 누락되어 추천 근거에서 제외합니다.</div><div class="dialog-actions"><button type="submit" class="button primary full">${icon('spark')}예시 초안 만들기</button></div></form>`,true);
}
function agentResult(form) {
  const product=String(form.get('product')||'').trim(),closed=form.get('closed'),goal=form.get('goal');
  const selected=goal==='weekday'?recs.filter(r=>r.month!==10):recs;
  agentDraft=selected.map(r=>{
    let date=r.date,shifted=false;
    if(closed!=='unknown'&&closed!=='none') {
      const d=new Date(`${date}T12:00:00`);if(d.getDay()===Number(closed)){d.setDate(d.getDate()+1);date=`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;shifted=true;}
    }
    return {...r,date,title:product&&r.month===11?`${product} 평일 안내`:r.title,shifted,region:state.region,product};
  });
  const shifted=agentDraft.some(d=>d.shifted);
  openDialog('AI 계획 도우미','예시 초안을 확인하고 적용할 항목을 골라요',`<span class="badge amber">검토 중 · 고정 예시 시연 · 실제 AI 미연결</span><div class="agent-steps"><span class="done">1. 운영 조건</span><span class="done">2. 초안 검토</span><span>3. 선택해 적용</span></div><div class="agent-status">${icon('check')} ${shifted?'선택한 휴무일과 겹친 일정을 다음 날로 옮겼어요.':'입력한 조건을 예시 초안에 반영했어요.'}</div><h3 class="agent-result-title">${agentDraft.length}개 계획 후보</h3><p class="small muted">${escapeHTML(state.region)} · ${goal==='weekday'?'평일 방문 계기 만들기':'겨울 비수기 미리 준비하기'}</p><form id="agent-apply-form">${agentDraft.map(r=>{const exists=state.plans.some(p=>p.recId===`${r.region}:${r.id}`);return `<label class="check-option"><input type="checkbox" name="selected" value="${r.id}" ${exists?'disabled':'checked'}><span><strong>${escapeHTML(r.title)}</strong><p>${r.date} · ${r.channel}${r.shifted?' · 휴무일 조정':''}</p><p>${exists?'이미 일정에 담긴 제안입니다.':r.task}</p></span></label>`;}).join('')}<div class="notice" style="margin-top:18px">${!product?'상품: 판매 가능한 상품 확인 필요<br>':''}${closed==='unknown'?'휴무일: 확인 필요<br>':''}예산·판매 가능 여부·기존 일정과의 시간 충돌은 직접 확인해 주세요.</div><div class="dialog-actions">${button('조건 다시 입력','agent')}<button type="submit" class="button primary">선택한 계획 담기</button></div></form>`,true);
}
function reportDialog() {
  const entries=state.plans.filter(p=>state.records[p.id]);
  const executed=entries.filter(p=>state.records[p.id].status==='실행함');
  const minutes=entries.map(p=>state.records[p.id].planningMinutes).filter(n=>n!==null);
  const display=(n,unit='건')=>n===null||n===undefined?'미확보':`${n}${unit}`;
  openDialog('사용 결과 요약','UC05 · 저장한 계획과 입력한 실제 활동 비교',`<span class="badge green">필수 기능 · 입력 기록 요약</span><div class="summary-stats"><span>계획 <b>${state.plans.length}개</b></span><span>기록 <b>${entries.length}개</b></span><span>실행함 <b>${executed.length}개</b></span><span>계획 소요 시간 <b>${minutes.length?minutes.reduce((a,b)=>a+b,0)+'분':'미확보'}</b><small> / 입력 ${minutes.length}개 기준</small></span></div><div class="table-wrap"><table><thead><tr><th>계획</th><th>실제 선택·활동</th><th>확보한 결과</th></tr></thead><tbody>${state.plans.map(p=>{const r=state.records[p.id];return `<tr><td><b>${escapeHTML(p.title)}</b><br>${p.date}<br>${escapeHTML(p.channel)}</td><td>${r?`${escapeHTML(r.status)} · ${r.date||'날짜 미기록'}<br>채택: ${({yes:'채택',no:'미채택',unknown:'미확인'})[r.adopted]}<br>${escapeHTML(r.activity||'활동 미기록')}`:'기록 없음'}</td><td>${r?`계획 ${display(r.planningMinutes,'분')}<br>클릭 ${display(r.clicks)} · 예약 ${display(r.bookings)}<br>쿠폰 사용 ${display(r.coupons)}`:'미확보'}</td></tr>`;}).join('')||'<tr><td colspan="3">저장한 계획이 없습니다. 3개월 일정에서 계획을 작성하세요.</td></tr>'}</tbody></table></div><p class="notice" style="margin-top:18px">미확보 값을 0이나 실행 실패로 해석하지 않습니다. 매출 증가·인과 효과를 추정하지 않습니다.</p><p class="source-inline">F04의 AI 보고서 초안은 선택·검토 중이며 위 필수 요약과 별개입니다.</p><div class="dialog-actions">${button('닫기','close-dialog','primary')}</div>`);
}
function helpDialog() {
  openDialog('화면 시안 사용 안내','제주 비수기 상권 마케팅 캘린더',`<p>첨부해 주신 화면을 바탕으로 다시 설계한 클릭 가능한 시안입니다.</p><ul class="relation-list"><li><b>홍보 제안 → 일정 → 실행 기록</b>제안을 담고 수정하거나, 일정에서 직접 계획을 작성할 수 있어요.</li><li><b>근거 연결</b>예시 추천과 자료·상품·계획의 관계를 확인할 수 있어요.</li><li><b>AI 계획 도우미</b>목표·상품·휴무일을 입력해 정해진 예시 초안을 체험해요. 실제 모델 호출은 없습니다.</li><li><b>저장 범위</b>계획과 기록은 현재 브라우저에만 저장됩니다. 실제 사업체 정보는 입력하지 마세요.</li><li><b>역할 전환</b>왼쪽 아래에서 두 역할의 화면을 체험할 수 있어요. 실제 인증 기능은 아닙니다.</li></ul><div class="dialog-actions">${button('시안 데이터 초기화','reset')} ${button('확인','close-dialog','primary')}</div>`);
}
function exportCSV() {
  const quote=v=>`"${String(v).replace(/"/g,'""')}"`;
  const rows=[['자료구분','지역','기준월','지표','값','단위','상태']];
  for(const [region,d] of Object.entries(regionData)) d[state.dataTab==='visits'?'visits':'spend'].forEach((v,i)=>rows.push(['화면 검토용 예시',region,`2025-${String(i+1).padStart(2,'0')}`,state.dataTab==='visits'?'지역 방문 집계':'카드 소비',v??'',state.dataTab==='visits'?'만 명':'억 원',v===null?'미수집':'예시']));
  const blob=new Blob(['\uFEFF'+rows.map(r=>r.map(quote).join(',')).join('\r\n')],{type:'text/csv;charset=utf-8;'});
  const url=URL.createObjectURL(blob),a=document.createElement('a');a.href=url;a.download=`제주_화면용_예시_${state.dataTab}.csv`;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);notify('예시 자료 CSV를 내려받습니다.');
}
document.addEventListener('click',event=>{
  const target=event.target.closest('button,a');if(!target)return;
  if(target.matches('.skip')){event.preventDefault();$('#main')?.focus();return;}
  if(target.matches('.sidebar a') && target.hash===location.hash){event.preventDefault();render();$('#main')?.focus();return;}
  if(target.dataset.analysisMode){state.analysisMode=target.dataset.analysisMode;render();$(`[data-analysis-mode="${state.analysisMode}"]`).focus();return;}
  if(target.dataset.regionMap){state.region=target.dataset.regionMap;persist();render();$(`[data-region-map="${state.region}"]`).focus();return;}
  if(target.dataset.metric){state.metric=target.dataset.metric;render();$(`[data-metric="${state.metric}"]`).focus();return;}
  if(target.dataset.tab){state.dataTab=target.dataset.tab;render();$(`[data-tab="${state.dataTab}"]`).focus();return;}
  if(target.dataset.evidence){evidenceDialog(target.dataset.evidence);return;}
  if(target.dataset.rec){planDialog({recId:target.dataset.rec});return;}
  if(target.dataset.edit){planDialog({id:target.dataset.edit});return;}
  if(target.dataset.record){recordDialog(target.dataset.record);return;}
  if(target.dataset.month){planDialog({month:Number(target.dataset.month)});return;}
  if(target.dataset.node){state.ontologyNode=target.dataset.node;render();$(`[data-node="${state.ontologyNode}"]`)?.focus();return;}
  if(target.dataset.role){roleChoice=target.dataset.role;render();$(`[data-role="${roleChoice}"]`).focus();return;}
  if(target.dataset.delete){const id=target.dataset.delete;openDialog('이 계획을 삭제할까요?','연결된 예시 실행 기록도 함께 삭제됩니다.',`<p>삭제할 계획을 확인한 뒤 진행해 주세요.</p><div class="dialog-actions">${button('취소','close-dialog')}<button class="button primary" data-confirm-delete="${escapeHTML(id)}">삭제하기</button></div>`);return;}
  if(target.dataset.confirmDelete){const id=target.dataset.confirmDelete;state.plans=state.plans.filter(p=>p.id!==id);delete state.records[id];const ok=persist();closeDialog();render();notify(ok?'계획을 삭제했습니다.':'현재 화면에서 삭제했습니다. 브라우저 저장은 실패했습니다.');return;}
  switch(target.dataset.action){
    case 'agent':agentDialog();break;
    case 'source':sourceDialog();break;
    case 'new-plan':planDialog();break;
    case 'close-dialog':closeDialog();break;
    case 'ontology':navigate('ontology');break;
    case 'help':helpDialog();break;
    case 'report':reportDialog();break;
    case 'roles':roleChoice=state.role;navigate('login');break;
    case 'enter':demoSessionRole=null;try{sessionStorage.removeItem(DEMO_SESSION_KEY);}catch{}state.role=roleChoice;persist();navigate('overview');break;
    case 'fill-demo':$('#login-email').value=demoAccounts[roleChoice];$('#login-password').value=demoPassword;$('#login-error').hidden=true;$('#login-email').removeAttribute('aria-invalid');$('#login-password').removeAttribute('aria-invalid');$('#login-form button[type="submit"]').focus();break;
    case 'toggle-password':{const field=$('#login-password'),visible=field.type==='password';field.type=visible?'text':'password';target.setAttribute('aria-pressed',String(visible));target.setAttribute('aria-label',visible?'비밀번호 숨기기':'비밀번호 보기');break;}
    case 'logout':demoSessionRole=null;try{sessionStorage.removeItem(DEMO_SESSION_KEY);}catch{}roleChoice=state.role;navigate('login');notify('로그아웃했습니다. 저장한 계획은 그대로 남아 있어요.');break;
    case 'sidebar-toggle':sidebarLayout.collapsed=!sidebarLayout.collapsed;applySidebarLayout();saveSidebarLayout();break;
    case 'sidebar-reset':sidebarLayout.width=248;sidebarLayout.collapsed=false;applySidebarLayout();saveSidebarLayout();notify('메뉴 너비를 초기화했습니다.');break;
    case 'menu':$('.sidebar').classList.add('open');$('.mobile-scrim').classList.add('open');target.setAttribute('aria-expanded','true');document.documentElement.classList.add('menu-open');$('.workspace').inert=true;$('.sidebar .nav-link').focus();break;
    case 'close-menu':closeMobileMenu(true);break;
    case 'export':exportCSV();break;
    case 'validate':validateCSV();break;
    case 'csv-sample':if(state.role==='service')loadCSVExample();break;
    case 'csv-sample-invalid':if(state.role==='service')loadCSVExample(true);break;
    case 'csv-register':if(state.role==='service'&&csvValidated){validateCSV();if(!csvValidated)break;const next={...adminState,registrations:[...adminState.registrations,{source:$('#csv-source').value.trim(),count:parseCSV(csvText).length}]};const ok=saveAdmin(next);$('#csv-status').textContent=ok?'현재 브라우저에 예시 등록했습니다. 원본 CSV를 서버에 전송하거나 운영 자료로 반영하지 않았습니다.':'브라우저 저장에 실패했습니다. 입력값을 유지했으니 다시 등록해 주세요.';if(ok){csvValidated=false;target.disabled=true;}}break;
    case 'criteria':criteriaDialog();break;
    case 'reset':openDialog('시안 데이터를 초기화할까요?','이 브라우저에서 만든 계획과 기록이 초기 예시로 바뀝니다.',`<div class="dialog-actions">${button('취소','close-dialog')}${button('초기화','confirm-reset','primary')}</div>`);break;
    case 'confirm-reset':state.plans=initialPlans();state.records={};const ok=persist();closeDialog();render();notify(ok?'처음의 예시 데이터로 돌아왔습니다.':'현재 화면을 초기화했습니다. 브라우저 저장은 실패했습니다.');break;
  }
});
document.addEventListener('change',async event=>{
  if(event.target.id==='analysis-year'){state.analysisYear=event.target.value;render();$('#analysis-year').focus();}
  if(event.target.closest('#csv-form'))invalidateCSV();
  if(event.target.id==='csv-file'){
    const file=event.target.files[0];csvText='';csvFilename='';if(!file)return;
    if(!/\.csv$/i.test(file.name)||file.size>1024*1024){$('#csv-status').textContent='1MB 이하의 CSV 파일을 선택해 주세요.';return;}
    const validate=$('[data-action="validate"]');validate.disabled=true;
    try{csvText=await file.text();csvFilename=file.name;$('#csv-status').textContent=`${file.name} 선택됨 · 메타데이터를 확인하고 검증해 주세요.`;}
    catch{$('#csv-status').textContent='파일을 읽지 못했습니다. 다시 선택해 주세요.';}
    finally{validate.disabled=false;}
  }
  if(event.target.id==='region'){state.region=event.target.value;persist();render();$('#region').focus();notify(`${state.region} 예시 자료를 표시합니다.`);}
  if(event.target.id==='record-status'){$('#record-date').required=event.target.value==='실행함';if(event.target.value!=='실행함')$('#record-date').value='';}
});
document.addEventListener('input',event=>{
  if(event.target.closest('#csv-form'))invalidateCSV();
  if(event.target.id==='data-search'){state.search=event.target.value;$('#data-rows').innerHTML=dataRows();}
  if(['login-email','login-password'].includes(event.target.id)){$('#login-error').hidden=true;event.target.removeAttribute('aria-invalid');}
});
document.addEventListener('keydown',event=>{
  if(event.target.id==='sidebar-resizer' && ['ArrowLeft','ArrowRight','Home','End'].includes(event.key)){
    event.preventDefault();sidebarLayout.width=event.key==='Home'?216:event.key==='End'?360:Math.min(360,Math.max(216,sidebarLayout.width+(event.key==='ArrowRight'?10:-10)));applySidebarLayout();saveSidebarLayout();return;
  }
  if(!dialog.open && $('.sidebar.open')){
    if(event.key==='Escape')closeMobileMenu(true);
    if(event.key==='Tab'){
      const items=[...$('.sidebar').querySelectorAll('a[href],button')].filter(el=>el.getClientRects().length),first=items[0],last=items.at(-1);
      if(event.shiftKey && document.activeElement===first){event.preventDefault();last.focus();}
      else if(!event.shiftKey && document.activeElement===last){event.preventDefault();first.focus();}
    }
  }
});
let sidebarDrag=null;
document.addEventListener('pointerdown',event=>{
  const handle=event.target.closest('#sidebar-resizer');
  if(!handle||event.button!==0||sidebarLayout.collapsed)return;
  sidebarDrag={id:event.pointerId,x:event.clientX,width:sidebarLayout.width};handle.setPointerCapture(event.pointerId);handle.focus();document.documentElement.classList.add('sidebar-resizing');event.preventDefault();
});
document.addEventListener('pointermove',event=>{
  if(!sidebarDrag||event.pointerId!==sidebarDrag.id)return;
  sidebarLayout.width=Math.round(Math.min(360,Math.max(216,sidebarDrag.width+event.clientX-sidebarDrag.x)));applySidebarLayout();
});
function finishSidebarDrag(event) {
  if(!sidebarDrag||event.pointerId!==sidebarDrag.id)return;
  if(event.type==='pointercancel')sidebarLayout.width=sidebarDrag.width;
  sidebarDrag=null;document.documentElement.classList.remove('sidebar-resizing');applySidebarLayout();
  if(event.type==='pointerup')saveSidebarLayout();
}
document.addEventListener('pointerup',finishSidebarDrag);
document.addEventListener('pointercancel',finishSidebarDrag);
window.matchMedia('(max-width:767px)').addEventListener('change',()=>closeMobileMenu());
document.addEventListener('submit',event=>{
  const form=event.target;if(form.id==='login-form'){event.preventDefault();startDemoLogin(form);return;}if(!['plan-form','record-form','agent-form','agent-apply-form','criteria-form'].includes(form.id))return;
  event.preventDefault();const data=new FormData(form);
  if(form.id==='criteria-form'){
    if(state.role!=='service')return;
    const criteria={threshold:Number(data.get('threshold')),source:String(data.get('source')).trim(),reason:String(data.get('reason')).trim(),condition:String(data.get('condition')).trim()};
    if(!criteria.source||!criteria.reason||!criteria.condition){$('#criteria-error').hidden=false;$('#criteria-error').textContent='출처, 판단 이유, 적용 조건을 모두 입력해 주세요.';return;}
    if(!saveAdmin({...adminState,criteria})){$('#criteria-error').hidden=false;$('#criteria-error').textContent='저장에 실패했습니다. 입력값을 확인하고 다시 저장해 주세요.';return;}
    closeDialog();render();notify('추천 기준과 근거를 현재 브라우저에 저장했습니다.');return;
  }
  if(form.id==='plan-form'){
    const title=String(data.get('title')).trim(),date=String(data.get('date'));
    if(!title){$('#plan-error').hidden=false;$('#plan-error').textContent='계획 이름을 입력해 주세요.';$('#plan-title').focus();return;}
    if(!/^2026-(10|11|12)-\d{2}$/.test(date))return;
    const id=form.dataset.id,old=state.plans.find(p=>p.id===id),recId=form.dataset.recId;
    const plan={id:old?.id||`plan-${Date.now()}-${Math.random().toString(36).slice(2,7)}`,title,date,channel:String(data.get('channel')),memo:String(data.get('memo')).trim(),region:old?.region||state.region,recId:old?.recId||(recId?`${state.region}:${recId}`:null)};
    const previousPlans=state.plans.slice();
    if(old)state.plans=state.plans.map(p=>p.id===id?plan:p);else state.plans.push(plan);
    const ok=persist();if(!ok){state.plans=previousPlans;$('#plan-error').hidden=false;$('#plan-error').setAttribute('role','alert');$('#plan-error').textContent='저장에 실패했습니다. 입력값을 유지했으니 다시 저장해 주세요.';return;}closeDialog();render();notify('계획을 이 브라우저에 저장했습니다.');
  }
  if(form.id==='record-form'){
    const id=form.dataset.id;if(!state.plans.some(p=>p.id===id))return;
    const previousRecord=state.records[id];
    state.records[id]={planningMinutes:data.get('planningMinutes')===''?null:Number(data.get('planningMinutes')),adopted:String(data.get('adopted')),activity:String(data.get('activity')).trim(),coupons:data.get('coupons')===''?null:Number(data.get('coupons')),status:String(data.get('status')),date:String(data.get('date')),clicks:data.get('clicks')===''?null:Number(data.get('clicks')),bookings:data.get('bookings')===''?null:Number(data.get('bookings')),note:String(data.get('note')).trim()};
    const ok=persist();if(!ok){if(previousRecord)state.records[id]=previousRecord;else delete state.records[id];$('#record-error').hidden=false;$('#record-error').textContent='저장에 실패했습니다. 입력값을 유지했으니 다시 저장해 주세요.';return;}closeDialog();render();notify('실행 기록을 이 브라우저에 저장했습니다.');
  }
  if(form.id==='agent-form')agentResult(data);
  if(form.id==='agent-apply-form'){
    const ids=data.getAll('selected');if(!ids.length){notify('적용할 계획을 하나 이상 선택해 주세요.');return;}
    let added=0;for(const r of agentDraft||[]){if(!ids.includes(r.id)||state.plans.some(p=>p.recId===`${r.region}:${r.id}`))continue;state.plans.push({id:`agent-${Date.now()}-${r.id}`,title:r.title,date:r.date,channel:r.channel,memo:r.task,region:r.region,recId:`${r.region}:${r.id}`});added++;}
    const ok=persist();closeDialog();navigate('calendar');notify(ok?`${added}개 예시 계획을 이 브라우저에 저장했습니다.`:'화면에 반영했지만 브라우저 저장은 실패했습니다.');
  }
});
window.addEventListener('hashchange',()=>{closeDialog();render();window.scrollTo(0,0);$('#main')?.focus({preventScroll:true});});
render();
