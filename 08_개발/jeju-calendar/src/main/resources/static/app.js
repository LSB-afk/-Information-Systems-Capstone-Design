/*
 * 제주 마케팅 캘린더 View (9주차)
 * 화면 시안(02_제품/화면시안)의 화면 구성과 styles.css를 그대로 쓰고, 자료는 서버 API에서 읽고 저장함
 * 브라우저 저장소에는 메뉴 너비 설정만 둠
 */
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
  arrow:'<path d="M4 12h16m-6-6 6 6-6 6"/>',
  plus:'<path d="M12 5v14M5 12h14"/>',
  close:'<path d="m6 6 12 12M6 18 18 6"/>',
  pin:'<path d="M19 10c0 5-7 11-7 11S5 15 5 10a7 7 0 1 1 14 0Z"/><circle cx="12" cy="10" r="2.5"/>',
  shop:'<path d="M3 9h18l-2-5H5L3 9Zm1 5v7h16v-7M8 21v-6h5v6M3 9v2a3 3 0 0 0 6 0 3 3 0 0 0 6 0 3 3 0 0 0 6 0V9"/>',
  cup:'<path d="M5 8h12v6a6 6 0 0 1-12 0V8Zm12 1h2a3 3 0 0 1 0 6h-2M7 3v2m4-2v2m4-2v2M3 21h17"/>',
  leaf:'<path d="M20 4C7 1 1 12 7 18S23 18 20 4ZM4 21 16 9"/>',
  people:'<circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M16 5a3 3 0 0 1 0 6m2 4a5 5 0 0 1 3 4v2"/>',
  help:'<circle cx="12" cy="12" r="9"/><path d="M9.5 9a2.5 2.5 0 0 1 5 0c0 2-2.5 2-2.5 4m0 3v.1"/>',
  info:'<circle cx="12" cy="12" r="9"/><path d="M12 11v6m0-10v.1"/>',
  download:'<path d="M12 3v12m-5-5 5 5 5-5M4 16v5h16v-5"/>',
  menu:'<path d="M4 6h16M4 12h16M4 18h16"/>',
  panel:'<rect x="3" y="4" width="18" height="16" rx="2"/><path d="M9 4v16m7-12-3 4 3 4"/>',
  eye:'<path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/>',
  logout:'<path d="M10 4H5v16h5m4-12 4 4-4 4m-6-4h13"/>',
  edit:'<path d="m15 4 5 5M4 20l5-1L21 7a2 2 0 0 0-5-5L4 14l-1 7M12 21h9"/>',
  book:'<path d="M3 4h6a3 3 0 0 1 3 3v14a3 3 0 0 0-3-3H3V4Zm18 0h-6a3 3 0 0 0-3 3m0 14a3 3 0 0 1 3-3h6V4"/>'
};
const icon = name => `<svg class="icon" viewBox="0 0 24 24" aria-hidden="true">${icons[name] || icons.grid}</svg>`;
const logo = () => '<svg class="logo" viewBox="0 0 40 40" aria-hidden="true"><rect width="40" height="40" rx="12" fill="#eaf3ec"/><path d="M12 27c3-6 6-8 9-11 4 5 8 9 8 13H12Z" fill="#236447"/><circle cx="14" cy="14" r="4" fill="#eba849"/><path d="M20 10q2-6 9-4-1 6-9 4" fill="#82a777"/><path d="M9 31h22" stroke="#236447" stroke-width="1.5" stroke-linecap="round"/></svg>';
const button = (label, action, extra = '', symbol = '') => `<button type="button" class="button ${extra}" data-action="${action}">${symbol ? icon(symbol) : ''}${label}</button>`;

// ---------- 코드값 이름 (ERD 코드 설계서) ----------
const CHANNELS = {INSTAGRAM:'인스타그램', STORE:'매장 안내', BLOG:'블로그', ETC:'기타'};
const EXEC = {DONE:'실행함', NOT_DONE:'미실행', CHECKING:'확인 중'};
const ADOPT = {YES:'채택', NO:'미채택', UNKNOWN:'미확인'};
const CATEGORY = {CAFE:'카페', EXPERIENCE:'체험 활동', RESTAURANT:'음식점'};
const ITEM_LABELS = {TIMING:'홍보 시기', AUDIENCE:'홍보 대상', PRODUCT:'상품 후보', ATTRACTION:'주변 관광지'};
const UNITS = {PERSON_10K:'만 명', KRW_100M:'억 원'};
const titles = {overview:'한눈에 보기',analysis:'지역 분석',recommendations:'홍보 제안 조회',calendar:'3개월 홍보 일정표 관리',records:'사용 기록 입력',data:'자료·기준 관리',login:'로그인'};
const label = (map, code) => map[code] || code || '';
const display = (value, unit = '건') => value === null || value === undefined ? '미확보' : `${value}${unit}`;
const monthOf = date => Number(String(date).slice(5, 7));
const dayOf = date => Number(String(date).slice(8, 10));

// ---------- 서버 API ----------
class ApiError extends Error {
  constructor(message, field, status) { super(message); this.field = field; this.status = status; }
}
async function api(path, {method = 'GET', body, form} = {}) {
  const options = {method, headers: {Accept: 'application/json'}};
  if (body !== undefined) { options.headers['Content-Type'] = 'application/json'; options.body = JSON.stringify(body); }
  if (form) options.body = form;
  let response;
  try { response = await fetch(path, options); }
  catch { throw new ApiError('서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.', null, 0); }
  if (response.status === 204) return null;
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    if (response.status === 401 && session.user && path !== '/api/auth/login') {
      session.user = null;
      navigate('login');
    }
    throw new ApiError(data?.message || '요청을 처리하지 못했습니다. 다시 시도해 주세요.', data?.field || null, response.status);
  }
  return data;
}
const query = params => new URLSearchParams(Object.entries(params).filter(([, v]) => v !== null && v !== undefined && v !== '')).toString();

// ---------- 화면 상태 (서버에서 읽은 값만 기억함) ----------
const session = {
  user: null, businesses: [], businessId: null, regions: [], region: null,
  metric: 'VISIT', year: null, analysisMode: 'visits', planCount: null, period: null, lastSource: null
};
let currentView = 'overview';
let opener = null;
let toastTimer;
let roleChoice = 'BUSINESS';
let renderToken = 0;
let csvChecked = false;

const LAYOUT_KEY = 'jeju-calendar-layout-v1';
const sidebarLayout = {width: 248, collapsed: false};
try {
  const layout = JSON.parse(localStorage.getItem(LAYOUT_KEY) || 'null');
  if (Number.isFinite(layout?.width)) sidebarLayout.width = Math.min(360, Math.max(216, layout.width));
  sidebarLayout.collapsed = layout?.collapsed === true;
} catch {}

const isService = () => session.user?.role === 'SERVICE';
const business = () => session.businesses.find(b => b.businessId === session.businessId) || null;
const regionName = code => session.regions.find(r => r.regionCode === code)?.regionName || code || '';

async function loadSession() {
  session.user = await api('/api/auth/me').catch(() => null);
  if (!session.user) return;
  [session.businesses, session.regions] = await Promise.all([api('/api/businesses'), api('/api/regions')]);
  if (!session.businesses.some(b => b.businessId === session.businessId)) session.businessId = session.businesses[0]?.businessId ?? null;
  session.region = business()?.regionCode || session.regions[0]?.regionCode || null;
}

function notify(message) {
  clearTimeout(toastTimer); $('#toast').textContent = message; $('#toast').classList.add('visible');
  toastTimer = setTimeout(() => $('#toast').classList.remove('visible'), 4000);
}

