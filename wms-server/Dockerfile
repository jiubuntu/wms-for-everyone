# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon || true

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S wms && adduser -S wms -G wms
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar
USER wms

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
