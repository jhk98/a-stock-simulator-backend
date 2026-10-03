# a-stock-simulator-backend

모의 주식 시뮬레이터

## 기술 스택

| 구분 | 버전 | 비고 |
|---|---|---|
| Java | **25 (LTS)** | |
| Spring Boot | **4.1.0** | |
| Gradle | **9.5.1 (Kotlin DSL)** | Gradle Wrapper 사용 — 별도 설치 불필요 |
| DB | **MySQL 8** | Ubuntu Server에 구축 |
| ORM | **Spring Data JPA / Hibernate** | DB 접근 및 Entity 매핑 |
| MySQL Driver | **MySQL Connector/J 9.7.0** | Spring Boot와 MySQL 연결 |

### 주요 의존성

- **Spring Web MVC**: REST API 및 HTTP 요청/응답 처리
- **Spring Data JPA**: Repository 기반 데이터 접근
- **Validation**: 요청 데이터 유효성 검증
- **Actuator**: 애플리케이션 상태 확인 및 모니터링
- **MySQL Connector/J**: Spring Boot와 MySQL 연결
- **Lombok**: Getter, Builder 등의 반복 코드 간소화

## 실행

### IntelliJ IDEA

메인 애플리케이션 클래스의 `main()` 메서드를 실행하거나 IntelliJ IDEA의 Run(▶) 버튼을 통해 실행한다.

### Gradle

터미널에서 실행하는 경우:

```bash
./gradlew bootRun
```

확인:

- 애플리케이션: http://localhost:8080
- 헬스체크: http://localhost:8080/actuator/health

빌드 / 테스트:

```bash
./gradlew build     # 컴파일 + 테스트 + jar
./gradlew test      # 테스트 코드만 실행
```