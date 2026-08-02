# a-stock-simulator

주식 시뮬레이터. 정훈님과 함께 작업하는 프로젝트.

## 기술 스택

| | 버전 | 비고 |
|---|---|---|
| Java | **25 (LTS)** | 26 도 나와 있지만 LTS 가 아니다. LTS 는 17 → 21 → 25 |
| Spring Boot | **4.1.0** | |
| Gradle | 9.5.1 (Kotlin DSL) | wrapper 포함 — 따로 설치 안 해도 된다 |
| DB | H2 (인메모리) | 설치 없이 바로 뜨게 하려고. 스키마 자리 잡으면 MySQL 로 옮길 것 |

의존성: `webmvc`, `data-jpa`, `validation`, `actuator`

## 실행

```bash
./gradlew bootRun
```

확인:

- 앱: http://localhost:8080
- 헬스체크: http://localhost:8080/actuator/health
- H2 콘솔: http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:mem:stockdb`, 사용자 `sa`, 비번 없음)

빌드 / 테스트:

```bash
./gradlew build     # 컴파일 + 테스트 + jar
./gradlew test      # 테스트만
```

> 로컬에 JDK 25 가 없어도 된다. gradle toolchain 이 알아서 받아온다.
> 다만 처음 한 번은 다운로드 때문에 오래 걸린다.

## 현재 상태

뼈대만 있다. 기동 + DB 연결 + 컨텍스트 로딩 테스트까지 확인된 상태고,
도메인(종목·주문·보유·체결 등)은 아직 하나도 없다.

검증 기록 (2026-08-02, macbook16):

- `./gradlew build` → BUILD SUCCESSFUL
- 테스트 1/1 통과
- 실행 후 `/actuator/health` → `db: UP` (H2), 기동 2.4초

## 짚어둘 함정

Boot 4 로 오면서 이름이 바뀐 것들이 있다. 옛날 블로그를 보고 따라 하면 막힌다.

- `spring-boot-starter-web` → **`spring-boot-starter-webmvc`**
- 테스트 스타터가 기능별로 쪼개졌다 (`...-webmvc-test`, `...-data-jpa-test`).
  Boot 3 의 `spring-boot-starter-test` 하나로 다 되던 시절과 다르다
- `validation` 스타터를 빠뜨리면 `@Valid` 가 **조용히 무시된다.** 에러도 안 난다
- `tasks.withType<Test> { useJUnitPlatform() }` 이 없으면
  테스트가 **하나도 안 돌면서 초록불**이 뜬다

## 규칙

- **private repo** — `github.com/metionlabs/a-stock-simulator`
- ⛔ **비밀번호·API 키·토큰을 코드에 절대 쓰지 않는다.** 환경변수로 주입한다.
  private 이라도 마찬가지다. 2026-07 에 실제로 DB 비번이 커밋돼 유출된 적이 있고,
  git 히스토리는 되돌릴 수 없어서 비번 로테이션 말고는 방법이 없었다.
- commit 은 `git@github.com:` SSH remote 로만. HTTPS + PAT URL 금지
