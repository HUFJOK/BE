# 1️⃣ JDK 이미지 선택 (예: OpenJDK 17)
FROM eclipse-temurin:17-jdk-jammy

# 2️⃣ JAR 파일을 컨테이너로 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 3️⃣ 실행 명령
ENTRYPOINT ["java","-jar","/app.jar"]