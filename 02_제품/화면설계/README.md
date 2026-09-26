# 화면설계 자료

정본 [PROJECT](../../00_제출/Red_PROJECT.md)·[Use Case](../../00_제출/Red_USECASE.md)·[StarUML](../../00_제출/Red_USECASE.mdj)의 3 Actor·32 UC를 8개 주요 화면에 연결함

## 결과물

- [PDF 검토본 — 32쪽](../../00_제출/Red_SCREEN.pdf)
- [화면구성도·목록·Storyboard 원고](../../00_제출/Red_SCREEN.md)
- [검증 결과와 남은 Figma 항목](../../06_증빙/화면설계/검증.md)
- [Figma Design — 이승보의 팀](https://www.figma.com/design/3g5xR8OkcV6sUclyGM5d30)

PDF에는 화면·상태 캡처 21개와 번호별 동작 설명 73개가 포함됨 Figma 플러그인 연결과 팀 Design 파일 생성을 완료했으며 전체 화면·캡처 이관을 진행 중임 공유 링크의 비로그인 열람은 CloudFront 403으로 미검증 상태임

## 자료 수정 위치

| 파일 | 역할 |
| --- | --- |
| `requirements.json` | 주요 화면 목록, 정본 32 UC 대응, Actor별 흐름, 제출 확인표 |
| `capture-storyboards.cjs` | 각 상태의 재현 동작·번호·사용자 행위→시스템 처리→결과 설명 정의 |
| `storyboards.json` | 위 두 자료를 합친 PDF·Markdown 공통 입력 캡처 실행으로 갱신 |
| `figma-status.json` | 실제 Figma 파일 URL, 이관·캡처 진행 상태, 공유 권한·글꼴·Prototype 검증 상태 |
| `sync-document.cjs` | Figma 상태와 캡처 명세를 확인해 공통 입력·Red_SCREEN.md의 출처·Storyboard·확인표 갱신 |
| `verify-design.cjs` | StarUML 정본·화면 ID·UC 명칭·설명·이미지·문서 일치 검사 |
| `build-pdf.cjs` | 로컬 글꼴과 캡처로 PDF 생성, 넘침·누락 이미지·번호 검사 |

Storyboard 설명을 바꿀 때에는 `capture-storyboards.cjs`를 먼저 수정하고 다시 생성함 `Red_SCREEN.md` 5장·7장이나 `storyboards.json`만 수정하면 다음 생성 때 덮어써질 수 있음 표지의 Figma 상태는 `figma-status.json`, 1장부터 4장의 나머지 내용·선택 기능 부록은 직접 관리

## 다시 생성하기

기존 Playwright·Chrome을 사용하며 앱 패키지는 추가하지 않음 [시안 서버](../화면시안/README.md)를 먼저 `127.0.0.1:8766`에서 실행한 후 저장소 최상위 폴더에서 실행

```sh
export PLAYWRIGHT_MODULE=/path/to/installed/playwright
export CHROME_PATH=/path/to/chrome
node 02_제품/화면설계/capture-storyboards.cjs
node 02_제품/화면설계/sync-document.cjs
node 02_제품/화면설계/verify-design.cjs
node 02_제품/화면설계/build-pdf.cjs --report 06_증빙/화면설계/pdf-build.json
```

명령을 순서대로 실행하고 오류가 나면 뒤 단계 진행 전에 수정함 PDF 생성 검사는 HTML 배치 확인이므로 최종 PDF를 별도로 열어 글꼴·잘림·번호를 확인해야 함

## Figma 자료 반영 방식

`figma-status.json`의 `captureStatus`가 `completed`일 때만 Figma 이미지로 교체함 [캡처 명세](../../06_증빙/화면설계/figma/manifest.json)의 파일 ID·21개 화면 키·Screen ID·프레임 ID·이미지 존재·73개 번호 좌표를 검사한 뒤 반영함 하나라도 맞지 않으면 문서 생성을 중단함

Figma 원본 캡처는 `06_증빙/화면설계/figma/screens/<key>.png`에 보관함 `callouts`의 `x`, `y`는 캡처 이미지 기준 번호 중심 좌표의 백분율임 PDF는 이 좌표에 번호를 표시하고 Markdown은 번호 없는 캡처와 원본 프레임 링크를 제공함

공유 URL만으로 열람 검증 완료로 처리하지 않음 비로그인 환경의 실제 프레임 열람과 Figma Prototype 동작을 각각 확인해야 함 Pretendard Variable 이름은 유지했으나 MCP의 글꼴 누락 표시로 클라우드 텍스트 편집은 미검증 상태임