// ---------- 공통 레이아웃 (SCR-C-000) ----------
function applySidebarLayout() {
  document.documentElement.style.setProperty('--sidebar-width', `${sidebarLayout.collapsed ? 76 : sidebarLayout.width}px`);
  document.documentElement.classList.toggle('sidebar-collapsed', sidebarLayout.collapsed);
  const toggle = $('[data-action="sidebar-toggle"]');
  if (toggle) {
    const text = sidebarLayout.collapsed ? '메뉴 펼치기' : '메뉴 접기';
    toggle.setAttribute('aria-label', text); toggle.title = text; toggle.setAttribute('aria-expanded', String(!sidebarLayout.collapsed));
  }
  const handle = $('#sidebar-resizer');
  if (handle) { handle.setAttribute('aria-valuenow', String(sidebarLayout.width)); handle.setAttribute('aria-valuetext', `${sidebarLayout.width}px`); }
}
function saveSidebarLayout() {
  try { localStorage.setItem(LAYOUT_KEY, JSON.stringify(sidebarLayout)); }
  catch { notify('메뉴 설정을 저장하지 못했어요. 현재 화면에는 적용했습니다.'); }
}
function closeMobileMenu(restoreFocus = false) {
  $('.sidebar')?.classList.remove('open'); $('.mobile-scrim')?.classList.remove('open');
  if ($('.workspace')) $('.workspace').inert = false;
  document.documentElement.classList.remove('menu-open');
  $('.mobile-menu')?.setAttribute('aria-expanded', 'false');
  if (restoreFocus) $('.mobile-menu')?.focus();
}
function navigate(view) {
  if (!titles[view]) view = 'overview';
  closeDialog();
  if (location.hash.slice(1) === view) render(); else location.hash = view;
}
function navItem(view, symbol) {
  const text = titles[view];
  const count = view === 'calendar' && session.planCount !== null ? `<span class="count">${session.planCount}</span>` : '';
  return `<a class="nav-link ${currentView === view ? 'active' : ''}" href="#${view}" aria-label="${text}" title="${text}" ${currentView === view ? 'aria-current="page"' : ''}>${icon(symbol)}<span>${text}</span>${count}</a>`;
}
function businessCard() {
  const b = business();
  const name = isService() ? '자료 운영 워크스페이스' : (b?.businessName || '등록된 사업체 없음');
  const select = session.businesses.length > 1
    ? `<label class="sr-only" for="business-select">계획·기록을 관리할 사업체</label><select id="business-select" class="field" style="margin-top:8px">${session.businesses.map(x => `<option value="${x.businessId}" ${x.businessId === session.businessId ? 'selected' : ''}>${escapeHTML(x.businessName)}</option>`).join('')}</select>` : '';
  return `<div class="business-card"><span class="avatar">${icon(isService() ? 'database' : 'shop')}</span><span><b>${escapeHTML(name)}</b><small>${isService() ? '서비스 운영자' : '사업체 운영자'}</small>${select}</span></div>`;
}
function shell() {
  const initial = isService() ? '운' : '가';
  const b = business();
  const permissionNote = isService() && ['overview', 'calendar', 'records'].includes(currentView)
    ? `<p class="notice">${b ? `권한을 부여받은 사업체: ${escapeHTML(b.businessName)} · 허용된 계획과 기록만 관리합니다.` : '권한을 부여받은 사업체가 없어 계획과 기록을 관리할 수 없습니다.'}</p>` : '';
  return `<button class="mobile-scrim" data-action="close-menu" aria-label="메뉴 닫기" tabindex="-1"></button>
  <aside class="sidebar" id="sidebar" aria-label="워크스페이스 메뉴"><a class="brand" href="#overview" aria-label="제주 마케팅 캘린더 홈" title="제주 마케팅 캘린더 홈">${logo()}<span><strong>제주 마케팅<br>캘린더</strong><small>다음 계절을 준비하는 공간</small></span></a>
    ${businessCard()}
    <div class="sidebar-controls"><span>나에게 맞는 메뉴 너비</span><button class="text-button" data-action="sidebar-reset" title="메뉴 너비 초기화" aria-label="메뉴 너비 초기화">초기화</button></div>
    <p class="nav-label">마케팅 워크스페이스</p><nav aria-label="주요 메뉴" class="nav-group">${navItem('overview', 'grid')}${navItem('analysis', 'chart')}${navItem('recommendations', 'spark')}${navItem('calendar', 'calendar')}${navItem('records', 'clipboard')}</nav>
    ${isService() ? `<p class="nav-label">자료와 근거</p><nav aria-label="자료 메뉴" class="nav-group">${navItem('data', 'database')}</nav>` : ''}
    <div class="sidebar-bottom"><div class="sidebar-note">${icon('leaf')}<b>작은 계획부터 차근차근</b><p>우리 가게에 맞는 제안을 고르고<br>실행할 수 있는 일정으로 바꿔요.</p></div><button class="profile" data-action="logout" aria-label="계정 로그아웃" title="계정 로그아웃"><span class="avatar">${initial}</span><span><b>${escapeHTML(session.user.email)}</b><small>로그아웃</small></span>${icon('chevron')}</button><button class="sidebar-session" data-action="logout" title="로그아웃" aria-label="로그아웃">${icon('logout')}<span>로그아웃</span></button></div>
  </aside><div id="sidebar-resizer" class="sidebar-resizer" role="separator" aria-label="메뉴 너비 조절" aria-controls="sidebar" aria-orientation="vertical" aria-valuemin="216" aria-valuemax="360" aria-valuenow="${sidebarLayout.width}" tabindex="0" title="끌어서 너비 조절 · 방향키로도 조절할 수 있어요"><span></span></div><div class="workspace"><header class="topbar"><div class="row"><button class="icon-button desktop-menu" data-action="sidebar-toggle" aria-label="메뉴 접기" aria-controls="sidebar" aria-expanded="true">${icon('panel')}</button><button class="icon-button mobile-menu" data-action="menu" aria-label="메뉴 열기" aria-expanded="false" aria-controls="sidebar">${icon('menu')}</button><div class="breadcrumb"><span>워크스페이스</span>${icon('chevron')}<strong>${titles[currentView]}</strong></div></div><div class="topbar-actions"><button class="icon-button" data-action="help" aria-label="사용 안내">${icon('help')}</button><span class="avatar" aria-label="로그인 계정">${initial}</span></div></header><main id="main" class="content" tabindex="-1">${permissionNote}<div id="view"><section class="panel empty-state" role="status"><p>불러오는 중입니다…</p></section></div><footer class="footer-note"><span>지역 방문 집계는 가게의 실제 고객 수가 아닙니다. 없는 값은 미확보로 표시합니다.</span><button data-action="help">사용 안내</button></footer></main></div>`;
}
function heading(title, description, action = '') {
  const b = business();
  const where = `제주 ${escapeHTML(regionName(session.region))}${b ? ` · ${escapeHTML(label(CATEGORY, b.category))}` : ''}`;
  return `<div class="page-heading"><div><div class="eyebrow">${icon('pin')}${where}</div><h1>${title}</h1><p>${description}</p></div>${action ? `<div class="heading-action">${action}</div>` : ''}</div>`;
}
function errorPanel(error) {
  return `<section class="panel empty-state" role="alert"><div class="empty-icon">${icon('info')}</div><h2>화면을 불러오지 못했습니다</h2><p>${escapeHTML(error.message)}</p>${button('다시 시도', 'reload', 'primary')}</section>`;
}
function needBusiness() {
  return `<section class="panel empty-state" role="status"><div class="empty-icon">${icon('shop')}</div><h2>관리할 수 있는 사업체가 없습니다</h2><p>${isService() ? '사업체에서 관리 권한을 받으면 계획과 기록을 처리할 수 있습니다.' : '사업체 정보가 등록되면 계획과 기록을 관리할 수 있습니다.'}</p></section>`;
}

async function render() {
  const token = ++renderToken;
  const hash = location.hash.slice(1);
  currentView = !session.user ? 'login' : (titles[hash] ? hash : 'overview');
  if (currentView === 'data' && !isService()) currentView = 'overview';
  if (session.user && currentView === 'login') currentView = 'overview';
  if (hash !== currentView) history.replaceState(null, '', `#${currentView}`);
  document.title = `${titles[currentView]} · 제주 마케팅 캘린더`;
  closeMobileMenu();
  csvChecked = false;
  if (currentView === 'login') { app.innerHTML = login(); applySidebarLayout(); return; }
  app.innerHTML = shell();
  applySidebarLayout();
  const views = {overview, analysis, recommendations, calendar, records, data: dataView};
  let html;
  try { html = await views[currentView](); }
  catch (error) { html = errorPanel(error); }
  if (token !== renderToken || !$('#view')) return;
  $('#view').innerHTML = html;
  const count = $('.nav-link[href="#calendar"] .count');
  if (session.planCount !== null) {
    if (count) count.textContent = session.planCount;
    else $('.nav-link[href="#calendar"]')?.insertAdjacentHTML('beforeend', `<span class="count">${session.planCount}</span>`);
  }
}

// ---------- 로그인 (SCR-C-001) ----------
function login() {
  return `<main class="login-page" id="main" tabindex="-1">
    <section class="login-story"><a class="brand" href="#overview">${logo()}<span><strong>제주 마케팅 캘린더</strong><small>다음 계절을 준비하는 공간</small></span></a>
      <div class="login-story-copy"><span class="login-kicker">우리 가게의 다음 계절</span><h1>준비한 오늘이<br>다음 방문으로.</h1><p>지역의 흐름을 읽고, 우리 가게에 맞는<br>3개월 홍보 계획을 이어가세요.</p></div>
      <div class="season-art" aria-hidden="true"><div class="season-arch"><b>10</b><span>가을을 준비하고</span></div><div class="season-arch"><b>11</b><span>겨울을 맞이하고</span></div><div class="season-arch"><b>12</b><span>다음 방문을 잇는</span></div></div>
      <p class="login-story-foot">제주에서 시작하는, 작고 꾸준한 계획</p>
    </section>
    <section class="login-form" aria-labelledby="login-title"><span class="badge green">제주 마케팅 캘린더</span><h2 id="login-title">다시 만나 반가워요</h2><p>로그인하고 준비하던 계획을 이어가세요.</p>
      <div class="login-roles" aria-label="로그인 역할">${['BUSINESS', 'SERVICE'].map(role => `<button type="button" data-role="${role}" class="${roleChoice === role ? 'selected' : ''}" aria-pressed="${roleChoice === role}">${icon(role === 'BUSINESS' ? 'shop' : 'database')}${role === 'BUSINESS' ? '사업체 운영자' : '서비스 운영자'}</button>`).join('')}</div>
      <form id="login-form" novalidate>
        <label class="field-label" for="login-email">이메일</label><input class="field" id="login-email" name="email" type="email" autocomplete="username" placeholder="이메일 주소를 입력하세요" required maxlength="254" aria-describedby="login-error">
        <label class="field-label" for="login-password">비밀번호</label><div class="password-field"><input class="field" id="login-password" name="password" type="password" autocomplete="current-password" placeholder="비밀번호를 입력하세요" required maxlength="128" aria-describedby="login-error"><button type="button" class="password-toggle" data-action="toggle-password" aria-label="비밀번호 보기" aria-pressed="false">${icon('eye')}</button></div>
        <p id="login-error" class="notice error" role="alert" hidden></p>
        <button class="button primary full login-submit" type="submit">로그인 ${icon('arrow')}</button>
      </form>
    </section></main>`;
}
async function submitLogin(form) {
  const email = $('#login-email'), password = $('#login-password'), submit = $('.login-submit', form);
  submit.disabled = true;
  try {
    await api('/api/auth/login', {method: 'POST', body: {role: roleChoice, email: email.value, password: password.value}});
    await loadSession();
    form.reset();
    navigate('overview');
    notify('로그인했습니다.');
  } catch (error) {
    const field = error.field === 'password' ? password : error.field === 'email' ? email : password;
    $('#login-error').textContent = error.message; $('#login-error').hidden = false;
    field.setAttribute('aria-invalid', 'true'); field.focus();
  } finally { submit.disabled = false; }
}

