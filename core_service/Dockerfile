FROM gradle:jdk21-alpine AS builder

WORKDIR /workspace

COPY core_service/build.gradle core_service/settings.gradle ./core_service/
COPY shared_security ./shared_security

WORKDIR /workspace/core_service

RUN gradle dependencies --no-daemon

COPY core_service/src ./src

RUN gradle build --no-daemon -x test



FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /workspace/core_service/build/libs/*.jar app.jar

EXPOSE 1111

ENTRYPOINT ["java", "-jar", "app.jar"]