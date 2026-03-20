FROM gradle:jdk21-alpine AS builder

WORKDIR /workspace

COPY user_service/build.gradle user_service/settings.gradle ./user_service/
COPY shared_security/src ./shared_security/src

WORKDIR /workspace/user_service

RUN gradle dependencies --no-daemon

COPY user_service/src ./src

RUN gradle build --no-daemon -x test



FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /workspace/user_service/build/libs/*.jar app.jar

EXPOSE 1115

ENTRYPOINT ["java", "-jar", "app.jar"]