// ---------- 그래프·필터 ----------
function niceMax(values) {
  const max = Math.max(...values.filter(v => v !== null), 0);
  if (max <= 0) return 10;
  const step = Math.pow(10, Math.floor(Math.log10(max)));
  return Math.ceil(max / step) * step + (max % step === 0 ? step : 0);
}
function chartSVG(trend) {
  const values = trend.months.map(m => m.value);
  const max = niceMax(values), width = 620, top = 24, bottom = 186, left = 36, step = 50;
  const points = values.map((v, i) => [left + i * step, v === null ? null : bottom - v / max * (bottom - top)]);
  const segments = []; let segment = [];
  points.forEach(p => { if (p[1] === null) { if (segment.length) segments.push(segment); segment = []; } else segment.push(p); });
  if (segment.length) segments.push(segment);
  const grid = Array.from({length: 5}, (_, i) => { const val = max - i * max / 4, y = top + i * (bottom - top) / 4; return `<line class="gridline" x1="36" y1="${y}" x2="600" y2="${y}"/><text x="24" y="${y + 4}" text-anchor="end">${Number(val.toFixed(1))}</text>`; }).join('');
  const paths = segments.map(s => `<path d="M${s.map(p => p.join(',')).join('L')}" fill="none" stroke="#528451" stroke-width="2.8" stroke-linecap="round" stroke-linejoin="round"/>`).join('');
  const marks = points.map((p, i) => p[1] === null
    ? `<text x="${p[0]}" y="${bottom - 9}" text-anchor="middle" fill="#8a4b16">미확보</text>`
    : `<circle cx="${p[0]}" cy="${p[1]}" r="3.5" fill="#528451" stroke="white" stroke-width="2"><title>${i + 1}월: ${values[i]}${trend.unitLabel}</title></circle>`).join('');
  return `<svg class="chart" viewBox="0 0 ${width} 221" role="img" aria-label="${escapeHTML(trend.regionName)} ${trend.year}년 월별 ${trend.metric === 'VISIT' ? '방문 집계' : '카드 소비'}, 단위 ${trend.unitLabel}. 수치는 아래 표에서 확인할 수 있습니다.">${grid}${paths}${marks}${values.map((_, i) => `<text x="${left + i * step}" y="213" text-anchor="middle">${i + 1}월</text>`).join('')}</svg>`;
}
function chartTable(trend) {
  return `<details class="chart-table"><summary>월별 수치 표로 보기</summary><div class="table-wrap"><table><caption class="sr-only">${trend.year}년 ${escapeHTML(trend.regionName)} 월별 ${trend.unitLabel}</caption><thead><tr>${trend.months.map(m => `<th scope="col">${m.month}월</th>`).join('')}</tr></thead><tbody><tr>${trend.months.map(m => `<td>${m.value === null ? '미확보' : m.value}</td>`).join('')}</tr></tbody></table></div></details>`;
}
function sourceLine(source) {
  if (!source) return '';
  return `출처: ${escapeHTML(source.providerBasis)} · ${escapeHTML(source.sourceVersion)} · ${String(source.startMonth).slice(0, 7)}부터 ${String(source.endMonth).slice(0, 7)}까지 · ${escapeHTML(source.unitLabel)}`;
}
function trendPanel(trend) {
  const visit = trend.metric === 'VISIT';
  const toggle = `<div class="segmented" aria-label="차트 지표"><button data-metric="VISIT" aria-pressed="${visit}" class="${visit ? 'selected' : ''}">방문</button><button data-metric="SPEND" aria-pressed="${!visit}" class="${!visit ? 'selected' : ''}">소비</button></div>`;
  if (!trend.hasData) {
    return `<section class="panel"><div class="chart-box"><div class="panel-heading"><div><h2>우리 지역의 계절 흐름</h2><p>${escapeHTML(trend.regionName)} · ${trend.year}년</p></div>${toggle}</div><div class="empty-state" id="analysis-empty" role="status"><div class="empty-icon">${icon('database')}</div><h3>${escapeHTML(trend.message)}</h3><p>없는 수치를 만들거나 0으로 바꾸지 않습니다.${trend.availableYears.length ? ' 연도나 지역을 바꿔 다시 조회해 주세요.' : ' 서비스 운영자가 자료를 등록하면 그래프가 표시됩니다.'}</p></div></div></section>`;
  }
  session.lastSource = trend.source;
  const summary = trend.peak && trend.low ? `가장 많은 달 ${trend.peak.month}월 ${trend.peak.value}${trend.unitLabel} · 가장 적은 달 ${trend.low.month}월 ${trend.low.value}${trend.unitLabel}` : '';
  return `<section class="panel"><div class="chart-box"><div class="panel-heading"><div><h2>우리 지역의 계절 흐름</h2><p>${escapeHTML(trend.regionName)} · ${trend.year}년 1월부터 12월</p></div>${toggle}</div><div class="chart-summary"><strong>${summary}</strong></div>${chartSVG(trend)}<div class="chart-note"><span><i class="legend-dot"></i>${visit ? '지역 방문 집계' : '카드 소비'} · ${trend.unitLabel}</span></div>${chartTable(trend)}</div><div class="chart-foot">${icon('info')}<span>${sourceLine(trend.source)}</span><button class="text-button" data-action="source">자료 기준 ${icon('chevron')}</button></div></section>`;
}
function filters({years = null, year = null} = {}) {
  const regionOptions = session.regions.map(r => `<option value="${escapeHTML(r.regionCode)}" ${r.regionCode === session.region ? 'selected' : ''}>${escapeHTML(r.regionName)}</option>`).join('');
  const yearPart = years
    ? `<label class="filter-pill">${icon('calendar')}분석 연도 <select id="analysis-year" aria-label="분석 연도">${(years.includes(year) ? years : [year, ...years]).map(y => `<option value="${y}" ${y === year ? 'selected' : ''}>${y}년${years.includes(y) ? '' : ' · 자료 없음'}</option>`).join('')}</select></label>`
    : `<span class="filter-pill">${icon('calendar')}분석 자료 <b>${year ? `${year}년` : '미등록'}</b></span>`;
  return `<div class="filter-strip"><label class="filter-pill">${icon('pin')}분석 지역 <select aria-label="분석 지역" id="region">${regionOptions}</select></label>${yearPart}<span class="filter-divider"></span><span class="filter-note">계획 대상 <b>${escapeHTML(session.period?.label || '2026년 10월부터 12월까지')}</b></span></div>`;
}
function rememberPeriod(calendar) {
  if (calendar) {
    session.period = {start: calendar.periodStart, end: calendar.periodEnd, label: calendar.periodLabel, months: calendar.months.map(m => m.month)};
    session.planCount = calendar.planCount;
  }
}

// ---------- 한눈에 보기 (SCR-C-002) ----------
function timeline(calendar) {
  if (!calendar) return '';
  return `<section class="panel timeline-panel"><div class="timeline-intro"><h3>우리 가게의 3개월</h3><p>${calendar.planCount}개 계획을 준비하고 있어요</p></div><div class="timeline-months">${calendar.months.map(m => `<div class="timeline-item"><b>${Number(m.month.slice(5))}월</b><span>${m.plans.length ? `${m.plans.length}개 계획 준비 중` : '새 계획을 담아보세요'}</span></div>`).join('')}</div><a class="button small" href="#calendar">일정 보기 ${icon('chevron')}</a></section>`;
}
function suggestionSummary(list) {
  const cards = list.months.flatMap(m => m.suggestions);
  const remaining = cards.filter(c => !c.addedPlanId).length;
  const action = list.canAddToPlan && remaining ? `<button class="button small primary" data-action="bulk-add">${icon('plus')}제안 ${remaining}개 한 번에 담기</button>` : '';
  const body = cards.length
    ? `<div class="recommendation-grid">${cards.map(c => `<article class="recommendation recommendation-summary"><div><span class="badge ${monthOf(c.targetMonth) === 11 ? 'amber' : 'green'}">${monthOf(c.targetMonth)}월 · ${escapeHTML(c.tag)}</span><h3>${escapeHTML(c.title)}</h3><p>${escapeHTML(c.description)}</p></div><a class="button small" href="#recommendations">${c.addedPlanId ? '일정에 추가됨' : '제안 상세보기'} ${icon('chevron')}</a></article>`).join('')}</div>`
    : `<section class="panel empty-state" role="status"><h3>${escapeHTML(list.message || '조건에 맞는 제안이 없습니다')}</h3><p>근거가 없는 제안은 만들지 않습니다.</p></section>`;
  return `<div class="section-header"><div><h2>작은 실행으로 이어지는 홍보 제안</h2><p>우리 가게에 맞는지 확인하고 일정에 담아보세요.</p></div><div class="row">${action}<a class="text-button" href="#recommendations">홍보 제안 조회 ${icon('chevron')}</a></div></div>${body}${list.criteria ? `<p class="source-inline">추천 기준: 직전 월 대비 방문 감소 ${list.criteria.declineRateThreshold}% 이상 · ${escapeHTML(list.criteria.sourceInfo)}</p>` : ''}`;
}
/** UC11 활용 사례: 화면에 고정한 검증 전 예시 (DB에 저장하지 않음) */
function casesSection() {
  const cases = [
    {title: '평일 오후 음료 세트 안내', activity: '매장 안내물과 인스타그램 게시물로 실내 메뉴를 소개', result: '예시: 쿠폰 사용 수를 직접 세어 기록', basis: '운영자가 매장에서 확인한 수치만 입력'},
    {title: '가을 산책 동선과 가게 위치 소개', activity: '대표 메뉴 사진과 찾아오는 길 안내를 게시', result: '예시: 게시물 클릭 수를 플랫폼 통계에서 확인', basis: '플랫폼에서 보이는 수치만 입력, 매출 효과는 추정하지 않음'}
  ];
  return `<div class="section-header"><div><h2>참고 사례</h2><p>다른 가게가 기록하는 방식을 참고해 보세요.</p></div><span class="badge amber">검증 전 예시</span></div><div class="two-col">${cases.map(c => `<section class="panel panel-padding"><h3>${c.title}</h3><ul class="relation-list"><li><b>실행 활동</b>${c.activity}</li><li><b>결과</b>${c.result}</li><li><b>수집 기준</b>${c.basis}</li></ul></section>`).join('')}</div><p class="source-inline">사례명·성과 수치는 검증 전 예시이며 실제 성과로 인용하지 않습니다.</p>`;
}
function dataStatusSection(status) {
  return `<div class="section-header"><div><h2>자료 준비 현황</h2><p>검증을 통과한 자료 ${status.sourceCount}개 · ${status.totalRows}행</p></div><span class="badge">${status.readyPercent}%</span></div><section class="panel panel-padding"><ul class="relation-list">${status.items.map(i => `<li><b>${escapeHTML(i.label)}</b><span class="badge ${i.ready ? 'green' : 'amber'}">${i.ready ? '준비됨' : '준비 전'}</span> ${escapeHTML(i.detail)}</li>`).join('')}</ul><p class="small muted">${escapeHTML(status.notice)}</p></section>`;
}
async function overview() {
  const data = await api(`/api/overview?${query({regionCode: session.region, businessId: session.businessId, metric: session.metric})}`);
  session.region = data.trend.regionCode;
  rememberPeriod(data.calendar);
  return `${heading('우리 가게의 다음 계절', '지역의 흐름을 읽고, 실행할 수 있는 홍보 계획을 세워보세요.')}${filters({year: data.trend.hasData ? data.trend.year : null})}<div class="overview-trend">${trendPanel(data.trend)}</div>${timeline(data.calendar)}${suggestionSummary(data.suggestions)}${casesSection()}${dataStatusSection(data.dataStatus)}`;
}

