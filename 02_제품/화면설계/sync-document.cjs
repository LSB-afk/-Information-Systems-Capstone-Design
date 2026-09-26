/* Keep the review manuscript's descriptions identical to the PDF source. */
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '../..');
const documentPath = path.join(root, '00_제출/Red_SCREEN.md');
const data = JSON.parse(fs.readFileSync(path.join(__dirname, 'storyboards.json'), 'utf8'));
const cell = text => String(text).replaceAll('|', '\\|').replaceAll('\n', '<br>');
const numbered = n => String.fromCodePoint(0x245f + n);
const boardText = data.boards.map((board, index) => {
  const rows = board.items.map(item => `| ${numbered(item.n)} ${cell(item.title)} | ${cell(item.action)} → ${cell(item.process)} → ${cell(item.result)} | ${cell(item.target)} |`).join('\n');
  return `### 5.${index + 1} ${board.screenId} / ${board.name}

**Actor:** ${board.actors.join('·')} · **관련 UC:** ${board.ucs.join('·') || '공통 접근 보조 · 별도 UC 없음'} · **경로:** ${board.path}

**범위:** ${board.scope}

![${board.screenId} ${board.name}](../${board.image})

${board.caption}

| 번호·요소 | Description — 사용자 행위 → 시스템 처리 → 결과 | 이동·연결 |
| --- | --- | --- |
${rows}

**주요 상태·예외 처리**

${board.states.map(state => `- ${state}`).join('\n')}`;
}).join('\n\n');
const section = `## 5. 화면별 Storyboard

[PDF 검토본](Red_SCREEN.pdf)은 주요 화면 또는 주요 상태 하나를 한 페이지에 배치함 상단 Screen ID·화면명·Actor·관련 UC·경로, 왼쪽 번호가 붙은 화면 캡처, 오른쪽 같은 번호의 Description을 포함함

8개 주요 화면의 탭·팝업·예외를 21개 Storyboard로 설명함 상태가 달라도 부모 Screen ID를 유지하며 새 독립 화면으로 세지 않음 화면 위 원형 숫자는 아래 ①, ② 등의 설명 번호와 같음

아래 설명과 PDF는 [동일한 Storyboard 자료](../02_제품/화면설계/storyboards.json)에서 생성함 현재 이미지는 **로컬 웹 시안 캡처**이며 Figma 캡처로 교체하기 전인 검토본임 입력·전환·저장은 시안의 동작이고 실제 서버 인증·권한·자료 적재는 후속 구현 범위임

${boardText}

`;
let manuscript = fs.readFileSync(documentPath, 'utf8');
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
