FROM eclipse-temurin:25-jre

WORKDIR /app

# 컨테이너 기본 시간대를 한국 시간으로 지정한다.
ENV TZ=Asia/Seoul

# Docker Healthcheck에서 사용할 curl 설치
# 설치 후 불필요한 패키지 목록을 삭제하여 이미지 크기를 줄인다.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# 빌드된 Spring Boot JAR 파일을 컨테이너에 복사한다.
COPY build/libs/*.jar app.jar

EXPOSE 8080

# 실행 중인 JVM의 기본 시간대도 명시적으로 지정한다.
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]