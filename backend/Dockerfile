# Multi-stage Dockerfile for Racing Stats Backend

# Stage 1: Build
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy Gradle configuration files
COPY build.gradle settings.gradle ./

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN gradle clean bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Install necessary packages
RUN apk add --no-cache \
    curl \
    netcat-openbsd

WORKDIR /app

# Create logs directory
RUN mkdir -p /app/logs

# Copy JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose ports
EXPOSE 8080 9996/udp 20777/udp 33740/udp

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]