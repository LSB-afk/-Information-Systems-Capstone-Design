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
  people:'<circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M16 5a3 3 0 0 1 0 6m2 4a5 5 0 0 1 3 4v2"/>',
  edit:'<path d="m15 4 5 5M4 20l5-1L21 7a2 2 0 0 0-5-5L4 14l-1 7M12 21h9"/>',
  book:'<path d="M3 4h6a3 3 0 0 1 3 3v14a3 3 0 0 0-3-3H3V4Zm18 0h-6a3 3 0 0 0-3 3m0 14a3 3 0 0 1 3-3h6V4"/>',
  lock:'<rect x="5" y="10" width="14" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3m-4 5v2"/>'
};
const icon = name => `<svg class="icon" viewBox="0 0 24 24" aria-hidden="true">${icons[name] || icons.grid}</svg>`;
const logo = () => '<svg class="logo" viewBox="0 0 40 40" aria-hidden="true"><rect width="40" height="40" rx="12" fill="#eaf3ec"/><path d="M12 27c3-6 6-8 9-11 4 5 8 9 8 13H12Z" fill="#236447"/><circle cx="14" cy="14" r="4" fill="#eba849"/><path d="M20 10q2-6 9-4-1 6-9 4" fill="#82a777"/><path d="M9 31h22" stroke="#236447" stroke-width="1.5" stroke-linecap="round"/></svg>';
const button = (label, action, extra = '', symbol = '') => `<button class="button ${extra}" data-action="${action}">${symbol ? icon(symbol) : ''}${label}</button>`;
const STORAGE_KEY = 'jeju-design-prototype-v1';
const initialPlans = () => [
  {id:'sample-oct',title:'가을 메뉴 콘텐츠 준비',date:'2026-10-05',channel:'인스타그램',memo:'계절 음료 사진과 소개 문구 준비',region:'애월읍',recId:null},
  {id:'sample-nov',title:'평일 티타임 세트 안내',date:'2026-11-09',channel:'매장 안내',memo:'판매 가능한 메뉴 구성 먼저 확인',region:'애월읍',recId:null}
];
let storageIssue = false;
let saved = {};
try { saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}'); } catch { storageIssue = true; }
const validPlan = p => p && typeof p.id === 'string' && typeof p.title === 'string' && /^2026-(10|11|12)-\d{2}$/.test(p.date) && typeof p.channel === 'string' && typeof p.memo === 'string' && typeof p.region === 'string';
let state = {
  role:saved.role === 'service' ? 'service' : 'business',
  region:['애월읍','구좌읍'].includes(saved.region) ? saved.region : '애월읍',
  plans:Array.isArray(saved.plans) && saved.plans.every(validPlan) ? saved.plans : initialPlans(),
  records:saved.records && typeof saved.records === 'object' && !Array.isArray(saved.records) ? saved.records : {},
  metric:'visits',dataTab:'visits',search:'',ontologyNode:'recommendation'
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
const titles = {overview:'한눈에 보기',analysis:'지역 분석',recommendations:'홍보 제안',calendar:'3개월 일정',records:'실행 기록',ontology:'근거 연결',data:'자료 둘러보기',login:'시안 시작'};
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
function navigate(view) {
  if (!titles[view]) view='overview';
  closeDialog();
  if(location.hash.slice(1) === view) render(); else location.hash=view;
}
function navItem(view,symbol) {
  const label=view==='data' && state.role==='service'?'자료·기준 관리':titles[view];
  return `<a class="nav-link ${currentView===view?'active':''}" href="#${view}" ${currentView===view?'aria-current="page"':''}>${icon(symbol)}<span>${label}</span>${view==='calendar'?`<span class="count">${state.plans.length}</span>`:''}</a>`;
}
function shell(content) {
  return `<button class="mobile-scrim" data-action="close-menu" aria-label="메뉴 닫기"></button>
  <aside class="sidebar" id="sidebar"><a class="brand" href="#overview">${logo()}<span><strong>제주 마케팅<br>캘린더</strong><small>다음 계절을 준비하는 공간</small></span></a>
    <div class="business-card"><span class="avatar">${icon(state.role==='service'?'database':'shop')}</span><span><b>${state.role==='service'?'자료 운영 워크스페이스':'제주 예시 카페'}</b><small>${state.role==='service'?'서비스 운영자 시안':'사업체 운영자 시안'}</small></span></div>
    <p class="nav-label">마케팅 워크스페이스</p><nav aria-label="주요 메뉴" class="nav-group">${navItem('overview','grid')}${navItem('analysis','chart')}${navItem('recommendations','spark')}${navItem('calendar','calendar')}${navItem('records','clipboard')}</nav>
    <p class="nav-label">자료와 근거</p><nav aria-label="자료 메뉴" class="nav-group">${navItem('ontology','network')}${navItem('data','database')}</nav>
    <div class="sidebar-bottom"><div class="sidebar-note">${icon('leaf')}<b>작은 계획부터 차근차근</b><p>우리 가게에 맞는 제안을 고르고<br>실행할 수 있는 일정으로 바꿔요.</p></div><button class="profile" data-action="roles"><span class="avatar">${state.role==='service'?'운':'가'}</span><span><b>${state.role==='service'?'서비스 운영자':'사업체 운영자'}</b><small>시안 역할 변경</small></span>${icon('chevron')}</button></div>
  </aside><div class="workspace"><header class="topbar"><div class="row"><button class="icon-button mobile-menu" data-action="menu" aria-label="메뉴 열기" aria-expanded="false" aria-controls="sidebar">${icon('menu')}</button><div class="breadcrumb"><span>워크스페이스</span>${icon('chevron')}<strong>${titles[currentView]}</strong></div></div><div class="topbar-actions"><span class="prototype-label">예시 데이터 · 화면 시안</span><button class="icon-button" data-action="help" aria-label="시안 사용 안내">${icon('help')}</button><span class="avatar" aria-label="예시 계정">${state.role==='service'?'운':'가'}</span></div></header><main id="main" class="content" tabindex="-1">${storageIssue?'<p class="notice error">브라우저 저장을 사용할 수 없습니다. 변경 내용은 현재 화면에서만 유지됩니다.</p>':''}${content}<footer class="footer-note"><span>화면 검토용 예시 자료입니다. 지역 방문 집계는 가게의 실제 고객 수가 아닙니다.</span><button data-action="help">시안 사용 안내</button></footer></main></div>`;
}
function heading(title,description,action='') {
  return `<div class="page-heading"><div><div class="eyebrow">${icon('pin')}제주 ${state.region} · 카페</div><h1>${title}</h1><p>${description}</p></div>${action?`<div class="heading-action">${action}</div>`:''}</div>`;
}
function filters() {
  return `<div class="filter-strip"><label class="filter-pill">${icon('pin')}분석 지역 <select aria-label="분석 지역" id="region"><option ${state.region==='애월읍'?'selected':''}>애월읍</option><option ${state.region==='구좌읍'?'selected':''}>구좌읍</option></select></label><span class="filter-pill">${icon('calendar')}분석 자료 <b>2025년 예시</b></span><span class="filter-divider"></span><span class="filter-note">계획 대상 <b>2026년 10월부터 12월까지</b></span></div>`;
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
  return `<section class="panel"><div class="chart-box"><div class="panel-heading"><div><h2>우리 지역의 계절 흐름</h2><p>${state.region} · 2025년 1월부터 12월 예시 자료</p></div><div class="segmented" aria-label="차트 지표"><button data-metric="visits" aria-pressed="${visit}" class="${visit?'selected':''}">방문</button><button data-metric="spend" aria-pressed="${!visit}" class="${!visit?'selected':''}">소비</button></div></div><div class="chart-summary"><strong>${visit?'겨울을 앞두고 줄어드는 방문':'방문과 구분해 살펴보는 소비'}</strong></div>${chartSVG()}<div class="chart-note"><span><i class="legend-dot"></i>${visit?'지역 방문 집계 · 만 명':'카드 소비 · 억 원'}</span><span><i class="legend-dot orange"></i>${visit?'11·12월 참고 구간':'11월 자료 미수집'}</span></div>${chartTable()}</div><div class="chart-foot">${icon('info')}<span>${visit?'과거의 흐름을 참고해 홍보 시기를 준비하세요.':'빠진 값은 0으로 계산하지 않습니다.'}</span><button class="text-button" data-action="source">자료 기준 ${icon('chevron')}</button></div></section>`;
}
function assistantCard() {
  return `<section class="assistant-card"><span class="assistant-tag">${icon('spark')}AI 계획 도우미 <span class="badge green">시연</span></span><h2>다음 3개월,<br>함께 준비해 볼까요?</h2><p>지역의 흐름과 가게의 조건을 연결해<br>실행할 계획으로 정리해요.</p><div class="assistant-illustration" aria-hidden="true">${[10,11,12].map(m=>`<div class="month-leaf"><small>2026</small><strong>${m}</strong></div>`).join('')}</div>${button('계획 초안 살펴보기','agent','primary full','spark')}<span class="demo-sub">정해진 예시로 체험 · 실제 AI 미연결</span></section>`;
}
function recommendationCard(r) {
  const added=state.plans.some(p=>p.recId===`${state.region}:${r.id}`);
  return `<article class="recommendation"><div class="recommendation-top"><div class="rec-symbol ${r.color}">${icon(r.icon)}</div><span class="badge ${r.month===11?'amber':'green'}">${r.month}월 · ${r.tag}</span></div><h3>${r.title}</h3><p>${r.description}</p><div class="rec-meta"><span>${icon('pin')}${state.region}</span><span>${icon('calendar')}2026년 ${r.month}월</span></div><div class="rec-bottom"><button class="text-button" data-evidence="${r.id}">${icon('network')}추천 근거</button><button class="button small ${added?'secondary':''}" data-rec="${r.id}">${icon(added?'check':'plus')}${added?'일정에 추가됨':'일정에 담기'}</button></div></article>`;
}
function timeline() {
  return `<section class="panel timeline-panel"><div class="timeline-intro"><h3>우리 가게의 3개월</h3><p>${state.plans.length}개 계획을 준비하고 있어요</p></div><div class="timeline-months">${[10,11,12].map(m=>{const n=state.plans.filter(p=>Number(p.date.slice(5,7))===m).length;return `<div class="timeline-item"><b>${m}월</b><span>${n?`${n}개 계획 준비 중`:'새 계획을 담아보세요'}</span></div>`;}).join('')}</div><a class="button small" href="#calendar">일정 보기 ${icon('chevron')}</a></section>`;
}
function overview() {
  return `${heading('우리 가게의 다음 계절','지역의 흐름을 읽고, 실행할 수 있는 홍보 계획을 세워보세요.',button('AI로 계획 세우기','agent','primary','spark'))}${filters()}<div class="overview-grid">${trendPanel()}${assistantCard()}</div><div class="section-header"><div><h2>작은 실행으로 이어지는 홍보 제안</h2><p>우리 가게에 맞는지 확인하고 일정에 담아보세요.</p></div><a class="text-button" href="#recommendations">모든 제안 ${icon('chevron')}</a></div><div class="recommendation-grid">${recs.map(recommendationCard).join('')}</div>${timeline()}`;
}
function analysis() {
  const v=regionData[state.region].visits,delta=((v[10]-v[9])/v[9]*100).toFixed(1);
  return `${heading('지역의 흐름을 이해하는 시간','방문과 소비를 각각 비교하고, 자료의 기준부터 확인하세요.')}${filters()}<div class="mini-stats"><div class="mini-stat"><p>2025년 10월 방문 · 예시</p><strong>${v[9]}<small> 만 명</small></strong><span>지역 방문 집계 · 가게 손님 수와 구분</span></div><div class="mini-stat"><p>10월 대비 11월 방문 변화 · 예시</p><strong>${delta}%</strong><span>동일한 예시 집계 기준으로 계산</span></div><div class="mini-stat"><p>소비 자료의 확인 범위</p><strong>11<small>개월 / 12개월</small></strong><span>11월 미수집 · 연간 합계로 사용하지 않음</span></div></div><div class="two-col">${trendPanel()}<section class="panel panel-padding"><div class="panel-heading"><div><h2>비교하기 전에 확인해요</h2><p>예시 자료의 해석과 제한</p></div>${icon('book')}</div><div class="list-row"><div><h4>지역 방문 집계</h4><p>분석 대상 지역에 방문한 집계값</p></div><span class="badge green">기준 동일</span></div><div class="list-row"><div><h4>카드 소비 집계</h4><p>방문 집계와 다른 모집단 · 11월 누락</p></div><span class="badge amber">일부 누락</span></div><div class="list-row"><div><h4>가게의 실제 매출</h4><p>이 시안에서 수집하지 않은 별도 자료</p></div><span class="badge">미수집</span></div><div class="notice" style="margin-top:19px">지역 방문과 카드 소비를 나누어 가게의 구매전환율이나 실제 1인당 소비액으로 해석하지 않아요.</div><button class="text-button" data-action="source" style="margin-top:13px">출처와 집계 기준 보기 ${icon('arrow')}</button></section></div><div class="section-header"><h2>같은 기간의 지역별 방문 비교</h2><span class="badge">2025년 예시</span></div><section class="panel"><div class="table-wrap"><table><thead><tr><th scope="col">지역</th><th scope="col">10월 방문</th><th scope="col">11월 방문</th><th scope="col">변화율</th><th scope="col">비교 조건</th></tr></thead><tbody>${Object.entries(regionData).map(([name,d])=>`<tr><td><b>${name}</b></td><td>${d.visits[9]}만 명</td><td>${d.visits[10]}만 명</td><td>${((d.visits[10]-d.visits[9])/d.visits[9]*100).toFixed(1)}%</td><td>같은 예시 집계 기준</td></tr>`).join('')}</tbody></table></div><p class="table-caption">변화율 = (11월 − 10월) ÷ 10월 × 100 · 실제 통계가 아닌 화면 검토용 수치</p></section>`;
}
function recommendations() {
  return `${heading('우리 가게에 맞는 홍보 아이디어','근거를 살펴보고, 운영할 수 있는 계획을 골라보세요.',button('AI로 계획 세우기','agent','primary','spark'))}${filters()}<section class="intro-callout">${icon('network')}<div><h3>왜 이 제안인지, 근거까지 함께</h3><p>지역·시기·운영 조건의 연결을 확인할 수 있어요. 상품과 비용은 적용 전 확인해 주세요.</p></div><a class="button small" href="#ontology">근거 연결 보기</a></section><div class="section-header"><h2>가을부터 겨울까지 제안 3개</h2><span class="badge amber">예시 추천</span></div><div class="recommendation-grid">${recs.map(recommendationCard).join('')}</div>${timeline()}`;
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
  return `${heading('실행한 만큼, 다음 계획이 선명해져요','계획별 실행 여부와 실제로 수집한 결과를 기록하세요.',button('회고 초안 보기','report','','book'))}<div class="mini-stats"><div class="mini-stat"><p>준비한 계획</p><strong>${state.plans.length}<small>개</small></strong><span>2026년 10월부터 12월</span></div><div class="mini-stat"><p>실행 기록 작성</p><strong>${done.length}<small>개</small></strong><span>입력한 예시 기록 기준</span></div><div class="mini-stat"><p>아직 기록하지 않은 계획</p><strong>${state.plans.length-done.length}<small>개</small></strong><span>기록 없음은 실행 실패를 뜻하지 않아요</span></div></div><section class="panel">${state.plans.length?state.plans.map(p=>{const record=state.records[p.id];return `<div class="record-row"><div class="date-box"><small>${Number(p.date.slice(5,7))}월</small><strong>${Number(p.date.slice(8))}</strong></div><div><h3>${escapeHTML(p.title)}</h3><p>${escapeHTML(p.channel)} · ${record?`${escapeHTML(record.status)} / 클릭 ${record.clicks===null?'미수집':escapeHTML(record.clicks)+'건'}`:'실행 결과를 아직 입력하지 않았어요'}</p></div><span class="badge ${record?'green':''}">${record?'기록 있음':'미기록'}</span><button class="button small" data-record="${escapeHTML(p.id)}">${record?'기록 수정':'기록 입력'}</button></div>`;}).join(''):`<div class="empty-state"><div class="empty-icon">${icon('clipboard')}</div><h3>먼저 작은 계획을 만들어 볼까요?</h3><p>일정에서 직접 계획을 추가하면 이곳에서 실행 여부와 결과를 기록할 수 있어요.</p><a class="button primary" href="#calendar">일정 작성하기</a></div>`}</section><p class="notice" style="margin-top:20px">수집하지 않은 클릭·예약 수는 빈칸으로 남겨 주세요. 0건과 미수집은 다르게 기록됩니다.</p>`;
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
function dataView() {
  return `${heading(state.role==='service'?'자료와 기준을 관리하는 공간':'추천에 사용한 자료를 살펴보세요','시안에 포함된 예시 자료이며, 실제 통계와 연결되지 않았습니다.',button('예시 CSV 내려받기','export','','download'))}${state.role==='service'?`<section class="panel panel-padding" style="margin-bottom:22px"><div class="upload-area">${icon('upload')}<h3>자료 등록 흐름 미리보기</h3><p>실제 파일을 업로드하지 않고, 형식·누락 검증 화면을 체험합니다.</p>${button('예시 파일 검증하기','validate','','check')}</div></section>`:''}<div class="source-tabs" aria-label="자료 종류"><button data-tab="visits" class="${state.dataTab==='visits'?'selected':''}" aria-pressed="${state.dataTab==='visits'}">지역 방문 자료</button><button data-tab="spend" class="${state.dataTab==='spend'?'selected':''}" aria-pressed="${state.dataTab==='spend'}">카드 소비 자료</button></div><div class="toolbar"><label class="search">${icon('search')}<input class="field" id="data-search" placeholder="지역명 또는 월로 검색" aria-label="자료 검색" value="${escapeHTML(state.search)}"></label><span class="badge">예시 자료 v1 · 2025년</span></div><section class="panel"><div class="table-wrap"><table><thead><tr><th scope="col">지역</th><th scope="col">기준기간</th><th scope="col">지표</th><th scope="col">값</th><th scope="col">단위</th><th scope="col">자료 상태</th></tr></thead><tbody id="data-rows">${dataRows()}</tbody></table></div><p class="table-caption">출처: 화면 시안 내 예시 자료 v1 · 다운로드 파일에도 예시 자료임을 표시합니다.</p></section>`;
}
function login() {
  return `<main class="login-page" id="main"><section class="login-story"><a class="brand" href="#overview">${logo()}<span><strong>제주 마케팅 캘린더</strong><small>다음 계절을 준비하는 공간</small></span></a><h1>다음 계절의 기회를,<br>오늘의 계획으로.</h1><p>우리 지역의 흐름을 이해하고<br>가게에 맞는 3개월 홍보 계획을 세워보세요.</p><div class="season-art" aria-hidden="true"><div class="season-arch"><b>10</b><span>가을을 준비하고</span></div><div class="season-arch"><b>11</b><span>겨울을 맞이하고</span></div><div class="season-arch"><b>12</b><span>다음 방문을 잇는</span></div></div></section><section class="login-form"><span class="badge green" style="margin-bottom:14px">클릭 가능한 화면 시안</span><h2>어떤 공간을 둘러볼까요?</h2><p>역할에 따라 필요한 화면을 확인할 수 있어요.</p>${['business','service'].map(role=>`<button class="role-option ${roleChoice===role?'selected':''}" data-role="${role}" aria-pressed="${roleChoice===role}">${icon(role==='business'?'shop':'database')}<span><b>${role==='business'?'사업체 운영자':'서비스 운영자'}</b><p>${role==='business'?'지역 분석 · 홍보 제안 · 일정과 실행 기록':'예시 자료 등록 · 품질 확인 · 근거 관리'}</p></span></button>`).join('')}${button('시안 둘러보기','enter','primary full','arrow')}<div class="notice green">역할 선택은 화면 체험용입니다. 실제 로그인이나 서버 권한 검증은 연결되지 않았어요.</div><p class="hint" style="margin-top:18px">모든 수치와 사업체 정보는 예시입니다.</p></section></main>`;
}
function render() {
  const hash=location.hash.slice(1);currentView=titles[hash]?hash:'overview';
  document.title=`${titles[currentView]} · 제주 마케팅 캘린더 시안`;
  const views={overview,analysis,recommendations,calendar,records,ontology,data:dataView};
  app.innerHTML=currentView==='login'?login():shell(views[currentView]());
}
function openDialog(title,subtitle,body,drawer=false) {
  if(!dialog.open) opener=document.activeElement;
  dialog.className=drawer?'drawer':'';
  dialog.innerHTML=`<header class="dialog-header"><div><h2 id="dialog-title">${title}</h2>${subtitle?`<p>${subtitle}</p>`:''}</div><button class="icon-button" data-action="close-dialog" aria-label="닫기">${icon('close')}</button></header><div class="dialog-body">${body}</div>`;
  if(!dialog.open)dialog.showModal();
  document.body.style.overflow='hidden';
}
function closeDialog() {if(dialog.open)dialog.close();}
dialog.addEventListener('close',()=>{document.body.style.overflow='';if(opener?.isConnected)opener.focus();});
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
  const record=state.records[id]||{status:'실행함',date:p.date,clicks:null,bookings:null,note:''};
  openDialog('실행 기록 남기기',escapeHTML(p.title),`<form id="record-form" data-id="${escapeHTML(id)}"><label class="field-label" for="record-status">실행 여부</label><select class="field" id="record-status" name="status">${['실행함','미실행','확인 중'].map(s=>`<option ${record.status===s?'selected':''}>${s}</option>`).join('')}</select><label class="field-label" for="record-date">실제 실행 날짜</label><input class="field" name="date" id="record-date" type="date" value="${escapeHTML(record.date||'')}" ${record.status==='실행함'?'required':''}><div class="form-row"><div><label class="field-label" for="record-clicks" style="margin-top:18px">확인한 클릭 수</label><input class="field" id="record-clicks" name="clicks" type="number" min="0" max="99999999" step="1" placeholder="미수집" value="${escapeHTML(record.clicks??'')}"></div><div><label class="field-label" for="record-bookings" style="margin-top:18px">확인한 예약 수</label><input class="field" id="record-bookings" name="bookings" type="number" min="0" max="99999999" step="1" placeholder="미수집" value="${escapeHTML(record.bookings??'')}"></div></div><p class="hint">빈칸은 미수집, 숫자 0은 실제로 확인한 0건입니다.</p><label class="field-label" for="record-note">결과와 수집 방법</label><textarea class="field" id="record-note" name="note" maxlength="500" placeholder="어디에서 확인한 수치인지, 다음에 바꿀 점을 적어주세요.">${escapeHTML(record.note||'')}</textarea><div class="dialog-actions"><button type="button" class="button" data-action="close-dialog">취소</button><button class="button primary" type="submit">기록 저장</button></div></form>`);
}
let agentDraft = null;
function agentDialog() {
  agentDraft=null;
  openDialog('AI 계획 도우미','우리 가게에 맞는 3개월을 준비해요',`<div class="notice green">화면 체험을 위해 정해진 예시를 보여줍니다. 실제 AI 모델은 연결되지 않았어요.</div><div class="agent-steps"><span class="done">1. 운영 조건</span><span>2. 초안 검토</span><span>3. 선택해 적용</span></div><form id="agent-form"><label class="field-label" for="agent-goal">어떤 목표로 준비할까요?</label><select class="field" id="agent-goal" name="goal"><option value="prepare">겨울 비수기 미리 준비하기</option><option value="weekday">평일 방문 계기 만들기</option></select><label class="field-label" for="agent-product">홍보할 상품</label><input class="field" id="agent-product" name="product" maxlength="35" placeholder="예: 귤차와 구움과자 세트"><p class="hint">입력하지 않으면 상품 확인이 필요한 초안으로 남겨요.</p><label class="field-label" for="agent-day">매주 쉬는 날</label><select class="field" id="agent-day" name="closed"><option value="unknown">아직 확인하지 않음</option><option value="none">정기 휴무 없음</option>${['일','월','화','수','목','금','토'].map((d,i)=>`<option value="${i}">${d}요일</option>`).join('')}</select><div class="notice" style="margin-top:20px">자료 기준: ${state.region} 2025년 예시 방문 자료<br>계획 대상: 2026년 10월부터 12월<br>11월 소비 자료는 누락되어 추천 근거에서 제외합니다.</div><div class="dialog-actions"><button type="submit" class="button primary full">${icon('spark')}예시 초안 만들기</button></div></form>`,true);
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
  openDialog('AI 계획 도우미','예시 초안을 확인하고 적용할 항목을 골라요',`<span class="badge amber">고정 예시 기반 · 실제 AI 미연결</span><div class="agent-steps"><span class="done">1. 운영 조건</span><span class="done">2. 초안 검토</span><span>3. 선택해 적용</span></div><div class="agent-status">${icon('check')} ${shifted?'선택한 휴무일과 겹친 일정을 다음 날로 옮겼어요.':'입력한 조건을 예시 초안에 반영했어요.'}</div><h3 class="agent-result-title">${agentDraft.length}개 계획 후보</h3><p class="small muted">${escapeHTML(state.region)} · ${goal==='weekday'?'평일 방문 계기 만들기':'겨울 비수기 미리 준비하기'}</p><form id="agent-apply-form">${agentDraft.map(r=>{const exists=state.plans.some(p=>p.recId===`${r.region}:${r.id}`);return `<label class="check-option"><input type="checkbox" name="selected" value="${r.id}" ${exists?'disabled':'checked'}><span><strong>${escapeHTML(r.title)}</strong><p>${r.date} · ${r.channel}${r.shifted?' · 휴무일 조정':''}</p><p>${exists?'이미 일정에 담긴 제안입니다.':r.task}</p></span></label>`;}).join('')}<div class="notice" style="margin-top:18px">${!product?'상품: 판매 가능한 상품 확인 필요<br>':''}${closed==='unknown'?'휴무일: 확인 필요<br>':''}예산·판매 가능 여부·기존 일정과의 시간 충돌은 직접 확인해 주세요.</div><div class="dialog-actions">${button('조건 다시 입력','agent')}<button type="submit" class="button primary">선택한 계획 담기</button></div></form>`,true);
}
function reportDialog() {
  const entries=state.plans.filter(p=>state.records[p.id]);
  const executed=entries.filter(p=>state.records[p.id].status==='실행함');
  openDialog('실행 회고 초안','입력한 예시 기록만 정리한 요약',`<span class="badge amber">규칙 기반 요약 · AI 미연결</span><h3 style="margin-top:20px">2026년 10월부터 12월</h3><ul class="relation-list"><li>작성한 계획 <b>${state.plans.length}개</b></li><li>실행 기록이 있는 계획 <b>${entries.length}개</b></li><li>실행했다고 기록한 계획 <b>${executed.length}개</b></li><li>미기록 계획 <b>${state.plans.length-entries.length}개</b></li></ul><p class="muted small">${entries.length?'입력한 기록을 바탕으로 다음 계획의 준비 사항을 검토하세요.':'아직 입력한 실행 기록이 없습니다. 기록을 먼저 남겨 주세요.'}</p><div class="notice" style="margin-top:18px">기록 누락을 실패로 해석하지 않으며 매출 증가나 홍보의 인과 효과를 추정하지 않습니다.</div><div class="dialog-actions">${button('닫기','close-dialog','primary')}</div>`);
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
  if(target.dataset.metric){state.metric=target.dataset.metric;render();return;}
  if(target.dataset.tab){state.dataTab=target.dataset.tab;render();return;}
  if(target.dataset.evidence){evidenceDialog(target.dataset.evidence);return;}
  if(target.dataset.rec){planDialog({recId:target.dataset.rec});return;}
  if(target.dataset.edit){planDialog({id:target.dataset.edit});return;}
  if(target.dataset.record){recordDialog(target.dataset.record);return;}
  if(target.dataset.month){planDialog({month:Number(target.dataset.month)});return;}
  if(target.dataset.node){state.ontologyNode=target.dataset.node;render();$(`[data-node="${state.ontologyNode}"]`)?.focus();return;}
  if(target.dataset.role){roleChoice=target.dataset.role;render();return;}
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
    case 'enter':state.role=roleChoice;persist();navigate('overview');break;
    case 'menu':$('.sidebar').classList.add('open');$('.mobile-scrim').classList.add('open');target.setAttribute('aria-expanded','true');$('.workspace').inert=true;$('.sidebar .nav-link').focus();break;
    case 'close-menu':$('.sidebar')?.classList.remove('open');$('.mobile-scrim')?.classList.remove('open');$('.workspace').inert=false;$('.mobile-menu')?.setAttribute('aria-expanded','false');$('.mobile-menu')?.focus();break;
    case 'export':exportCSV();break;
    case 'validate':if(state.role==='service')openDialog('예시 파일 검증 결과','실제 업로드 없이 화면 흐름을 확인하는 시연',`<div class="notice green">예시 필수 열·단위·기간 형식을 확인했습니다.</div><ul class="relation-list"><li><b>지역 코드·지표·기준월·단위</b>예시 형식 일치</li><li><b>카드 소비 11월</b>두 지역 모두 누락 · 0으로 변환하지 않음</li><li><b>추천 사용 여부</b>누락된 소비값을 제외하고 방문 예시만 사용</li></ul><div class="notice">실제 CSV 검증기는 연결되지 않았습니다.</div><div class="dialog-actions">${button('확인','close-dialog','primary')}</div>`);break;
    case 'reset':openDialog('시안 데이터를 초기화할까요?','이 브라우저에서 만든 계획과 기록이 초기 예시로 바뀝니다.',`<div class="dialog-actions">${button('취소','close-dialog')}${button('초기화','confirm-reset','primary')}</div>`);break;
    case 'confirm-reset':state.plans=initialPlans();state.records={};const ok=persist();closeDialog();render();notify(ok?'처음의 예시 데이터로 돌아왔습니다.':'현재 화면을 초기화했습니다. 브라우저 저장은 실패했습니다.');break;
  }
});
document.addEventListener('change',event=>{
  if(event.target.id==='region'){state.region=event.target.value;persist();render();notify(`${state.region} 예시 자료를 표시합니다.`);}
  if(event.target.id==='record-status'){$('#record-date').required=event.target.value==='실행함';if(event.target.value!=='실행함')$('#record-date').value='';}
});
document.addEventListener('input',event=>{if(event.target.id==='data-search'){state.search=event.target.value;$('#data-rows').innerHTML=dataRows();}});
document.addEventListener('keydown',event=>{if(event.key==='Escape' && !dialog.open && $('.sidebar.open')){$('.sidebar').classList.remove('open');$('.mobile-scrim').classList.remove('open');$('.workspace').inert=false;$('.mobile-menu')?.setAttribute('aria-expanded','false');$('.mobile-menu')?.focus();}});
document.addEventListener('submit',event=>{
  const form=event.target;if(!['plan-form','record-form','agent-form','agent-apply-form'].includes(form.id))return;
  event.preventDefault();const data=new FormData(form);
  if(form.id==='plan-form'){
    const title=String(data.get('title')).trim(),date=String(data.get('date'));
    if(!title){$('#plan-error').hidden=false;$('#plan-error').textContent='계획 이름을 입력해 주세요.';$('#plan-title').focus();return;}
    if(!/^2026-(10|11|12)-\d{2}$/.test(date))return;
    const id=form.dataset.id,old=state.plans.find(p=>p.id===id),recId=form.dataset.recId;
    const plan={id:old?.id||`plan-${Date.now()}-${Math.random().toString(36).slice(2,7)}`,title,date,channel:String(data.get('channel')),memo:String(data.get('memo')).trim(),region:old?.region||state.region,recId:old?.recId||(recId?`${state.region}:${recId}`:null)};
    if(old)state.plans=state.plans.map(p=>p.id===id?plan:p);else state.plans.push(plan);
    const ok=persist();closeDialog();render();notify(ok?'계획을 이 브라우저에 저장했습니다.':'화면에 반영했지만 브라우저 저장은 실패했습니다.');
  }
  if(form.id==='record-form'){
    const id=form.dataset.id;if(!state.plans.some(p=>p.id===id))return;
    state.records[id]={status:String(data.get('status')),date:String(data.get('date')),clicks:data.get('clicks')===''?null:Number(data.get('clicks')),bookings:data.get('bookings')===''?null:Number(data.get('bookings')),note:String(data.get('note')).trim()};
    const ok=persist();closeDialog();render();notify(ok?'실행 기록을 이 브라우저에 저장했습니다.':'화면에 반영했지만 브라우저 저장은 실패했습니다.');
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
