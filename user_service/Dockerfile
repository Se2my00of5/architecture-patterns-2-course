FROM gradle:9.3-jdk21-alpine AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

RUN gradle dependencies --no-daemon

COPY src src

RUN gradle bootJar --no-daemon


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/user_service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 1115

ENTRYPOINT ["java", "-jar", "app.jar"]