// ---------- 지역 분석 (SCR-C-003) ----------
function regionMap(trend) {
  if (!trend.regions.length) return '';
  const startMonth = Number(trend.changeLabel.split('월')[0]);
  const buttons = trend.regions.map((r, i) => `<button class="map-region ${i < 2 ? `region-${i}` : ''} ${r.regionCode === session.region ? 'selected' : ''}" data-region-map="${escapeHTML(r.regionCode)}" aria-pressed="${r.regionCode === session.region}">${icon('pin')}<b>${escapeHTML(r.regionName)}</b><small>${r.value === null ? '미확보' : `${r.value}${trend.unitLabel}`} · ${startMonth}월</small></button>`);
  return `<section class="panel panel-padding"><div class="panel-heading"><div><h2>지역을 선택해 비교해요</h2><p>분석 지역 위치 개념도 · 실제 경계·축척과 다름</p></div><span class="badge">${trend.regions.length}개 지역</span></div><div class="region-sketch" role="group" aria-label="제주 분석 지역 위치 개념도"><span class="sketch-north">북 ↑</span><div class="island-shape" aria-hidden="true"></div>${buttons.slice(0, 2).join('')}<span class="sketch-caption">제주도 위치 개념도</span></div>${buttons.length > 2 ? `<div class="row wrap" style="margin-top:12px">${buttons.slice(2).join('')}</div>` : ''}<p class="source-inline">${sourceLine(trend.source)}</p></section>`;
}
function seasonTable(trend) {
  if (!trend.seasonTable.length) return '';
  return `<div class="section-header"><h2>성수기·비수기 비교</h2><span class="badge">${trend.year}년 · 같은 자료 기준</span></div><section class="panel"><div class="table-wrap"><table><thead><tr><th scope="col">지역</th><th scope="col">가장 많은 달</th><th scope="col">가장 적은 달</th><th scope="col">${escapeHTML(trend.changeLabel)}</th></tr></thead><tbody>${trend.seasonTable.map(r => `<tr><td><b>${escapeHTML(r.regionName)}</b></td><td>${r.peak ? `${r.peak.month}월 · ${r.peak.value}${trend.unitLabel}` : '미확보'}</td><td>${r.low ? `${r.low.month}월 · ${r.low.value}${trend.unitLabel}` : '미확보'}</td><td>${r.changeRate === null ? '비교 불가' : `${r.changeRate}%`}</td></tr>`).join('')}</tbody></table></div><p class="table-caption">${sourceLine(trend.source)} · 변화율 = (다음 달 − 시작 달) ÷ 시작 달 × 100 · 가게 손님 수와 구분</p></section>`;
}
function consumptionView(data) {
  const rows = data.visitSpend.filter(r => r.visit !== null || r.spend !== null);
  const visitSpend = rows.length
    ? `<div class="table-wrap"><table><thead><tr><th>기준월</th><th>지역 방문</th><th>카드 소비</th><th>비교 가능 여부</th></tr></thead><tbody>${rows.map(r => `<tr><td>${data.year}년 ${r.month}월</td><td>${r.visit === null ? '미확보' : `${r.visit}만 명`}</td><td>${r.spend === null ? '미확보' : `${r.spend}억 원`}</td><td>${escapeHTML(r.note)}</td></tr>`).join('')}</tbody></table></div>`
    : `<div class="empty-state" role="status"><h3>${data.year}년 방문·소비 자료가 없습니다</h3><p>없는 수치를 만들거나 0으로 바꾸지 않습니다.</p></div>`;
  const s = data.shares;
  const shares = s.hasData
    ? `<div class="two-col consumption-tables"><section><h3>내국인·외국인 소비 비중</h3><p class="small muted">${data.year}년 ${data.shareMonth}월 · 업종별 소비액 안의 비중</p><div class="table-wrap"><table><thead><tr><th>업종</th><th>내국인</th><th>외국인</th><th>비교</th></tr></thead><tbody>${s.segments.map(r => `<tr><td>${escapeHTML(label(CATEGORY, r.category))}</td><td>${r.domestic === null ? '미확보' : `${r.domestic}%`}</td><td>${r.foreign === null ? '미확보' : `${r.foreign}%`}</td><td>${r.comparable ? '비교 가능' : escapeHTML(r.note)}</td></tr>`).join('')}</tbody></table></div></section><section id="timeband-table"><h3>시간대별 소비</h3><p class="small muted">${data.year}년 ${data.shareMonth}월 · ${escapeHTML(label(CATEGORY, s.timeSlotCategory))}</p><div class="table-wrap"><table><thead><tr><th>시간대</th><th>소비 비중</th></tr></thead><tbody>${s.timeSlots.length ? s.timeSlots.map(t => `<tr><td>${escapeHTML(t.label)}</td><td>${t.share === null ? '미확보' : `${t.share}%`}</td></tr>`).join('') : '<tr><td colspan="2">미확보</td></tr>'}</tbody></table></div></section></div><p class="source-inline">${sourceLine(s.source)}</p>`
    : `<p class="notice" style="margin-top:16px">${escapeHTML(s.message)}</p>`;
  return `<section class="panel panel-padding" id="consumption-comparison"><div class="panel-heading"><div><h2>방문과 소비를 함께 살펴봐요</h2><p>${escapeHTML(data.regionName)} · ${data.year}년</p></div></div>${visitSpend}<p class="source-inline">${data.visitSource ? `방문 ${sourceLine(data.visitSource)}` : '방문 자료 미등록'}<br>${data.spendSource ? `소비 ${sourceLine(data.spendSource)}` : '소비 자료 미등록'}<br>${escapeHTML(data.notice)}</p>${shares}</section>`;
}
async function analysis() {
  const tabs = `<div class="source-tabs" aria-label="분석 항목"><button data-analysis-mode="visits" aria-pressed="${session.analysisMode === 'visits'}" class="${session.analysisMode === 'visits' ? 'selected' : ''}">방문·계절 비교</button><button data-analysis-mode="consumption" aria-pressed="${session.analysisMode === 'consumption'}" class="${session.analysisMode === 'consumption' ? 'selected' : ''}">소비 비교</button></div>`;
  const head = heading('지역의 흐름을 이해하는 시간', '방문과 소비를 각각 비교하고, 자료의 기준부터 확인하세요.');
  if (session.analysisMode === 'consumption') {
    const data = await api(`/api/analysis/consumption?${query({regionCode: session.region, year: session.year, category: business()?.category})}`);
    session.region = data.regionCode; session.year = data.year;
    return `${head}${filters({years: data.availableYears, year: data.year})}${tabs}${consumptionView(data)}`;
  }
  const trend = await api(`/api/analysis/trend?${query({regionCode: session.region, year: session.year, metric: session.metric})}`);
  session.region = trend.regionCode; session.year = trend.year;
  return `${head}${filters({years: trend.availableYears, year: trend.year})}${tabs}<div class="two-col analysis-map-grid">${regionMap(trend)}${trendPanel(trend)}</div>${seasonTable(trend)}`;
}

