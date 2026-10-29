FROM eclipse-temurin:25-jre

WORKDIR /app

# 컨테이너 기본 시간대를 한국 시간으로 지정한다.
ENV TZ=Asia/Seoul

COPY build/libs/*.jar app.jar

EXPOSE 8080

# 실행 중인 JVM의 기본 시간대도 명시적으로 지정한다.
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]