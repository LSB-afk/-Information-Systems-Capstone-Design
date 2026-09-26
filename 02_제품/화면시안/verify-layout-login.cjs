/* Exercise menu sizing and the public demo login through browser controls. */
const {chromium} = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const {test, before, after} = require('node:test');
const assert = require('node:assert/strict');
const base = process.env.PREVIEW_URL || 'http://127.0.0.1:8766/';
const layoutKey = 'jeju-design-layout-v1';
const sessionKey = 'jeju-design-demo-session-v1';
const plansKey = 'jeju-design-prototype-v1';
let browser;

before(async () => {
  browser = await chromium.launch({headless:true, executablePath:process.env.CHROME_PATH || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'});
});
after(async () => { await browser?.close(); });
async function withPage(run, viewport = {width:1440, height:1080}) {
  const context = await browser.newContext({viewport, reducedMotion:'reduce'});
  const page = await context.newPage();
  page.setDefaultTimeout(3000);
  page.setDefaultNavigationTimeout(5000);
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  try { await run(page); assert.deepEqual(errors, [], 'No JavaScript runtime errors'); }
  finally { await context.close(); }
}
async function widthIs(page, expected) {
  await page.waitForFunction(width => Math.abs(document.querySelector('#sidebar').getBoundingClientRect().width - width) < 1, expected, {timeout:3000});
}
async function noOverflow(page) {
  assert.equal(await page.evaluate(() => document.documentElement.scrollWidth > innerWidth), false, 'Page should fit the viewport');
}
async function dragTo(page, x) {
  const bounds = await page.locator('#sidebar-resizer').boundingBox();
  assert(bounds, 'The sidebar separator should be visible');
  await page.mouse.move(bounds.x + bounds.width / 2, bounds.y + Math.min(bounds.height / 2, 240));
  await page.mouse.down();
  await page.mouse.move(x, bounds.y + Math.min(bounds.height / 2, 240), {steps:8});
  await page.mouse.up();
}
async function demoCredentials(page, role = 'business') {
  await page.locator(`[data-role="${role}"]`).click();
  await page.locator('[data-action="fill-demo"]').click();
  const email = await page.locator('#login-email').inputValue();
  const password = await page.locator('#login-password').inputValue();
  assert(email.includes('@'), 'The public demo email should be filled');
  assert(password.length > 0, 'The public demo password should be filled');
  return {email,password};
}
async function signIn(page, role = 'business') {
  const credentials = await demoCredentials(page, role);
  await page.locator('#login-form button[type="submit"]').click();
  await page.waitForURL('**/#overview', {timeout:3000});
  return credentials;
}

test('Desktop menu starts at 248px with an accessible width separator at 1440px and 1024px', async () => {
  for (const width of [1440,1024]) await withPage(async page => {
    await page.goto(base + '#overview');
    const separator = page.getByRole('separator', {name:'메뉴 너비 조절'});
    await separator.waitFor({state:'visible'});
    assert.equal(await separator.getAttribute('id'), 'sidebar-resizer');
    assert.equal(await separator.getAttribute('aria-valuemin'), '216');
    assert.equal(await separator.getAttribute('aria-valuemax'), '360');
    assert.equal(await separator.getAttribute('aria-valuenow'), '248');
    await widthIs(page, 248);
    await noOverflow(page);
  }, {width,height:1080});
});

test('Dragging the menu clamps to 216–360px and saves its width across refresh', () => withPage(async page => {
  await page.goto(base + '#overview');
  await page.locator('#sidebar-resizer').waitFor({state:'visible'});
  await dragTo(page, 100);
  await widthIs(page, 216);
  await dragTo(page, 520);
  await widthIs(page, 360);
  await dragTo(page, 304);
  const width = await page.locator('#sidebar').evaluate(el => el.getBoundingClientRect().width);
  assert(width >= 296 && width <= 312, `Drag should follow the pointer, got ${width}px`);
  const saved = await page.evaluate(key => JSON.parse(localStorage.getItem(key)), layoutKey);
  assert.equal(saved.collapsed, false);
  assert(Math.abs(saved.width - width) < 1);
  await page.reload();
  await widthIs(page, saved.width);
  await noOverflow(page);
}));

test('Keyboard resizing, collapse, restore and reset retain the correct saved width', () => withPage(async page => {
  await page.goto(base + '#overview');
  await page.locator('#sidebar-resizer').focus();
  await page.keyboard.press('ArrowRight');
  await widthIs(page, 258);
  await page.keyboard.press('ArrowLeft');
  await widthIs(page, 248);
  await page.keyboard.press('End');
  await widthIs(page, 360);
  await page.keyboard.press('Home');
  await widthIs(page, 216);
  await page.keyboard.press('ArrowRight');
  await widthIs(page, 226);
  assert.equal(await page.locator('#sidebar-resizer').evaluate(el => el === document.activeElement), true);
  await page.locator('[data-action="sidebar-toggle"]').click();
  await widthIs(page, 76);
  assert.deepEqual(await page.evaluate(key => JSON.parse(localStorage.getItem(key)), layoutKey), {width:226,collapsed:true});
  await page.reload();
  await widthIs(page, 76);
  const calendar = page.locator('#sidebar a[href="#calendar"]');
  assert((await calendar.getAttribute('aria-label')) || (await calendar.innerText()).trim(), 'Collapsed links need accessible names');
  await calendar.click();
  await page.waitForURL('**/#calendar', {timeout:3000});
  await page.locator('[data-action="sidebar-toggle"]').click();
  await widthIs(page, 226);
  await page.locator('[data-action="sidebar-reset"]').click();
  await widthIs(page, 248);
  assert.deepEqual(await page.evaluate(key => JSON.parse(localStorage.getItem(key)), layoutKey), {width:248,collapsed:false});
  await noOverflow(page);
}));

test('A mobile drawer stays expanded despite desktop preferences and its bottom is reachable at short height', () => withPage(async page => {
  await page.addInitScript(key => localStorage.setItem(key, JSON.stringify({width:340,collapsed:true})), layoutKey);
  await page.goto(base + '#overview');
  assert.equal(await page.locator('#sidebar').isVisible(), false);
  await page.getByRole('button', {name:'메뉴 열기'}).click();
  const sidebar = page.locator('#sidebar');
  assert.equal(await sidebar.isVisible(), true);
  const bounds = await sidebar.boundingBox();
  assert(bounds.width >= 216 && bounds.width <= 390, 'Mobile drawer must have room for full menu labels');
  assert.equal(await page.locator('#sidebar-resizer').isVisible(), false);
  assert.equal(await page.locator('.workspace').evaluate(el => el.inert), true);
  const bottom = page.locator('#sidebar .profile');
  await bottom.scrollIntoViewIfNeeded();
  const bottomBounds = await bottom.boundingBox();
  assert(bottomBounds.y >= 0 && bottomBounds.y + bottomBounds.height <= 421, 'Bottom profile control must scroll into the mobile viewport');
  assert.equal(await sidebar.evaluate(el => el.scrollTop > 0), true, 'Short mobile menus must scroll');
  await noOverflow(page);
  await page.keyboard.press('Escape');
  assert.equal(await sidebar.isVisible(), false);
  assert.equal(await page.locator('.workspace').evaluate(el => el.inert), false);
  assert.deepEqual(await page.evaluate(key => JSON.parse(localStorage.getItem(key)), layoutKey), {width:340,collapsed:true});
}, {width:390,height:420}));

test('A fresh visit opens the login form without horizontal overflow at desktop, tablet and mobile widths', async () => {
  for (const width of [1440,1024,390]) await withPage(async page => {
    await page.goto(base);
    await page.locator('#login-form').waitFor({state:'visible'});
    assert.equal(await page.locator('#login-email').getAttribute('type'), 'email');
    assert.equal(await page.locator('#login-password').getAttribute('type'), 'password');
    assert.equal(await page.locator('#login-form button[type="submit"]').count(), 1);
    await noOverflow(page);
  }, {width,height:width === 390 ? 844 : 1080});
});

test('Password visibility changes without submitting the form or losing its values', () => withPage(async page => {
  await page.goto(base + '#login');
  const credentials = await demoCredentials(page);
  const toggle = page.locator('[data-action="toggle-password"]');
  await toggle.click();
  assert.equal(await page.locator('#login-password').getAttribute('type'), 'text');
  assert.equal(await page.locator('#login-password').inputValue(), credentials.password);
  assert.equal(await page.locator('#login-email').inputValue(), credentials.email);
  assert.equal(new URL(page.url()).hash, '#login');
  await toggle.click();
  assert.equal(await page.locator('#login-password').getAttribute('type'), 'password');
  assert.equal(new URL(page.url()).hash, '#login');
}));

test('Unknown credentials and credentials for the other role are rejected visibly without navigation', () => withPage(async page => {
  await page.goto(base + '#login');
  await page.locator('#login-email').fill('unregistered@example.invalid');
  await page.locator('#login-password').fill('not-a-demo-password');
  await page.locator('#login-form button[type="submit"]').click();
  const error = page.locator('#login-error');
  await error.waitFor({state:'visible'});
  assert.equal(await error.getAttribute('role'), 'alert');
  assert((await error.textContent()).trim());
  assert.equal(new URL(page.url()).hash, '#login');
  const business = await demoCredentials(page, 'business');
  await page.locator('[data-role="service"]').click();
  await page.locator('#login-email').fill(business.email);
  await page.locator('#login-password').fill(business.password);
  await page.locator('#login-form button[type="submit"]').click();
  await page.locator('#login-error').waitFor({state:'visible'});
  assert.equal(new URL(page.url()).hash, '#login');
  assert.equal(await page.evaluate(key => localStorage.getItem(key) || sessionStorage.getItem(key), sessionKey), null);
}));

test('Each public demo account opens its role and stores only the role in its session', async () => {
  for (const role of ['business','service']) await withPage(async page => {
    await page.goto(base + '#login');
    const credentials = await signIn(page, role);
    assert((await page.locator('.profile').textContent()).includes(role === 'service' ? '서비스 운영자' : '사업체 운영자'));
    const stored = await page.evaluate(key => ({
      session:localStorage.getItem(key) || sessionStorage.getItem(key),
      all:[...Object.entries(localStorage),...Object.entries(sessionStorage)]
    }), sessionKey);
    assert.deepEqual(JSON.parse(stored.session), {role});
    for (const [key,value] of stored.all) {
      assert(!value.includes(credentials.email), `${key} must not store the demo email`);
      assert(!value.includes(credentials.password), `${key} must not store the demo password`);
    }
    await page.goto(base);
    await page.waitForURL('**/#overview', {timeout:3000});
    assert((await page.locator('.profile').textContent()).includes(role === 'service' ? '서비스 운영자' : '사업체 운영자'));
  });
});

test('Logout clears the demo session while keeping saved plans and menu preferences', () => withPage(async page => {
  await page.goto(base + '#login');
  await signIn(page);
  await page.locator('#sidebar-resizer').focus();
  await page.keyboard.press('End');
  await page.goto(base + '#calendar');
  await page.locator('[data-action="new-plan"]').click();
  await page.locator('#plan-title').fill('로그아웃 뒤에도 유지할 계획');
  await page.locator('#plan-form button[type="submit"]').click();
  await page.getByRole('heading', {name:'로그아웃 뒤에도 유지할 계획', exact:true}).waitFor();
  const saved = await page.evaluate(([plans,layout]) => ({plans:JSON.parse(localStorage.getItem(plans)).plans,layout:localStorage.getItem(layout)}), [plansKey,layoutKey]);
  await page.locator('[data-action="logout"]').click();
  await page.waitForURL('**/#login', {timeout:3000});
  await page.locator('#login-form').waitFor({state:'visible'});
  const after = await page.evaluate(([plans,layout,session]) => ({
    plans:JSON.parse(localStorage.getItem(plans)).plans,
    layout:localStorage.getItem(layout),
    session:localStorage.getItem(session) || sessionStorage.getItem(session)
  }), [plansKey,layoutKey,sessionKey]);
  assert.deepEqual(after.plans, saved.plans);
  assert.equal(after.layout, saved.layout);
  assert.equal(after.session, null);
  await page.goto(base);
  await page.locator('#login-form').waitFor({state:'visible'});
}));

test('Explicit preview links and the preview button remain available without a demo session', () => withPage(async page => {
  for (const view of ['overview','calendar']) {
    await page.goto(base + '#' + view);
    assert.equal(new URL(page.url()).hash, '#' + view);
    assert.equal(await page.locator('#sidebar').isVisible(), true);
    assert.equal(await page.locator('h1').count(), 1);
  }
  await page.goto(base + '#login');
  await page.locator('[data-role="service"]').click();
  await page.locator('[data-action="enter"]').click();
  await page.waitForURL('**/#overview', {timeout:3000});
  assert((await page.locator('.profile').textContent()).includes('서비스 운영자'));
}));
