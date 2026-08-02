# a-stock-simulator

주식 시뮬레이터. 정훈님과 함께 작업하는 프로젝트.

## 상태

초기 셋업만 되어 있다. 기술 스택·기능 범위는 아직 정하지 않았다.

## 규칙

- **private repo** — `github.com/metionlabs/a-stock-simulator`
- ⛔ **비밀번호·API 키·토큰을 코드에 절대 쓰지 않는다.** 환경변수로 주입한다.
  (private 이라도 마찬가지다. 2026-07 에 실제로 DB 비번이 커밋돼 유출된 적 있다.
  git 히스토리는 되돌릴 수 없어서 비번 로테이션 말고는 방법이 없었다.)
- commit 은 `git@github.com:` SSH remote 로만. HTTPS + PAT URL 금지.
