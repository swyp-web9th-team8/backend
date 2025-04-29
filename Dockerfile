# 📦 gradle-wrapper.jar 생성 스테이지
FROM gradle:7.6-jdk17 AS wrapper-generator

WORKDIR /app

# wrapper 설정 복사 (gradle-wrapper.properties만 있으면 충분)
COPY gradlew .
COPY build.gradle .
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/

# wrapper 실행해서 gradle-wrapper.jar 생성
RUN gradle wrapper

# 첫 번째 스테이지: 빌드 스테이지
FROM gradle:7.6-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드와 Gradle 래퍼 복사
# gradle-wrapper.jar를 먼저 복사
COPY --from=wrapper-generator /app/gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Gradle 래퍼에 실행 권한 부여
RUN chmod +x ./gradlew

# 종속성 설치
# 이 단계에서 변경사항이 없다면, 다음 빌드에서 캐시됩니다.
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew build --no-daemon

# 두 번째 스테이지: 실행 스테이지
FROM eclipse-temurin:17-jdk

# 작업 디렉토리 설정
WORKDIR /app

# 첫 번째 스테이지에서 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행할 JAR 파일 지정
ENTRYPOINT ["java", "-jar", "app.jar"]
