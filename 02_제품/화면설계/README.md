# 화면설계 자료

정본 [PROJECT](../../00_제출/Red_PROJECT.md)·[Use Case](../../00_제출/Red_USECASE.md)·[StarUML](../../00_제출/Red_USECASE.mdj)의 3 Actor·32 UC를 8개 주요 화면에 연결함

## 결과물

- [PDF 검토본 — 32쪽](../../00_제출/Red_SCREEN.pdf)
- [화면구성도·목록·Storyboard 원고](../../00_제출/Red_SCREEN.md)
- [검증 결과와 남은 Figma 항목](../../06_증빙/화면설계/검증.md)

PDF에는 화면·상태 캡처 21개와 번호별 동작 설명 73개가 포함됨 현재 이미지는 로컬 웹 시안 캡처임 Figma 연결·프레임 생성·캡처 교체·팀 공유 링크·비로그인 열람 확인 전에는 제출 완료로 처리하지 않음

## 자료 수정 위치

| 파일 | 역할 |
| --- | --- |
| `requirements.json` | 주요 화면 목록, 정본 32 UC 대응, Actor별 흐름, 제출 확인표 |
| `capture-storyboards.cjs` | 각 상태의 재현 동작·번호·사용자 행위→시스템 처리→결과 설명 정의 |
| `storyboards.json` | 위 두 자료를 합친 PDF·Markdown 공통 입력 캡처 실행으로 갱신 |
| `sync-document.cjs` | 공통 입력에서 Red_SCREEN.md의 Storyboard·확인표 갱신 |
| `verify-design.cjs` | StarUML 정본·화면 ID·UC 명칭·설명·이미지·문서 일치 검사 |
| `build-pdf.cjs` | 로컬 글꼴과 캡처로 PDF 생성, 넘침·누락 이미지·번호 검사 |

Storyboard 설명을 바꿀 때에는 `capture-storyboards.cjs`를 먼저 수정하고 다시 생성함 `Red_SCREEN.md` 5장·7장이나 `storyboards.json`만 수정하면 다음 생성 때 덮어써질 수 있음 1장부터 4장·선택 기능 부록은 직접 관리

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

## Figma 연결 후 남은 작업

1. 연결된 계정에서 팀 Design 파일을 만들거나 지정된 파일에 작성
2. 주요 Screen ID 8개를 프레임명에 반영하고 6개 팝업·패널 및 필요한 상태 표현
3. 메뉴·버튼의 이동을 문서의 Screen ID에 연결하고 Prototype 동작 확인
4. Figma 프레임에서 캡처한 이미지로 교체하고 캡처 출처 기록 갱신
5. 문서·PDF에 같은 팀 공유 URL을 넣고 비로그인 환경에서 실제 프레임 열람 확인

현재 `meta.figma.url`은 null임 기존 Figma Make 참고 링크를 이번 제출용 링크로 바꾸어 쓰지 않음 공유 URL 입력만으로 Figma 작성·캡처·권한 확인을 완료로 간주하지 않음
