// ############################################################################
// Gradle 빌드 스크립트 (Kotlin DSL)
// ----------------------------------------------------------------------------
// Spring Boot 4.1.0 + Java 25 (LTS) 기준.
//
// ⚠️ Java 26 도 나와 있지만 LTS 가 아니다. LTS 는 17 → 21 → 25 순서다.
//    LTS 가 아닌 버전은 지원 기간이 6개월이라 실무 프로젝트에 쓰지 않는다.
// ############################################################################

plugins {
	java
	id("org.springframework.boot") version "4.1.0"
	// Boot 가 관리하는 라이브러리 버전을 자동으로 맞춰준다.
	// 이게 있어서 아래 dependencies 에 버전을 안 적어도 된다.
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.metionlabs"
version = "0.0.1-SNAPSHOT"
description = "주식 시뮬레이터"

java {
	// 누가 어느 머신에서 빌드해도 같은 자바 버전이 쓰이도록 고정한다.
	// 로컬에 25 가 없으면 gradle 이 알아서 받아온다.
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// ------------------------------------------------------------------------
	// 웹 (REST API)
	// ------------------------------------------------------------------------
	// ⚠️ Boot 4 에서 이름이 바뀌었다. Boot 3 까지는 spring-boot-starter-web 이었다.
	//    옛날 블로그를 보고 web 이라고 적으면 못 찾는다.
	// 내장 톰캣 + Spring MVC + JSON 변환(Jackson)이 한 번에 딸려온다.
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

	// ------------------------------------------------------------------------
	// DB 접근 (JPA)
	// ------------------------------------------------------------------------
	// Spring Data JPA + Hibernate + 트랜잭션 + HikariCP(커넥션 풀)가 함께 온다.
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// ------------------------------------------------------------------------
	// 입력값 검증 (@NotNull, @Positive 등)
	// ------------------------------------------------------------------------
	// ⚠️ 이걸 빠뜨리면 @Valid 를 붙여도 검증이 **조용히 무시된다.** 에러도 안 난다.
	//    "왜 검증이 안 되지?" 로 한참 헤매는 대표적인 함정.
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// ------------------------------------------------------------------------
	// 운영 상태 확인 (/actuator/health 등)
	// ------------------------------------------------------------------------
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// ------------------------------------------------------------------------
	// DB — 일단 H2(인메모리)로 시작한다
	// ------------------------------------------------------------------------
	// runtimeOnly = 컴파일에는 필요 없고 실행할 때만 필요하다는 뜻.
	// 우리 코드는 JPA 인터페이스만 쓰고 H2 클래스를 직접 참조하지 않으므로
	// 이렇게 두면 "실수로 특정 DB 전용 코드를 쓰는 것" 도 컴파일 단계에서 막힌다.

	runtimeOnly("com.mysql:mysql-connector-j")

	// ------------------------------------------------------------------------
	// 테스트
	// ------------------------------------------------------------------------
	// ⚠️ Boot 4 부터 테스트 스타터가 기능별로 쪼개졌다.
	//    Boot 3 의 spring-boot-starter-test 하나로 다 되던 시절과 다르다.
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// 컴파일 전용 + 어노테이션 프로세서로 설정(?)
//	implementation("org.projectlombok:lombok:1.18.46")

	compileOnly("org.projectlombok:lombok:1.18.46")
	annotationProcessor("org.projectlombok:lombok:1.18.46")

	testCompileOnly("org.projectlombok:lombok:1.18.46")
	testAnnotationProcessor("org.projectlombok:lombok:1.18.46")

}

tasks.withType<Test> {
	// ⚠️ 없으면 테스트가 **하나도 안 돌면서 조용히 성공한다.** 초록불에 속지 말 것.
	useJUnitPlatform()
}