// ---------- 홍보 제안 (SCR-C-004) ----------
let lastSuggestions = null;
function recommendationCard(c, canAdd) {
  const added = !!c.addedPlanId;
  const items = c.items.map(i => `<dt>${label(ITEM_LABELS, i.itemType)}</dt><dd>${escapeHTML(i.content)}${i.reason ? `<small>이유: ${escapeHTML(i.reason)}${i.checkNote ? ` · ${escapeHTML(i.checkNote)}` : ''}</small>` : ''}</dd>`).join('');
  const addButton = canAdd ? `<button class="button small ${added ? 'secondary' : ''}" data-rec="${c.suggestionId}">${icon(added ? 'check' : 'plus')}${added ? '일정에 추가됨' : '일정에 담기'}</button>` : '';
  return `<article class="recommendation"><div class="recommendation-top"><div class="rec-symbol ${monthOf(c.targetMonth) === 11 ? '' : 'green'}">${icon(monthOf(c.targetMonth) === 11 ? 'cup' : 'leaf')}</div><span class="badge ${monthOf(c.targetMonth) === 11 ? 'amber' : 'green'}">${monthOf(c.targetMonth)}월 · ${escapeHTML(c.tag)}</span></div><h3>${escapeHTML(c.title)}</h3><p>${escapeHTML(c.description)}</p><dl class="proposal-fields">${items}</dl><p class="proposal-reason"><b>판단 이유</b> ${escapeHTML(c.reason)}${lastSuggestions?.criteria ? ` ${escapeHTML(lastSuggestions.criteria.reasonTemplate)}` : ''}</p><p class="source-inline">${sourceLine(c.source)}</p>${lastSuggestions?.criteria ? `<p class="small muted">적용 조건: ${escapeHTML(lastSuggestions.criteria.requiredCondition)}</p>` : ''}<div class="rec-meta"><span>${icon('pin')}${escapeHTML(regionName(c.regionCode))}</span><span>${icon('calendar')}${String(c.targetMonth).slice(0, 4)}년 ${monthOf(c.targetMonth)}월</span></div><div class="rec-bottom"><button class="text-button" data-evidence="${c.suggestionId}">${icon('network')}추천 근거</button>${addButton}</div></article>`;
}
async function recommendations() {
  const list = await api(`/api/suggestions?${query({regionCode: session.region, businessId: session.businessId})}`);
  lastSuggestions = list;
  session.region = list.regionCode;
  const remaining = list.months.flatMap(m => m.suggestions).filter(c => !c.addedPlanId).length;
  const bulk = list.canAddToPlan && remaining ? button(`남은 제안 ${remaining}개 한 번에 담기`, 'bulk-add', 'primary', 'plus') : '';
  const months = list.months.map(m => {
    const month = Number(m.month.slice(5));
    const add = list.canAddToPlan ? `<button class="plan-add" data-month="${month}">${icon('plus')}${month}월 마케팅 제안 직접 추가</button>` : '';
    const body = m.suggestions.length ? `<div class="recommendation-grid">${m.suggestions.map(c => recommendationCard(c, list.canAddToPlan)).join('')}</div>` : `<section class="panel empty-state" role="status"><h3>${month}월은 기준에 맞는 제안이 없습니다</h3><p>근거가 없는 제안은 만들지 않습니다. 직접 계획을 작성할 수 있어요.</p></section>`;
    return `<div class="section-header"><h2>${month}월 제안 ${m.suggestions.length}개</h2></div>${body}${add}`;
  }).join('');
  const intro = `<section class="intro-callout">${icon('info')}<div><h3>판단 이유와 출처를 함께 확인하세요</h3><p>판매 가능 상품·비용·휴무일·접근 경로는 적용 전에 확인해 주세요. ${list.canAddToPlan ? '' : '일정 담기는 처리할 수 있는 사업체의 지역에서만 가능합니다.'}</p></div></section>`;
  return `${heading('우리 가게에 맞는 홍보 아이디어', '근거를 살펴보고, 운영할 수 있는 계획을 골라보세요.', bulk)}${filters({year: null})}${intro}<div class="section-header"><h2>${escapeHTML(list.periodLabel)} 제안 ${list.totalCount}개</h2>${list.criteria ? `<span class="badge">방문 감소 기준 ${list.criteria.declineRateThreshold}% 이상</span>` : ''}</div>${list.message ? `<p class="notice">${escapeHTML(list.message)}</p>` : ''}${months}`;
}
async function evidenceDialog(id) {
  try {
    const d = await api(`/api/suggestions/${id}?${query({businessId: session.businessId})}`);
    const c = d.card;
    // 좁은 측면 패널에서 가로로 넘치지 않도록 6개월씩 두 줄로 보여줌
    const half = list => `<tr>${list.map(m => `<th scope="col">${m.month}월</th>`).join('')}</tr><tr>${list.map(m => `<td>${m.value === null ? '미확보' : m.value}</td>`).join('')}</tr>`;
    const observed = `<div class="table-wrap"><table><tbody>${half(d.observed.slice(0, 6))}${half(d.observed.slice(6))}</tbody></table></div>`;
    const add = d.canAddToPlan ? `<button class="button primary" data-rec="${c.suggestionId}">${icon('plus')}${c.addedPlanId ? '담은 계획 수정' : '일정에 담기'}</button>` : '';
    openDialog('이 제안의 근거', '자료에서 일정까지 연결해 살펴봐요', `<span class="badge amber">${monthOf(c.targetMonth)}월 제안</span><h3 class="evidence-title">${escapeHTML(c.title)}</h3><div class="evidence-row"><span class="evidence-number">1</span><div><h3>지역의 과거 흐름</h3><p>${escapeHTML(c.reason)}</p>${observed}<span class="badge">${escapeHTML(regionName(c.regionCode))} · ${d.baseYear}년 방문 자료 (만 명)</span><p class="source-inline">${sourceLine(c.source)}</p></div></div><div class="evidence-row"><span class="evidence-number">2</span><div><h3>확인할 운영 조건</h3><ul class="relation-list">${d.confirmedConditions.map(x => `<li><b>확인됨</b>${escapeHTML(x)}</li>`).join('')}${d.uncheckedConditions.map(x => `<li><b>확인 필요</b>${escapeHTML(x)}</li>`).join('')}</ul><span class="badge amber">운영자 확인 필요</span></div></div><div class="evidence-row"><span class="evidence-number">3</span><div><h3>계획으로 옮길 행동</h3><p>${escapeHTML(c.preparationTask)}</p><span class="badge green">${String(c.targetMonth).slice(0, 4)}년 ${monthOf(c.targetMonth)}월 계획 후보</span></div></div><div class="notice">${escapeHTML(d.notice)}</div><div class="dialog-actions">${button('자료 기준', 'source')}${add}</div>`, true);
  } catch (error) { notify(error.message); }
}

// ---------- 3개월 일정표·계획 (SCR-B-001, P01) ----------
function planCard(p) {
  return `<article class="plan-item"><div class="plan-date"><span>${monthOf(p.scheduledDate)}월 ${dayOf(p.scheduledDate)}일 · ${escapeHTML(label(CHANNELS, p.channel))}</span>${p.hasRecord ? '<span class="accent">기록 완료</span>' : ''}</div><h3>${escapeHTML(p.title)}</h3><p>${escapeHTML(p.preparationNote || '운영할 수 있는 조건을 확인해 주세요.')}</p>${p.suggestionId ? '<span class="badge green">제안에서 담음</span>' : ''}<div class="row between"><button class="text-button" data-edit="${p.planId}">${icon('edit')}계획 수정</button><button class="text-button" data-record="${p.planId}">${p.hasRecord ? '기록 보기' : '실행 기록'} ${icon('chevron')}</button></div></article>`;
}
async function calendar() {
  if (!session.businessId) return needBusiness();
  const data = await api(`/api/businesses/${session.businessId}/calendar`);
  rememberPeriod(data);
  const tags = ['가을 준비', '겨울 시작', '연말 운영'];
  const columns = data.months.map((m, i) => {
    const month = Number(m.month.slice(5));
    return `<section class="month-column"><header class="month-heading"><div><small>${m.month.slice(0, 4)}</small><b>${month}월</b></div><span class="badge ${i === 1 ? 'amber' : 'green'}">${tags[i] || ''}</span></header><div class="month-content">${m.plans.length ? m.plans.map(planCard).join('') : `<div class="month-empty">${icon('calendar')}<p>이달의 계획을 채워보세요.</p></div>`}<button class="plan-add" data-month="${month}">${icon('plus')}${month}월 계획 추가</button></div></section>`;
  }).join('');
  return `${heading('생각을 실행할 일정으로', '홍보 제안을 담거나, 우리 가게의 계획을 직접 작성하세요.', button('직접 계획 추가', 'new-plan', 'primary', 'plus'))}<div class="toolbar"><div class="row"><h2>${escapeHTML(data.periodLabel)}</h2><span class="badge green">${data.planCount}개 계획</span></div><div class="row">${button('홍보 제안 자동 채우기', 'bulk-add', '', 'spark')}<a class="button" href="#recommendations">홍보 제안 조회</a></div></div><div class="calendar-grid">${columns}</div><div class="steps-line"><b>실행 가능한 계획 만들기</b><span>근거 확인</span>${icon('chevron')}<span>상품·일정 수정</span>${icon('chevron')}<span>저장</span>${icon('chevron')}<span>실행 기록 남기기</span></div>`;
}
async function ensurePeriod() {
  if (!session.period && session.businessId) rememberPeriod(await api(`/api/businesses/${session.businessId}/calendar`));
  return session.period;
}
async function planDialog({planId = null, month = null, suggestionId = null} = {}) {
  if (!session.businessId) { notify('계획을 관리할 수 있는 사업체가 없습니다.'); return; }
  try {
    const period = await ensurePeriod();
    let plan = null, suggestion = null;
    if (suggestionId) {
      suggestion = lastSuggestions?.months.flatMap(m => m.suggestions).find(c => c.suggestionId === suggestionId)
        || (await api(`/api/suggestions/${suggestionId}?${query({businessId: session.businessId})}`)).card;
      if (suggestion.addedPlanId) { notify('이미 담은 제안입니다. 저장된 계획을 수정할 수 있어요.'); planId = suggestion.addedPlanId; suggestion = null; }
    }
    if (planId) plan = await api(`/api/plans/${planId}`);
    const firstMonth = period.months[0];
    const defaultDate = month ? `${period.months.find(m => Number(m.slice(5)) === month) || firstMonth}-15` : `${firstMonth}-15`;
    const v = plan || {title: suggestion?.title || '', scheduledDate: suggestion?.recommendedDate || defaultDate, channel: suggestion?.channel || 'INSTAGRAM', preparationNote: suggestion?.preparationTask || ''};
    openDialog(plan ? '계획 수정' : '일정에 담을 계획', escapeHTML(period.label), `<form id="plan-form" data-plan-id="${plan?.planId || ''}" data-suggestion-id="${suggestion?.suggestionId || ''}" novalidate><label class="field-label" for="plan-title">어떤 활동을 할까요?</label><input class="field" id="plan-title" name="title" required maxlength="33" value="${escapeHTML(v.title)}" placeholder="예: 겨울 메뉴 사진 촬영"><p class="hint">한글 기준 33자까지 입력할 수 있어요.</p><div class="form-row"><div><label class="field-label" for="plan-date" style="margin-top:18px">예정 날짜</label><input class="field" id="plan-date" name="scheduledDate" type="date" min="${period.start}" max="${period.end}" required value="${escapeHTML(v.scheduledDate)}"></div><div><label class="field-label" for="plan-channel" style="margin-top:18px">홍보 채널</label><select class="field" id="plan-channel" name="channel">${Object.entries(CHANNELS).map(([code, name]) => `<option value="${code}" ${code === v.channel ? 'selected' : ''}>${name}</option>`).join('')}</select></div></div><label class="field-label" for="plan-memo">준비할 내용</label><textarea class="field" id="plan-memo" name="preparationNote" maxlength="166" placeholder="상품, 준비물, 확인할 조건을 적어보세요.">${escapeHTML(v.preparationNote || '')}</textarea><p class="hint">상품 판매 가능 여부와 휴무일은 직접 확인해 주세요.</p><p id="plan-error" class="notice error" role="alert" hidden></p><div class="dialog-actions">${plan ? `<button type="button" class="text-button danger" data-delete="${plan.planId}">계획 삭제</button>` : ''}<button type="button" class="button" data-action="close-dialog">취소</button><button class="button primary" type="submit">${icon('check')}저장</button></div></form>`);
  } catch (error) { notify(error.message); }
}
const PLAN_FIELDS = {title: 'plan-title', scheduledDate: 'plan-date', channel: 'plan-channel', preparationNote: 'plan-memo'};
async function submitPlan(form) {
  const data = new FormData(form);
  const body = {title: data.get('title'), scheduledDate: data.get('scheduledDate') || null, channel: data.get('channel'), preparationNote: data.get('preparationNote'), suggestionId: form.dataset.suggestionId ? Number(form.dataset.suggestionId) : null};
  const submit = $('button[type="submit"]', form); submit.disabled = true;
  try {
    if (form.dataset.planId) await api(`/api/plans/${form.dataset.planId}`, {method: 'PUT', body});
    else await api(`/api/businesses/${session.businessId}/plans`, {method: 'POST', body});
    closeDialog(); await render(); notify('계획을 저장했습니다.');
  } catch (error) {
    $('#plan-error').hidden = false; $('#plan-error').textContent = error.message;
    if (error.field && PLAN_FIELDS[error.field]) $(`#${PLAN_FIELDS[error.field]}`).focus();
  } finally { submit.disabled = false; }
}
async function bulkAdd() {
  if (!session.businessId) return;
  try {
    const result = await api(`/api/businesses/${session.businessId}/plans/from-suggestions`, {method: 'POST'});
    await render(); notify(result.message);
  } catch (error) { notify(`저장에 실패했습니다. 기존 계획은 유지됩니다. ${error.message}`); }
}

