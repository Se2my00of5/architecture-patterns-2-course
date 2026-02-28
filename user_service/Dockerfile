FROM gradle:jdk21-alpine AS builder

WORKDIR /app

COPY build.gradle settings.gradle ./

RUN gradle dependencies --no-daemon

COPY src ./src

RUN gradle build --no-daemon -x test



FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 1115

ENTRYPOINT ["java", "-jar", "app.jar"]