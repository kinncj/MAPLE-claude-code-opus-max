# syntax=docker/dockerfile:1
# Multi-stage build: vite (frontend) -> maven (jar with SPA embedded) -> temurin JRE runtime.
# Build context is the repo root. Result: one self-contained image serving SPA + /api/v1 on :8080.

# ---- Stage 1: build the React SPA -----------------------------------------
FROM node:24-alpine AS web
WORKDIR /web
COPY app/web/package.json app/web/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY app/web/ ./
RUN npm run build

# ---- Stage 2: build the Spring Boot jar, embedding the SPA as static -------
FROM maven:3.9-eclipse-temurin-21 AS api
WORKDIR /api
COPY app/api/pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY app/api/src ./src
COPY --from=web /web/dist ./src/main/resources/static
RUN mvn -B -q -DskipTests clean package

# ---- Stage 3: minimal runtime ---------------------------------------------
FROM eclipse-temurin:21-jre AS runtime
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=api /api/target/todo-api.jar ./app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=12 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
