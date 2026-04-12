FROM gradle:jdk21-alpine AS builder

WORKDIR /workspace

COPY notification_service/build.gradle notification_service/settings.gradle ./notification_service/
COPY shared_security/src ./shared_security/src
COPY shared_resilience/src ./shared_resilience/src

WORKDIR /workspace/notification_service

RUN gradle dependencies --no-daemon

COPY notification_service/src ./src

RUN gradle build --no-daemon -x test


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /workspace/notification_service/build/libs/*.jar app.jar

EXPOSE 1116

ENTRYPOINT ["java", "-jar", "app.jar"]
