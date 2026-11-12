# Multi-stage Dockerfile for India Post Philately Spring Boot Portal
# Stage 1: Build JAR with Maven & Eclipse Temurin JDK 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy Java source code and static web resources
COPY src ./src

# Package executable fat JAR (skipping test execution during container build)
RUN mvn clean package -DskipTests=true

# Stage 2: Minimal Distroless / Temurin JRE 21 Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create unprivileged system user for OWASP container security
RUN addgroup -S postgroup && adduser -S postuser -G postgroup
USER postuser

# Copy built artifact from builder
COPY --from=builder /app/target/stamp-sih-philately-1.0.0.jar app.jar

# Render dynamic port binding support
ENV PORT=8085
EXPOSE 8085

# Java performance flags for containers
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -jar app.jar"]