// ---------- 실행 기록 (SCR-B-002, P01, P02) ----------
async function records() {
  if (!session.businessId) return needBusiness();
  const data = await api(`/api/businesses/${session.businessId}/records`);
  session.planCount = data.planCount;
  const rows = data.rows.length ? data.rows.map(({plan: p, record: r}) => `<div class="record-row"><div class="date-box"><small>${monthOf(p.scheduledDate)}월</small><strong>${dayOf(p.scheduledDate)}</strong></div><div><h3>${escapeHTML(p.title)}</h3><p>${escapeHTML(label(CHANNELS, p.channel))} · ${r ? `${label(EXEC, r.values.execStatus)} / 클릭 ${display(r.values.checkedClickCount)}` : '실행 결과를 아직 입력하지 않았어요'}</p></div><span class="badge ${r ? 'green' : ''}">${r ? '기록 있음' : '미기록'}</span><button class="button small" data-record="${p.planId}">${r ? '기록 수정' : '기록 입력'}</button></div>`).join('')
    : `<div class="empty-state"><div class="empty-icon">${icon('clipboard')}</div><h3>먼저 작은 계획을 만들어 볼까요?</h3><p>일정에서 계획을 추가하면 이곳에서 실행 여부와 결과를 기록할 수 있어요.</p><a class="button primary" href="#calendar">일정 작성하기</a></div>`;
  return `${heading('실행한 만큼, 다음 계획이 선명해져요', '계획별 실행 여부와 실제로 수집한 결과를 기록하세요.', button('사용 결과 요약', 'report', '', 'book'))}<div class="mini-stats"><div class="mini-stat"><p>준비한 계획</p><strong>${data.planCount}<small>개</small></strong><span>${escapeHTML(data.periodLabel)}</span></div><div class="mini-stat"><p>실행 기록 작성</p><strong>${data.recordedCount}<small>개</small></strong><span>입력한 기록 기준</span></div><div class="mini-stat"><p>아직 기록하지 않은 계획</p><strong>${data.unrecordedCount}<small>개</small></strong><span>기록 없음은 실행 실패를 뜻하지 않아요</span></div></div><section class="panel">${rows}</section><p class="notice" style="margin-top:20px">수집하지 않은 클릭·예약·쿠폰 사용 수는 빈칸으로 남겨 주세요. 0건과 미확보는 다르게 기록됩니다.</p>`;
}
async function recordDialog(planId) {
  try {
    const f = await api(`/api/plans/${planId}/record`);
    const v = f.values;
    const count = (name, text) => `<div><label class="field-label" for="record-${name}">${text}</label><input class="field" id="record-${name}" name="${name}" type="number" min="0" max="99999999" step="1" placeholder="미확보" value="${v[name] ?? ''}"></div>`;
    openDialog(f.prefilled ? '실행 기록 남기기' : '실행 기록 수정', escapeHTML(f.plan.title), `<form id="record-form" data-plan-id="${planId}" novalidate>${f.prefilled ? '<p class="notice">계획의 날짜·채널·준비 내용을 기본값으로 채웠어요. 실제로 실행한 내용으로 확인해 주세요.</p>' : ''}<div class="form-row">${count('spentTimeMin', '계획 소요 시간 · 분')}<div><label class="field-label" for="record-adoptionStatus">추천 채택 여부</label><select class="field" id="record-adoptionStatus" name="adoptionStatus">${Object.entries(ADOPT).map(([code, name]) => `<option value="${code}" ${code === v.adoptionStatus ? 'selected' : ''}>${name}</option>`).join('')}</select></div></div><label class="field-label" for="record-execStatus">실행 여부</label><select class="field" id="record-execStatus" name="execStatus">${Object.entries(EXEC).map(([code, name]) => `<option value="${code}" ${code === v.execStatus ? 'selected' : ''}>${name}</option>`).join('')}</select><label class="field-label" for="record-executedDate">실제 실행 날짜</label><input class="field" id="record-executedDate" name="executedDate" type="date" value="${escapeHTML(v.executedDate || '')}" ${v.execStatus === 'DONE' ? 'required' : ''}><label class="field-label" for="record-actualActivity">실제로 실행한 활동 · 선택한 내용</label><textarea class="field" id="record-actualActivity" name="actualActivity" maxlength="166">${escapeHTML(v.actualActivity || '')}</textarea><div class="form-row record-counts">${count('checkedClickCount', '확인한 클릭 수')}${count('checkedBookingCount', '확인한 예약 수')}${count('couponUsageCount', '확인한 쿠폰 사용 수')}</div><p class="hint">빈칸은 미확보, 숫자 0은 실제로 확인한 0건입니다. 광고·예약·쿠폰 발행은 대신 실행하지 않습니다.</p><label class="field-label" for="record-resultMemo">결과</label><textarea class="field" id="record-resultMemo" name="resultMemo" maxlength="166" placeholder="확인한 결과와 다음에 바꿀 점을 적어주세요.">${escapeHTML(v.resultMemo || '')}</textarea><label class="field-label" for="record-collectionMethod">수집 방법</label><input class="field" id="record-collectionMethod" name="collectionMethod" maxlength="66" value="${escapeHTML(v.collectionMethod || '')}" placeholder="예: 인스타그램 통계, 매장 쿠폰 회수"><p id="record-error" class="notice error" role="alert" hidden></p><div class="dialog-actions"><button type="button" class="button" data-action="close-dialog">취소</button><button class="button primary" type="submit">기록 저장</button></div></form>`);
  } catch (error) { notify(error.message); }
}
async function submitRecord(form) {
  const data = new FormData(form);
  const num = name => data.get(name) === '' ? null : Number(data.get(name));
  const body = {execStatus: data.get('execStatus'), executedDate: data.get('executedDate') || null, actualActivity: data.get('actualActivity'), adoptionStatus: data.get('adoptionStatus'), spentTimeMin: num('spentTimeMin'), checkedClickCount: num('checkedClickCount'), checkedBookingCount: num('checkedBookingCount'), couponUsageCount: num('couponUsageCount'), resultMemo: data.get('resultMemo'), collectionMethod: data.get('collectionMethod')};
  const bad = ['spentTimeMin', 'checkedClickCount', 'checkedBookingCount', 'couponUsageCount'].find(k => body[k] !== null && !Number.isInteger(body[k]));
  if (bad) { $('#record-error').hidden = false; $('#record-error').textContent = '숫자 칸은 0 이상의 정수로 입력하거나 비워 두세요.'; $(`#record-${bad}`).focus(); return; }
  const submit = $('button[type="submit"]', form); submit.disabled = true;
  try {
    await api(`/api/plans/${form.dataset.planId}/record`, {method: 'PUT', body});
    closeDialog(); await render(); notify('실행 기록을 저장했습니다.');
  } catch (error) {
    $('#record-error').hidden = false; $('#record-error').textContent = error.message;
    if (error.field) $(`#record-${error.field}`)?.focus();
  } finally { submit.disabled = false; }
}
async function reportDialog() {
  try {
    const s = await api(`/api/businesses/${session.businessId}/records/summary`);
    const total = (t, unit = '건') => t.total === null ? '미확보' : `${t.total}${unit}<small> / 입력 ${t.inputCount}개 기준</small>`;
    const rows = s.rows.map(({plan: p, record: r}) => `<tr><td><b>${escapeHTML(p.title)}</b><br>${escapeHTML(p.scheduledDate)}<br>${escapeHTML(label(CHANNELS, p.channel))}</td><td>${r ? `${label(EXEC, r.values.execStatus)} · ${r.values.executedDate || '날짜 미기록'}<br>채택: ${label(ADOPT, r.values.adoptionStatus)}<br>${escapeHTML(r.values.actualActivity || '활동 미기록')}` : '기록 없음'}</td><td>${r ? `계획 ${display(r.values.spentTimeMin, '분')}<br>클릭 ${display(r.values.checkedClickCount)} · 예약 ${display(r.values.checkedBookingCount)}<br>쿠폰 사용 ${display(r.values.couponUsageCount)}` : '미확보'}</td></tr>`).join('');
    openDialog('사용 결과 요약', '저장한 계획과 입력한 실제 활동 비교', `<span class="badge green">입력 기록 요약</span><div class="summary-stats"><span>계획 <b>${s.planCount}개</b></span><span>기록 <b>${s.recordCount}개</b></span><span>실행함 <b>${s.doneCount}개</b></span><span>채택 <b>${s.adoptedYes}개</b><small> / 미채택 ${s.adoptedNo} · 미확인 ${s.adoptedUnknown}</small></span><span>계획 소요 시간 <b>${total(s.spentTimeMin, '분')}</b></span><span>클릭 <b>${total(s.clicks)}</b></span><span>예약 <b>${total(s.bookings)}</b></span><span>쿠폰 사용 <b>${total(s.coupons)}</b></span></div><div class="table-wrap"><table><thead><tr><th>계획</th><th>실제 선택·활동</th><th>확보한 결과</th></tr></thead><tbody>${rows || '<tr><td colspan="3">저장한 계획이 없습니다. 3개월 홍보 일정표에서 계획을 작성하세요.</td></tr>'}</tbody></table></div><p class="notice" style="margin-top:18px">미확보 값을 0이나 실행 실패로 해석하지 않습니다. 매출 증가·인과 효과를 추정하지 않습니다.</p><div class="dialog-actions">${button('닫기', 'close-dialog', 'primary')}</div>`);
  } catch (error) { notify(error.message); }
}

