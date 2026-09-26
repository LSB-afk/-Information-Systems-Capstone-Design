/* Check traceability without claiming that backend or Figma work is complete. */
const fs=require('node:fs');
const path=require('node:path');
const assert=require('node:assert/strict');
const {createHash}=require('node:crypto');
const root=path.resolve(__dirname,'../..');
const read=p=>JSON.parse(fs.readFileSync(path.join(root,p),'utf8'));
const req=read('02_제품/화면설계/requirements.json');
const doc=read('02_제품/화면설계/storyboards.json');
const model=read('00_제출/Red_USECASE.mdj');
const nodes=[];
function walk(x){if(!x||typeof x!=='object')return;if(x._type)nodes.push(x);Object.values(x).forEach(v=>Array.isArray(v)?v.forEach(walk):walk(v));}
walk(model);
const canonical=nodes.filter(n=>n._type==='UMLUseCase').map(n=>({id:n.name.match(/^UC\d+(?:-\d+)?/)[0],name:n.name.replace(/^UC\d+(?:-\d+)?/,'').replace(/^(?:\\n|\s)+/,'')}));
const actors=nodes.filter(n=>n._type==='UMLActor').map(n=>n.name).sort();
assert.deepEqual(actors,['사용자','사업체 운영자','서비스 운영자'].sort());
assert.equal(canonical.length,32);
assert.equal(req.inventory.length,8);
assert.equal(new Set(req.inventory.map(s=>s.id)).size,8);
assert.equal(new Set(req.coverage.map(c=>c.uc)).size,32);
assert.deepEqual(req.coverage.map(c=>c.uc).sort(),canonical.map(c=>c.id).sort());
const screenIDs=new Set(req.inventory.map(s=>s.id));
const boardIDs=new Set(doc.boards.map(b=>b.screenId));
for(const source of canonical){const row=req.coverage.find(c=>c.uc===source.id);assert.equal(row.name,source.name,`${source.id}: canonical name mismatch`);}
for(const row of req.coverage){
  const targets=Array.isArray(row.screen)?row.screen:[row.screen];
  assert(targets.some(t=>[...screenIDs].some(id=>String(t).includes(id))),`${row.uc} missing screen mapping`);
  if(row.uc.startsWith('UC06-'))assert.match(row.scope,/선택|검토/);
}
for(const screen of req.inventory){
  assert(boardIDs.has(screen.id),`${screen.id} lacks primary storyboard`);
  assert(screen.actors.length&&screen.path&&screen.name);
  for(const uc of screen.ucs)assert(canonical.some(c=>c.id===uc),`Unknown UC ${uc}`);
}
let callouts=0;
for(const board of doc.boards){
  assert(screenIDs.has(board.screenId.replace(/-P\d+$/,'')),`Unknown parent screen ${board.screenId}`);
  assert(fs.existsSync(path.join(root,board.image)),`Missing screenshot ${board.image}`);
  assert(board.actors.length&&board.path&&board.name&&board.states.length);
  assert.deepEqual(board.items.map(i=>i.n),Array.from({length:board.items.length},(_,i)=>i+1));
  for(const i of board.items){for(const field of ['title','action','process','result','target'])assert(i[field]?.trim(),`${board.key} item ${i.n} lacks ${field}`);}
  callouts+=board.items.length;
}
assert.deepEqual(doc.coverage,req.coverage);
assert.deepEqual(doc.inventory,req.inventory);
const figmaPending=!doc.meta.figma?.url || doc.meta.captureSource!=='Figma';
if(figmaPending){assert.match(doc.meta.version,/검토/);assert(doc.boards.every(b=>/로컬 웹/.test(b.caption)));}
const paths=['00_제출/Red_PROJECT.md','00_제출/Red_USECASE.md','00_제출/Red_USECASE.mdj','02_제품/화면설계/requirements.json','02_제품/화면설계/storyboards.json'];
const hashes=Object.fromEntries(paths.map(p=>[p,createHash('sha256').update(fs.readFileSync(path.join(root,p))).digest('hex')]));
const result={checkedAt:new Date().toISOString(),traceability:'passed',actors:actors.length,useCases:canonical.length,mainScreens:screenIDs.size,storyboardPages:doc.boards.length,callouts,requiredAndSupportUseCases:30,optionalUnderReview:2,figmaPending,submissionReady:!figmaPending,limits:figmaPending?['Figma connector account connection pending','Screenshots are local web captures','Figma file generation, prototype links, shared URL and anonymous access check not completed']:[],hashes};
fs.writeFileSync(path.join(root,'06_증빙/화면설계/coverage-result.json'),JSON.stringify(result,null,2)+'\n');
console.log(JSON.stringify(result,null,2));
