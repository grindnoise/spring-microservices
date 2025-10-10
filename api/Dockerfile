# Этап 1: сборка (multi-stage, build)
# Базовый образ: gradle:8.14.2-jdk24 - среда со JDK 24 и Gradle.
# ARG ... + ENV ... - пробрасывает креды и URL Nexus (для публикации артефактов).

FROM gradle:8.14.2-jdk24 AS build
ARG NEXUS_URL=http://host.docker.internal:8081/repository/maven-snapshots
ARG NEXUS_USERNAME=admin
ARG NEXUS_PASSWORD=admin

# Копирование проекта:
# сначала build.gradle.kts, settings.gradle.kts(лучше кэшируются зависимости), затем src и openapi.
WORKDIR /home/gradle/project

COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src ./src
COPY openapi ./openapi

# Сборка:
# gradle --no-daemon clean build -x test - собирает JAR (без тестов).
RUN gradle --no-daemon clean build -x test

# Этап 2: рантайм (runtime)
# Базовый образ: eclipse-temurin:24-jre - облегчённый JRE для запуска.
FROM eclipse-temurin:24-jre

# Устанавливает curl (для health-check/отладки), чистит apt-кэш.
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# WORKDIR /app
# Копирует из билд-слоя все JAR'ы
# build/libs/*jar kak app.jar.
WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8091

# Команда запуска: java-jar app.jar.
CMD ["java", "-jar", "app.jar"]