// ---------- 자료·기준 관리 (SCR-A-001, P01, P02) ----------
const CHECK_LABELS = ['자료 기간', '집계 기준', '지역 코드', '누락값', '제공 기준 일관성'];
async function dataView() {
  const [sources, criteria] = await Promise.all([api('/api/admin/data-sources'), api('/api/admin/criteria')]);
  const now = new Date(), lastYear = now.getFullYear() - 1;
  const form = `<section class="panel panel-padding source-manager"><div class="panel-heading"><div><h2>CSV 자료 등록</h2><p>CSV와 출처·기준 정보를 입력한 뒤 검증하고 등록하세요.</p></div></div><form id="csv-form" novalidate><div class="metadata-grid"><div><label class="field-label" for="csv-file">CSV 파일</label><input class="field" id="csv-file" name="file" type="file" accept=".csv,text/csv"><p class="hint">최대 1MB · UTF-8 CSV</p></div><div><label class="field-label" for="csv-source">자료 출처·버전</label><input class="field" id="csv-source" name="sourceVersion" maxlength="40" required placeholder="예: 제주 지역별 방문객 2025 v1"></div><div><label class="field-label" for="csv-from">기준기간 시작</label><input class="field" id="csv-from" name="startMonth" type="month" value="${lastYear}-01" required></div><div><label class="field-label" for="csv-to">기준기간 종료</label><input class="field" id="csv-to" name="endMonth" type="month" value="${lastYear}-12" required></div><div><label class="field-label" for="csv-unit">단위</label><select class="field" id="csv-unit" name="unit">${Object.entries(UNITS).map(([code, name]) => `<option value="${code}">${name}</option>`).join('')}</select></div><div><label class="field-label" for="csv-provider">제공자·집계 기준</label><input class="field" id="csv-provider" name="providerBasis" maxlength="26" required placeholder="CSV의 provider 열과 같은 값"></div></div><details class="csv-format"><summary>CSV 열과 예시 형식 보기</summary><code>region_code,month,value,unit,provider,aggregation</code><p class="hint">지역 코드: ${session.regions.map(r => `${escapeHTML(r.regionName)} ${escapeHTML(r.regionCode)}`).join(', ')} · 월: ${lastYear}-10 · 값: 0 이상의 정수 · 집계: 월별 지역 집계</p></details><div class="row wrap source-actions"><button type="button" class="button small" data-action="validate">자료 검증</button><button type="button" class="button primary small" data-action="csv-register" disabled>자료 등록</button></div><p class="notice" id="csv-status" role="status">자료를 선택하고 출처·기준을 확인한 뒤 검증해 주세요.</p><ol class="validation-grid" id="csv-validation">${CHECK_LABELS.map(l => `<li>${l} · 대기</li>`).join('')}</ol></form>`;
  const criteriaBar = `<div class="row between criteria-toolbar"><div><h3>추천 기준·근거 관리</h3><p class="small muted" id="criteria-summary">${criteria ? `방문 감소 기준 ${criteria.declineRateThreshold}% · ` : '추천 기준 미설정 · '}등록한 자료 ${sources.count}개</p></div>${button('기준·근거 수정', 'criteria', '', 'edit')}</div></section>`;
  const list = sources.count ? `<section class="panel"><div class="table-wrap"><table><thead><tr><th>파일</th><th>출처·버전</th><th>기간</th><th>단위</th><th>행 수</th><th>등록일</th></tr></thead><tbody>${sources.sources.map(s => `<tr><td>${escapeHTML(s.fileName)}</td><td>${escapeHTML(s.sourceVersion)}</td><td>${String(s.startMonth).slice(0, 7)}부터 ${String(s.endMonth).slice(0, 7)}까지</td><td>${escapeHTML(label(UNITS, s.unit))}</td><td>${s.rowCount}</td><td>${escapeHTML(s.registeredAt)}</td></tr>`).join('')}</tbody></table></div></section>` : '';
  return `${heading('자료와 기준을 관리하는 공간', '자료의 출처·기간·단위와 누락 여부를 확인하세요.', `<a class="button" href="/api/admin/data-sources/template" download>${icon('download')}CSV 양식 내려받기</a>`)}${form}${criteriaBar}${list}`;
}
function csvForm() {
  const form = $('#csv-form'), data = new FormData(form);
  return data;
}
function invalidateCSV() {
  csvChecked = false;
  $('[data-action="csv-register"]')?.setAttribute('disabled', '');
  if ($('#csv-status')) $('#csv-status').textContent = '입력이 바뀌었습니다. 다섯 검증을 다시 실행해 주세요.';
  if ($('#csv-validation')) $('#csv-validation').innerHTML = CHECK_LABELS.map(l => `<li>${l} · 재검증 필요</li>`).join('');
}
function showChecks(result) {
  $('#csv-validation').innerHTML = result.checks.map(c => `<li data-result="${c.passed ? 'pass' : 'fail'}"><b>${escapeHTML(c.label)} · ${c.passed ? '통과' : '확인 필요'}</b><span>${c.passed ? '기준과 일치' : escapeHTML(c.reason)}</span></li>`).join('');
  $('#csv-status').textContent = result.message;
}
async function validateCSV() {
  const status = $('#csv-status');
  try {
    const result = await api('/api/admin/data-sources/validate', {method: 'POST', form: csvForm()});
    showChecks(result);
    csvChecked = result.registrable;
    $('[data-action="csv-register"]').disabled = !result.registrable;
  } catch (error) { status.textContent = error.message; if (error.field === 'file') $('#csv-file').focus(); }
}
async function registerCSV() {
  if (!csvChecked) return;
  const register = $('[data-action="csv-register"]'); register.disabled = true;
  try {
    const result = await api('/api/admin/data-sources', {method: 'POST', form: csvForm()});
    await render(); notify(result.message);
  } catch (error) { $('#csv-status').textContent = `${error.message} 입력값을 유지했으니 다시 시도해 주세요.`; register.disabled = false; }
}
async function criteriaDialog() {
  try {
    const c = await api('/api/admin/criteria') || {declineRateThreshold: 20, sourceInfo: '', basePeriod: '', reasonTemplate: '', requiredCondition: ''};
    openDialog('추천 기준과 근거 수정', '검증한 자료와 판단 이유를 함께 기록', `<form id="criteria-form" novalidate><p class="notice">저장하면 홍보 제안을 바로 다시 계산합니다. 출처·판단 이유·적용 조건도 제안과 함께 표시됩니다.</p><label class="field-label" for="criteria-declineRateThreshold">직전 월 대비 방문 감소 기준 · %</label><input class="field" id="criteria-declineRateThreshold" name="declineRateThreshold" type="number" min="0" max="100" step="1" required value="${escapeHTML(c.declineRateThreshold)}"><label class="field-label" for="criteria-sourceInfo">분석 결과·출처·단위</label><input class="field" id="criteria-sourceInfo" name="sourceInfo" required maxlength="83" value="${escapeHTML(c.sourceInfo)}"><label class="field-label" for="criteria-basePeriod">기준 기간</label><input class="field" id="criteria-basePeriod" name="basePeriod" required maxlength="16" value="${escapeHTML(c.basePeriod)}" placeholder="예: 2023년부터 2025년까지"><label class="field-label" for="criteria-reasonTemplate">추천 판단 이유</label><textarea class="field" id="criteria-reasonTemplate" name="reasonTemplate" required maxlength="166">${escapeHTML(c.reasonTemplate)}</textarea><label class="field-label" for="criteria-requiredCondition">적용 조건·확인이 필요한 항목</label><textarea class="field" id="criteria-requiredCondition" name="requiredCondition" required maxlength="166">${escapeHTML(c.requiredCondition)}</textarea><p id="criteria-error" class="notice error" role="alert" hidden></p><div class="dialog-actions">${button('취소', 'close-dialog')}<button class="button primary" type="submit">저장</button></div></form>`);
  } catch (error) { notify(error.message); }
}
async function submitCriteria(form) {
  const data = new FormData(form);
  const threshold = data.get('declineRateThreshold');
  const body = {declineRateThreshold: threshold === '' ? null : Number(threshold), sourceInfo: data.get('sourceInfo'), basePeriod: data.get('basePeriod'), reasonTemplate: data.get('reasonTemplate'), requiredCondition: data.get('requiredCondition')};
  try {
    await api('/api/admin/criteria', {method: 'PUT', body});
    closeDialog(); await render(); notify('추천 기준과 근거를 저장하고 홍보 제안을 다시 계산했습니다.');
  } catch (error) {
    $('#criteria-error').hidden = false; $('#criteria-error').textContent = `${error.message} 기존 적용 기준은 유지됩니다.`;
    if (error.field) $(`#criteria-${error.field}`)?.focus();
  }
}

