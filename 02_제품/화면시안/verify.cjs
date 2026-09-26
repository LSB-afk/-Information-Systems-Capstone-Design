/* Uses an already available Playwright installation; adds no app dependency. */
const {chromium} = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {createHash} = require('node:crypto');
const base = process.env.PREVIEW_URL || 'http://127.0.0.1:8766/';
const output = process.env.PREVIEW_QA_DIR || '/private/tmp/jeju-screen-design';
const screenshots = path.join(output,'verified');
const checks=[];
const errors=[];
function pass(name){checks.push(name);console.log('PASS',name);}
(async()=>{
  fs.mkdirSync(screenshots,{recursive:true});
  const browser=await chromium.launch({headless:true,executablePath:process.env.CHROME_PATH || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'});
  try{
    for(const width of [1440,1024,390]){
      const context=await browser.newContext({viewport:{width,height:width===390?844:1080},deviceScaleFactor:1,colorScheme:'dark',reducedMotion:'reduce'});
      const page=await context.newPage();page.on('pageerror',e=>errors.push(e.message));
      for(const view of ['overview','analysis','recommendations','calendar','records','ontology','data','login']){
        await page.goto(base+'#'+view);await page.evaluate(()=>document.fonts.ready);
        const status=await page.evaluate(()=>({overflow:document.documentElement.scrollWidth>innerWidth,font:document.fonts.check('16px "Pretendard Variable"'),loaded:[...document.fonts].some(f=>f.family==='Pretendard Variable'&&f.status==='loaded'),scheme:getComputedStyle(document.documentElement).colorScheme,bg:getComputedStyle(document.body).backgroundColor}));
        assert.equal(status.overflow,false,`${view} at ${width}: horizontal overflow`);
        assert.equal(status.font,true);assert.equal(status.loaded,true);assert.equal(status.scheme,'light');assert.equal(status.bg,'rgb(247, 248, 245)');
        if(width===390 && view!=='login')assert.equal(await page.locator('.sidebar').isVisible(),false);
        await page.screenshot({path:path.join(screenshots,`${view}-${width}.png`),fullPage:true,animations:'disabled'});
      }
      pass(`8 views: ${width}px, no page overflow, real Pretendard loaded, light under OS dark mode`);
      if(width===390){
        await page.goto(base+'#overview');await page.getByRole('button',{name:'메뉴 열기'}).click();
        assert.equal(await page.locator('.sidebar').isVisible(),true);
        assert.equal(await page.locator('.workspace').evaluate(e=>e.inert),true);
        await page.keyboard.press('Escape');assert.equal(await page.locator('.sidebar').isVisible(),false);
        assert.equal(await page.locator('.workspace').evaluate(e=>e.inert),false);
        pass('Mobile menu: open, background inert, Escape close');
      }
      await context.close();
    }
    const context=await browser.newContext({viewport:{width:1440,height:1080},reducedMotion:'reduce'});
    const page=await context.newPage();page.on('pageerror',e=>errors.push(e.message));
    await page.goto(base+'#calendar');
    await page.getByRole('button',{name:'직접 계획 추가',exact:true}).click();
    await page.locator('#plan-title').fill('   ');await page.locator('#plan-form button[type=submit]').click();assert.equal(await page.locator('#plan-error').isVisible(),true);
    await page.locator('#plan-title').fill('<수동> 겨울 메뉴 준비');await page.locator('#plan-date').fill('2026-12-15');await page.locator('#plan-memo').fill('사진 촬영과 판매 조건 확인');await page.locator('#plan-form button[type=submit]').click();
    await page.reload();await page.getByRole('heading',{name:'<수동> 겨울 메뉴 준비',exact:true}).waitFor();
    let plans=await page.evaluate(()=>JSON.parse(localStorage.getItem('jeju-design-prototype-v1')).plans);assert.equal(plans.length,3);
    const manual=plans.find(p=>p.title==='<수동> 겨울 메뉴 준비');
    await page.locator(`[data-edit="${manual.id}"]`).click();await page.locator('#plan-title').fill('수정한 겨울 메뉴 준비');await page.locator('#plan-form button[type=submit]').click();
    await page.getByRole('heading',{name:'수정한 겨울 메뉴 준비',exact:true}).waitFor();pass('Direct plan: whitespace validation, escaped text, persistence and editing');
    await page.locator(`[data-record="${manual.id}"]`).click();await page.locator('#record-clicks').fill('0');await page.locator('#record-bookings').fill('');await page.locator('#record-note').fill('예시 기록');await page.locator('#record-form button[type=submit]').click();
    const record=await page.evaluate(id=>JSON.parse(localStorage.getItem('jeju-design-prototype-v1')).records[id],manual.id);assert.equal(record.clicks,0);assert.equal(record.bookings,null);pass('Records distinguish a measured zero from uncollected data');
    await page.goto(base+'#recommendations');await page.locator('[data-evidence="winter"]').click();await page.locator('#dialog').getByText('예시 추천 · 11월').waitFor();
    await page.keyboard.press('Escape');assert.equal(await page.locator('[data-evidence="winter"]').evaluate(e=>e===document.activeElement),true);pass('Evidence drawer: content, Escape and focus restoration');
    await page.locator('#app [data-rec="winter"]').click();await page.locator('#plan-form button[type=submit]').click();
    const count=await page.evaluate(()=>JSON.parse(localStorage.getItem('jeju-design-prototype-v1')).plans.length);
    await page.locator('#app [data-rec="winter"]').click();await page.getByRole('heading',{name:'계획 수정',exact:true}).waitFor();await page.locator('#plan-form button[type=submit]').click();
    assert.equal(await page.evaluate(()=>JSON.parse(localStorage.getItem('jeju-design-prototype-v1')).plans.length),count);pass('Adding an existing recommendation opens edit without duplication');
    await page.evaluate(()=>localStorage.removeItem('jeju-design-prototype-v1'));await page.goto(base+'#overview');await page.reload();
    await page.getByRole('button',{name:'AI로 계획 세우기',exact:true}).click();await page.locator('#agent-goal').selectOption('weekday');await page.locator('#agent-product').fill('귤차 세트');await page.locator('#agent-day').selectOption('1');await page.locator('#agent-form button[type=submit]').click();
    await page.getByText('귤차 세트 평일 안내',{exact:true}).waitFor();await page.getByText('선택한 휴무일과 겹친 일정을 다음 날로 옮겼어요.',{exact:false}).waitFor();
    await page.screenshot({path:path.join(screenshots,'agent-1440.png'),fullPage:true,animations:'disabled'});
    await page.locator('#agent-apply-form button[type=submit]').click();await page.waitForURL('**/#calendar');
    plans=await page.evaluate(()=>JSON.parse(localStorage.getItem('jeju-design-prototype-v1')).plans);assert.equal(plans.length,4);
    const generated=plans.filter(p=>p.id.startsWith('agent-'));assert.equal(generated.length,2);assert(generated.every(p=>new Date(p.date+'T12:00:00').getDay()!==1));
    pass('Agent demo: goal changes candidates, product reflected, holiday shifted, selected drafts persist');
    await page.goto(base+'#ontology');await page.locator('[data-node="source"]').click();await page.getByRole('heading',{name:'화면 시안 방문 자료 v1',exact:true}).waitFor();pass('Ontology relation selection changes the explanation');
    await page.goto(base+'#data');assert.equal(await page.locator('[data-action="validate"]').count(),0);await page.locator('[data-tab="spend"]').click();await page.locator('#data-search').fill('11월');assert.equal(await page.locator('#data-rows tr').count(),2);assert.equal(await page.locator('#data-rows').getByText('미수집',{exact:true}).count(),2);pass('Source table: filter works and missing consumption stays missing');
    const downloadWait=page.waitForEvent('download');await page.locator('[data-action="export"]').click();const download=await downloadWait;const downloaded=path.join(output,'export.csv');await download.saveAs(downloaded);const csv=fs.readFileSync(downloaded,'utf8');assert(csv.includes('화면 검토용 예시'));assert(csv.includes('"","억 원","미수집"'));pass('CSV download identifies fixtures and retains missing values');
    await page.locator('[data-action="roles"]').click();assert.equal(await page.getByText('일반 사용자',{exact:true}).count(),0);await page.locator('[data-role="service"]').click();await page.locator('[data-action="enter"]').click();await page.goto(base+'#data');await page.locator('[data-action="csv-sample"]').click();assert.equal(await page.locator('#csv-validation [data-result=pass]').count(),5);pass('Two role previews; all five source checks appear only for service operator');
    await context.close();
    const limited=await browser.newContext({viewport:{width:390,height:844},reducedMotion:'reduce'});const blocked=await limited.newPage();
    await blocked.addInitScript(()=>{Storage.prototype.setItem=function(){throw new DOMException('Storage blocked','SecurityError')};});
    await blocked.goto(base+'#calendar');await blocked.getByRole('button',{name:'직접 계획 추가',exact:true}).click();await blocked.locator('#plan-title').fill('저장 제한 시안');await blocked.locator('#plan-form button[type=submit]').click();await blocked.locator('#plan-error').waitFor({state:'visible'});assert((await blocked.locator('#plan-error').textContent()).includes('실패'));assert.equal(await blocked.locator('#plan-title').inputValue(),'저장 제한 시안');assert.equal(await blocked.locator('#dialog').evaluate(el=>el.open),true);pass('Blocked storage retains the editable draft and does not report a completed save');await limited.close();
    assert.deepEqual(errors,[]);pass('No JavaScript runtime errors during screen and interaction checks');
    const sourceHashes=Object.fromEntries(['index.html','styles.css','app.js','verify.cjs','verify-regressions.cjs','verify-layout-login.cjs','verify-usecase-gaps.cjs'].map(file=>[file,createHash('sha256').update(fs.readFileSync(path.join(__dirname,file))).digest('hex')]));
    fs.writeFileSync(path.join(output,'verification.json'),JSON.stringify({status:'passed',runAt:new Date().toISOString(),browserVersion:browser.version(),nodeVersion:process.version,sourceHashes,checks,errors,screenshots:'verified',limits:['Standalone prototype, not original Figma application','Fixture data and scripted AI demo','No server authentication, real CSV ingestion or real ontology inference','No screen-reader, real mobile device or full WCAG conformance audit']},null,2)+'\n');
  }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1});
