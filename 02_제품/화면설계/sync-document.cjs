/* Keep the review manuscript's descriptions identical to the PDF source. */
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const root = path.resolve(__dirname, '../..');
const documentPath = path.join(root, '00_제출/Red_SCREEN.md');
const data = JSON.parse(fs.readFileSync(path.join(__dirname, 'storyboards.json'), 'utf8'));
const requirementsPath = path.join(__dirname, 'requirements.json');
const requirements = JSON.parse(fs.readFileSync(requirementsPath, 'utf8'));
const figma = JSON.parse(fs.readFileSync(path.join(__dirname, 'figma-status.json'), 'utf8'));
const capturesComplete = figma.captureStatus === 'completed';
if (capturesComplete) {
  const manifest = JSON.parse(fs.readFileSync(path.join(root, figma.manifest), 'utf8'));
  assert.equal(manifest.fileKey, figma.fileKey, 'Figma file differs from capture manifest');
  assert.equal(manifest.boards.length, data.boards.length, 'Figma capture count differs from storyboard count');
  assert.equal(new Set(manifest.boards.map(board => board.key)).size, data.boards.length, 'Duplicate Figma capture key');
  for (const board of data.boards) {
    const frame = manifest.boards.find(frame => frame.key === board.key);
    assert(frame, `Missing Figma frame for ${board.key}`);
    assert.equal(frame.screenId, board.screenId, `${board.key} Screen ID mismatch`);
    assert.match(frame.nodeId, /^\d+:\d+$/, `${board.key} lacks a Figma node ID`);
    assert.equal(frame.image, `06_증빙/화면설계/figma/screens/${board.key}.png`);
    assert(fs.existsSync(path.join(root, frame.image)), `Missing Figma screenshot ${frame.image}`);
    assert(frame.width > 0 && frame.height > 0, `${board.key} lacks capture dimensions`);
    assert.deepEqual(frame.callouts.map(item => item.n), board.items.map(item => item.n), `${board.key} callout numbers differ`);
    board.items.forEach(item => {
      const marker = frame.callouts.find(marker => marker.n === item.n);
      assert([marker.x, marker.y].every(value => Number.isFinite(value) && value >= 0 && value <= 100), `${board.key} #${item.n} position must be a percentage`);
      item.x = marker.x;
      item.y = marker.y;
    });
    board.image = frame.image;
    board.annotated = false;
    board.figma = { nodeId: frame.nodeId, url: `${figma.url}?node-id=${frame.nodeId.replace(':', '-')}`, width: frame.width, height: frame.height };
    board.caption = 'Figma 프레임 캡처 · 인위적 예시 자료 · 번호는 PDF에서 캡처 위에 표시';
  }
}
data.meta.figma = { ...figma, status: capturesComplete ? '전체 화면·Figma 캡처 반영 · 공유 권한 확인 전 검토본' : '계정 연결·팀 Design 파일 생성 완료 · 전체 화면 이관·캡처 진행 중' };
data.meta.version = capturesComplete ? '화면설계 v0.2 Figma 반영 검토본' : '화면설계 v0.2 Figma 이관 검토본';
data.meta.captureSource = capturesComplete ? 'Figma' : '로컬 웹 시안';
function check(requirement, status, evidence) {
  const row = requirements.checklist.find(row => row.requirement === requirement);
  assert(row, `Unknown submission check ${requirement}`);
  Object.assign(row, { status, evidence });
}
check('Figma 활용 화면 설계', capturesComplete ? '전체 화면 이관 완료' : '계정 연결·파일 생성 완료 / 이관 중', capturesComplete ? '이승보의 팀 Design 파일에 주요 화면 8개와 팝업·상태를 포함한 21개 프레임 반영; 프레임 ID는 캡처 명세에 기록' : 'Figma 플러그인 인증과 이승보의 팀 Design 파일 생성 확인; 전체 21개 화면·상태를 이관 중');
check('Figma 캡처 삽입', capturesComplete ? '캡처 21개 반영' : '캡처 교체 대기', capturesComplete ? 'Figma 원본 프레임 캡처를 사용하고 PDF에서 설명 번호 73개를 좌표에 맞춰 표시; 원본 프레임 링크 포함' : 'Figma 캡처 완료 전까지 현재 로컬 웹 캡처의 출처 표시 유지');
check('팀 Figma 공유 링크 첨부', 'Design 링크 첨부', `${figma.team} 파일 URL을 화면설계서와 PDF 공통 입력에 기록; 링크 확보와 열람 권한 검증을 구분`);
check('제출 후 열람 가능한 공유 권한', figma.sharedAccessVerified ? '열람 검증 완료' : '미검증', figma.sharedAccessStatus);
check('Figma Prototype 화면 이동', figma.prototypeStatus === 'verified' ? '연결·동작 검증 완료' : figma.prototypeStatus === 'defined' ? '연결 정의 완료 / 재생 미검증' : '미완료·권장 항목', figma.prototypeEvidence);
check('PROJECT→UC→화면설계서→Figma 최종 일관성', capturesComplete ? '화면·UC 대응 반영 / 공유 권한 미검증' : '문서·웹 시안 완료 / Figma 이관 중', capturesComplete ? '정본 32 UC·8개 화면 ID와 21개 Figma 프레임을 대응; 공유 권한과 클라우드 글꼴 편집은 별도 확인 필요' : '정본 32 UC·명칭·필수/선택·화면 이동 대조 완료; Figma 이관 완료 후 프레임·캡처 대조 필요');
data.checklist = requirements.checklist;
fs.writeFileSync(requirementsPath, JSON.stringify(requirements, null, 2) + '\n');
fs.writeFileSync(path.join(__dirname, 'storyboards.json'), JSON.stringify(data, null, 2) + '\n');
const cell = text => String(text).replaceAll('|', '\\|').replaceAll('\n', '<br>');
const numbered = n => String.fromCodePoint(0x245f + n);
const boardText = data.boards.map((board, index) => {
  const rows = board.items.map(item => `| ${numbered(item.n)} ${cell(item.title)} | ${cell(item.action)} → ${cell(item.process)} → ${cell(item.result)} | ${cell(item.target)} |`).join('\n');
  return `### 5.${index + 1} ${board.screenId} / ${board.name}

**Actor:** ${board.actors.join('·')} · **관련 UC:** ${board.ucs.join('·') || '공통 접근 보조 · 별도 UC 없음'} · **경로:** ${board.path}

**범위:** ${board.scope}

![${board.screenId} ${board.name}](../${board.image})

${board.caption}
${board.figma ? `\n[Figma 원본 프레임](${board.figma.url}) · Node ID: ${board.figma.nodeId}\n` : ''}

| 번호·요소 | Description — 사용자 행위 → 시스템 처리 → 결과 | 이동·연결 |
| --- | --- | --- |
${rows}

**주요 상태·예외 처리**

${board.states.map(state => `- ${state}`).join('\n')}`;
}).join('\n\n');
const section = `## 5. 화면별 Storyboard

[PDF 검토본](Red_SCREEN.pdf)은 주요 화면 또는 주요 상태 하나를 한 페이지에 배치함 상단 Screen ID·화면명·Actor·관련 UC·경로, 왼쪽 번호가 붙은 화면 캡처, 오른쪽 같은 번호의 Description을 포함함

8개 주요 화면의 탭·팝업·예외를 21개 Storyboard로 설명함 상태가 달라도 부모 Screen ID를 유지하며 새 독립 화면으로 세지 않음 PDF 화면 위 원형 숫자는 아래 ①, ② 등의 설명 번호와 같음

아래 설명과 PDF는 [동일한 Storyboard 자료](../02_제품/화면설계/storyboards.json)에서 생성함 ${capturesComplete ? '현재 이미지는 **Figma 프레임 캡처**이며 번호는 PDF에서만 캡처 위에 덧붙임 원본 프레임은 각 화면의 링크에서 확인' : '현재 이미지는 **로컬 웹 시안 캡처**이며 Figma 캡처 완료 후 교체함'} 입력·전환·저장은 시안의 동작이고 실제 서버 인증·권한·자료 적재는 후속 구현 범위임

${boardText}

`;
let manuscript = fs.readFileSync(documentPath, 'utf8');
manuscript = manuscript.replace(/^\| 문서 상태 \|.*$/m, `| 문서 상태 | **${data.meta.figma.status} · 제출 준비 미완료** |`);
manuscript = manuscript.replace(/^\| 최종 제출 형식 \|.*$/m, `| 최종 제출 형식 | [Red_SCREEN.pdf 검토본](Red_SCREEN.pdf) 32쪽 + [팀 Figma Design 공유 링크](${figma.url}) |`);
manuscript = manuscript.replace(/^\| Figma·PDF \|.*$/m, `| Figma·PDF | ${capturesComplete ? '21개 Figma 프레임 캡처를 반영한 PDF 32쪽 검토본' : 'PDF 32쪽 검토본 제작·표시 확인 완료; 전체 Figma 화면·캡처 이관 중'} 공유 권한 검증 후 제출 상태 확정 |`);
const figmaStart = manuscript.indexOf('### Figma 공유와 캡처 상태');
const figmaEnd = manuscript.indexOf('## 1. 요구사항과 구현 범위');
assert(figmaStart >= 0 && figmaEnd > figmaStart, 'Figma status section boundaries are missing');
manuscript = manuscript.slice(0, figmaStart) + `### Figma 공유와 캡처 상태

| 항목 | 현재 상태 |
| --- | --- |
| 제출용 Figma Design 공유 링크 | [${figma.team} · 제주 마케팅 캘린더](${figma.url}) |
| Figma 계정 연결 | 플러그인 설치·계정 인증·팀 Design 파일 생성 확인 |
| Figma 원본 프레임 | ${capturesComplete ? '주요 화면 8개와 팝업·상태를 포함한 21개 프레임 반영; 각 Storyboard의 원본 프레임 링크 참조' : '전체 21개 화면·상태 이관 중'} |
| Figma 화면 캡처 | ${capturesComplete ? '[캡처 명세](../' + figma.manifest + ')의 Figma 캡처 21개 사용; 설명 번호 73개는 PDF에서 표시' : '전체 캡처 완료 전까지 로컬 웹 시안 캡처 사용'} |
| 공유 권한 | ${figma.sharedAccessStatus} |
| Prototype 이동 연결 | ${figma.prototypeEvidence} |
| 글꼴 편집 | ${figma.fontStatus} |

${capturesComplete ? 'Figma 파일과 화면 캡처를 반영함' : 'Figma 계정 연결과 팀 Design 파일 생성을 완료했으며 화면·캡처를 이관 중임'} 제출 후 열람 가능 여부는 비로그인 환경에서 주요 프레임이 실제로 열리는지 확인한 뒤 완료로 바꿈

` + manuscript.slice(figmaEnd);
const start = manuscript.indexOf('## 5.');
const end = manuscript.search(/## [67]\. 선택 기능과 확장 검토 부록/);
if (start < 0 || end <= start) throw new Error('Storyboard section boundaries are missing');
manuscript = manuscript.slice(0, start) + section + manuscript.slice(end);
manuscript = manuscript.replace(/## [67]\. 선택 기능과 확장 검토 부록/, '## 6. 선택 기능과 확장 검토 부록');
const checklistStart = manuscript.search(/## [78]\. 제출 전 일관성·검증 확인표/);
if (checklistStart < 0) throw new Error('Checklist section is missing');
manuscript = manuscript.slice(0, checklistStart) + `## 7. 제출 전 일관성·검증 확인표

| 확인 항목 | 현재 상태 | 근거·남은 확인 |
| --- | --- | --- |
${data.checklist.map(row => `| ${cell(row.requirement)} | ${cell(row.status)} | ${cell(row.evidence)} |`).join('\n')}

문서 구조·ID 검사와 실제 화면 표시 검사는 [화면설계 검증 기록](../06_증빙/화면설계/검증.md)에 구분함 Figma 필수 제출 항목을 실제로 확인한 뒤 표지의 ‘제출 준비 미완료’를 갱신함
`;
fs.writeFileSync(documentPath, manuscript);
console.log(`Synchronized ${data.boards.length} storyboards and ${data.checklist.length} submission checks`);