// ---------- 공통 팝업 ----------
function openDialog(title, subtitle, body, drawer = false) {
  if (!dialog.open) opener = document.activeElement;
  dialog.className = drawer ? 'drawer' : '';
  dialog.innerHTML = `<header class="dialog-header"><div><h2 id="dialog-title">${title}</h2>${subtitle ? `<p>${subtitle}</p>` : ''}</div><button class="icon-button" data-action="close-dialog" aria-label="닫기">${icon('close')}</button></header><div class="dialog-body">${body}</div>`;
  if (!dialog.open) dialog.showModal();
  else $('[data-action="close-dialog"]', dialog).focus();
  document.body.style.overflow = 'hidden';
}
function closeDialog() { if (dialog.open) dialog.close(); }
dialog.addEventListener('close', () => {
  document.body.style.overflow = '';
  let target = opener;
  if (target && !target.isConnected) {
    const attribute = target.getAttributeNames().find(name => name.startsWith('data-'));
    target = attribute ? $(`[${attribute}="${CSS.escape(target.getAttribute(attribute))}"]`, app) : null;
  }
  (target || $('#main'))?.focus({preventScroll: true});
});
function sourceDialog() {
  const s = session.lastSource;
  const body = s
    ? `<h3>${escapeHTML(s.sourceVersion)}</h3><ul class="relation-list"><li><b>파일</b>${escapeHTML(s.fileName)}</li><li><b>제공자·집계 기준</b>${escapeHTML(s.providerBasis)} · 월별 지역 집계</li><li><b>기간</b>${String(s.startMonth).slice(0, 7)}부터 ${String(s.endMonth).slice(0, 7)}까지</li><li><b>단위</b>${escapeHTML(s.unitLabel)} · 개별 가게의 고객 수와 구분</li><li><b>등록일</b>${escapeHTML(s.registeredAt)}</li></ul>`
    : '<p>아직 등록된 자료가 없습니다.</p>';
  openDialog('자료의 출처와 기준', '검증을 통과해 등록한 자료', `${body}<p class="notice">과거 수치를 미래 전망으로 사용하지 않습니다. 실제 적용 전에 원본 자료의 이용 조건을 확인해 주세요.</p><div class="dialog-actions">${button('확인', 'close-dialog', 'primary')}</div>`);
}
function helpDialog() {
  openDialog('사용 안내', '제주 비수기 상권 마케팅 캘린더', `<ul class="relation-list"><li><b>지역 분석 → 홍보 제안 조회</b>지역별 방문·소비를 비교하고 추천 이유와 자료 기준을 확인하세요.</li><li><b>3개월 홍보 일정표 관리</b>제안을 선택하거나 직접 계획을 작성해 저장하세요.</li><li><b>사용 기록 입력</b>실제 수행한 활동과 확보한 결과를 기록하세요. 미확보 값은 빈칸으로 남겨 주세요.</li><li><b>자료·기준 관리</b>서비스 운영자는 자료를 검증·등록하고 추천 기준을 관리합니다.</li></ul><div class="dialog-actions">${button('확인', 'close-dialog', 'primary')}</div>`);
}
function logoutDialog() {
  openDialog('로그아웃할까요?', '저장한 계획과 기록은 유지됩니다.', `<p>다른 계정을 사용하려면 로그아웃한 뒤 다시 로그인하세요.</p><div class="dialog-actions">${button('취소', 'close-dialog')}${button('로그아웃', 'confirm-logout', 'primary')}</div>`);
}
function deleteDialog(planId) {
  openDialog('이 계획을 삭제할까요?', '연결된 실행 기록도 함께 삭제됩니다.', `<p>삭제할 계획을 확인한 뒤 진행해 주세요.</p><p class="notice error" id="delete-error" role="alert" hidden></p><div class="dialog-actions">${button('취소', 'close-dialog')}<button class="button primary" data-confirm-delete="${planId}">삭제하기</button></div>`);
}
async function confirmDelete(planId) {
  try {
    await api(`/api/plans/${planId}`, {method: 'DELETE'});
    closeDialog(); await render(); notify('계획을 삭제했습니다.');
  } catch (error) { $('#delete-error').hidden = false; $('#delete-error').textContent = `삭제에 실패했습니다. 계획과 기록을 유지했으니 다시 시도해 주세요. ${error.message}`; }
}

// ---------- 이벤트 ----------
document.addEventListener('click', event => {
  const target = event.target.closest('button,a'); if (!target) return;
  if (target.matches('.skip')) { event.preventDefault(); $('#main')?.focus(); return; }
  if (target.matches('.sidebar a') && target.hash === location.hash) { event.preventDefault(); render(); return; }
  if (target.dataset.analysisMode) { session.analysisMode = target.dataset.analysisMode; render(); return; }
  if (target.dataset.regionMap) { session.region = target.dataset.regionMap; render(); return; }
  if (target.dataset.metric) { session.metric = target.dataset.metric; render(); return; }
  if (target.dataset.evidence) { evidenceDialog(Number(target.dataset.evidence)); return; }
  if (target.dataset.rec) { planDialog({suggestionId: Number(target.dataset.rec)}); return; }
  if (target.dataset.edit) { planDialog({planId: Number(target.dataset.edit)}); return; }
  if (target.dataset.record) { recordDialog(Number(target.dataset.record)); return; }
  if (target.dataset.month) { planDialog({month: Number(target.dataset.month)}); return; }
  if (target.dataset.role) { roleChoice = target.dataset.role; app.innerHTML = login(); $(`[data-role="${roleChoice}"]`).focus(); return; }
  if (target.dataset.delete) { deleteDialog(Number(target.dataset.delete)); return; }
  if (target.dataset.confirmDelete) { confirmDelete(Number(target.dataset.confirmDelete)); return; }
  switch (target.dataset.action) {
    case 'source': sourceDialog(); break;
    case 'new-plan': planDialog(); break;
    case 'close-dialog': closeDialog(); break;
    case 'help': helpDialog(); break;
    case 'report': reportDialog(); break;
    case 'reload': render(); break;
    case 'bulk-add': bulkAdd(); break;
    case 'toggle-password': { const field = $('#login-password'), visible = field.type === 'password'; field.type = visible ? 'text' : 'password'; target.setAttribute('aria-pressed', String(visible)); target.setAttribute('aria-label', visible ? '비밀번호 숨기기' : '비밀번호 보기'); break; }
    case 'logout': logoutDialog(); break;
    case 'confirm-logout': api('/api/auth/logout', {method: 'POST'}).catch(() => {}).finally(() => { session.user = null; session.planCount = null; session.period = null; navigate('login'); notify('로그아웃했습니다. 저장한 계획은 그대로 남아 있어요.'); }); break;
    case 'sidebar-toggle': sidebarLayout.collapsed = !sidebarLayout.collapsed; applySidebarLayout(); saveSidebarLayout(); break;
    case 'sidebar-reset': sidebarLayout.width = 248; sidebarLayout.collapsed = false; applySidebarLayout(); saveSidebarLayout(); notify('메뉴 너비를 초기화했습니다.'); break;
    case 'menu': $('.sidebar').classList.add('open'); $('.mobile-scrim').classList.add('open'); target.setAttribute('aria-expanded', 'true'); document.documentElement.classList.add('menu-open'); $('.workspace').inert = true; $('.sidebar .nav-link').focus(); break;
    case 'close-menu': closeMobileMenu(true); break;
    case 'validate': validateCSV(); break;
    case 'csv-register': registerCSV(); break;
    case 'criteria': criteriaDialog(); break;
  }
});
document.addEventListener('change', event => {
  if (event.target.closest('#csv-form')) invalidateCSV();
  if (event.target.id === 'analysis-year') { session.year = Number(event.target.value); render(); }
  if (event.target.id === 'region') { session.region = event.target.value; session.year = null; render(); notify(`${regionName(session.region)} 자료를 표시합니다.`); }
  if (event.target.id === 'business-select') { session.businessId = Number(event.target.value); session.region = business()?.regionCode || session.region; session.period = null; render(); }
  if (event.target.id === 'record-execStatus') { $('#record-executedDate').required = event.target.value === 'DONE'; }
});
document.addEventListener('input', event => {
  if (event.target.closest('#csv-form')) invalidateCSV();
  if (['login-email', 'login-password'].includes(event.target.id)) { $('#login-error').hidden = true; event.target.removeAttribute('aria-invalid'); }
});
document.addEventListener('submit', event => {
  const form = event.target;
  const handlers = {'login-form': submitLogin, 'plan-form': submitPlan, 'record-form': submitRecord, 'criteria-form': submitCriteria};
  if (!handlers[form.id]) return;
  event.preventDefault();
  handlers[form.id](form);
});
document.addEventListener('keydown', event => {
  if (event.target.id === 'sidebar-resizer' && ['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) {
    event.preventDefault();
    sidebarLayout.width = event.key === 'Home' ? 216 : event.key === 'End' ? 360 : Math.min(360, Math.max(216, sidebarLayout.width + (event.key === 'ArrowRight' ? 10 : -10)));
    applySidebarLayout(); saveSidebarLayout(); return;
  }
  if (!dialog.open && $('.sidebar.open')) {
    if (event.key === 'Escape') closeMobileMenu(true);
    if (event.key === 'Tab') {
      const items = [...$('.sidebar').querySelectorAll('a[href],button,select')].filter(el => el.getClientRects().length), first = items[0], last = items.at(-1);
      if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
      else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
    }
  }
});
let sidebarDrag = null;
document.addEventListener('pointerdown', event => {
  const handle = event.target.closest('#sidebar-resizer');
  if (!handle || event.button !== 0 || sidebarLayout.collapsed) return;
  sidebarDrag = {id: event.pointerId, x: event.clientX, width: sidebarLayout.width}; handle.setPointerCapture(event.pointerId); handle.focus(); document.documentElement.classList.add('sidebar-resizing'); event.preventDefault();
});
document.addEventListener('pointermove', event => {
  if (!sidebarDrag || event.pointerId !== sidebarDrag.id) return;
  sidebarLayout.width = Math.round(Math.min(360, Math.max(216, sidebarDrag.width + event.clientX - sidebarDrag.x))); applySidebarLayout();
});
function finishSidebarDrag(event) {
  if (!sidebarDrag || event.pointerId !== sidebarDrag.id) return;
  if (event.type === 'pointercancel') sidebarLayout.width = sidebarDrag.width;
  sidebarDrag = null; document.documentElement.classList.remove('sidebar-resizing'); applySidebarLayout();
  if (event.type === 'pointerup') saveSidebarLayout();
}
document.addEventListener('pointerup', finishSidebarDrag);
document.addEventListener('pointercancel', finishSidebarDrag);
window.matchMedia('(max-width:767px)').addEventListener('change', () => closeMobileMenu());
window.addEventListener('hashchange', () => { closeDialog(); render(); window.scrollTo(0, 0); });

loadSession().finally(render);
