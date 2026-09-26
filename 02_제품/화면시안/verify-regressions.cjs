/* Reproduce keyboard and saved-state failures through the real browser UI. */
const {chromium} = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const {test, before, after} = require('node:test');
const assert = require('node:assert/strict');
const base = process.env.PREVIEW_URL || 'http://127.0.0.1:8766/';
let browser;
before(async () => {
  browser = await chromium.launch({headless:true, executablePath:process.env.CHROME_PATH || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'});
});
after(async () => { await browser?.close(); });
async function withPage(run, viewport = {width:1440, height:1080}) {
  const context = await browser.newContext({viewport, reducedMotion:'reduce'});
  try { await run(await context.newPage()); }
  finally { await context.close(); }
}
async function focused(page, selector) {
  assert.equal(await page.locator(selector).evaluate(el => el === document.activeElement), true, `${selector} should keep keyboard focus`);
}

test('Skip to content keeps the current view and focuses its main element', () => withPage(async page => {
  for (const view of ['calendar','analysis','records','login']) {
    await page.goto(base + '#' + view);
    const title = await page.locator('h1').textContent();
    await page.locator('.skip').focus();
    await page.keyboard.press('Enter');
    assert.equal(new URL(page.url()).hash, '#' + view);
    assert.equal(await page.locator('h1').textContent(), title);
    await focused(page, '#main');
  }
}));

test('Metric, source, role and region choices retain keyboard focus', () => withPage(async page => {
  for (const [view, selector] of [
    ['analysis','[data-metric="spend"]'],
    ['data','[data-tab="spend"]'],
    ['login','[data-role="service"]']
  ]) {
    await page.goto(base + '#' + view);
    await page.locator(selector).focus();
    await page.keyboard.press('Enter');
    await focused(page, selector);
    assert.equal(await page.locator(selector).getAttribute('aria-pressed'), 'true');
  }
  await page.goto(base + '#analysis');
  await page.locator('#region').focus();
  await page.locator('#region').selectOption('구좌읍');
  await focused(page, '#region');
  assert((await page.locator('h1').textContent()).includes('지역'));
  assert((await page.locator('.eyebrow').textContent()).includes('구좌읍'));
}));

test('Selecting the active mobile menu closes it and releases the page', () => withPage(async page => {
  await page.goto(base + '#calendar');
  await page.getByRole('button', {name:'메뉴 열기'}).click();
  await page.locator('.sidebar a[href="#calendar"]').focus();
  await page.keyboard.press('Enter');
  assert.equal(await page.locator('.sidebar').isVisible(), false);
  assert.equal(await page.locator('.workspace').evaluate(el => el.inert), false);
  await focused(page, '#main');
}, {width:390, height:844}));

test('Saving a plan restores focus to its replacement launch button', () => withPage(async page => {
  await page.goto(base + '#calendar');
  await page.locator('[data-action="new-plan"]').click();
  await page.locator('#plan-title').fill('키보드 복귀 확인');
  await page.locator('#plan-form button[type="submit"]').click();
  await page.waitForFunction(() => document.activeElement?.matches('[data-action="new-plan"]'), null, {timeout:2000});
  await focused(page, '[data-action="new-plan"]');
  await page.getByRole('heading', {name:'키보드 복귀 확인', exact:true}).waitFor();
}));

test('Replacing an open dialog puts focus inside its new controls', () => withPage(async page => {
  await page.goto(base + '#overview');
  await page.getByRole('button', {name:'AI로 계획 세우기', exact:true}).click();
  await page.locator('#agent-form button[type="submit"]').click();
  assert.equal(await page.locator('#dialog').evaluate(el => el.contains(document.activeElement)), true);
  await page.keyboard.press('Escape');
  await focused(page, '.heading-action [data-action="agent"]');
}));

test('A null saved value does not leave the prototype blank', () => withPage(async page => {
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.addInitScript(() => localStorage.setItem('jeju-design-prototype-v1', 'null'));
  await page.goto(base + '#calendar');
  assert.equal(await page.locator('h1').count(), 1);
  assert.deepEqual(errors, []);
}));

test('Recovery keeps valid plans and records while excluding malformed entries', () => withPage(async page => {
  await page.addInitScript(() => {
    const valid = {id:'kept', title:'보존할 계획', date:'2026-10-05', channel:'매장 안내', memo:'정상 메모', region:'애월읍', recId:null};
    const record = {status:'실행함', date:'2026-10-05', clicks:0, bookings:null, note:'정상 기록'};
    localStorage.setItem('jeju-design-prototype-v1', JSON.stringify({
      plans:[valid, {...valid,id:'bad-array',date:['2026-10-05']}, {...valid,id:'bad-day',date:'2026-11-31'}, {...valid,id:'bad-record',title:'기록만 제외할 계획'}],
      records:{kept:record,'bad-record':true}
    }));
  });
  await page.goto(base + '#records');
  assert.equal(await page.locator('.record-row').count(), 2);
  await page.getByRole('heading', {name:'보존할 계획', exact:true}).waitFor();
  await page.getByRole('heading', {name:'기록만 제외할 계획', exact:true}).waitFor();
  assert((await page.locator('.record-row').first().textContent()).includes('클릭 0건'));
  assert((await page.locator('.record-row').nth(1).textContent()).includes('미기록'));
  assert((await page.locator('#main').textContent()).includes('복원하지 못한 항목'));
}));
