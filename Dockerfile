# ─────────────────────────────────────────
# Stage 1: Build Spring Boot JAR bằng Gradle
# ─────────────────────────────────────────
FROM gradle:8.14-jdk17-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper và config trước (tận dụng layer cache)
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle/ gradle/

# Download dependencies trước (cache layer riêng, không bị vỡ khi đổi source)
RUN ./gradlew dependencies --no-daemon 2>/dev/null || true

# Copy source code
COPY src/ src/

# Build JAR, bỏ qua test (test sẽ chạy riêng trong CI pipeline)
RUN ./gradlew bootJar -x test --no-daemon

# ─────────────────────────────────────────
# Stage 2: Runtime image (nhẹ hơn nhiều)
# ─────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Cài curl — ECS healthCheck (ecs.tf) gọi "curl -f http://localhost:8080/actuator/health",
# nhưng base image "eclipse-temurin:17-jre-alpine" không có sẵn curl, khiến health check
# luôn báo lỗi "command not found" dù ứng dụng chạy bình thường, làm ECS liên tục kill task
RUN apk add --no-cache curl

# Tạo user non-root để bảo mật
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy JAR từ build stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Set quyền cho user non-root
RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

# Chạy với Spring profile "prod" để load application-prod.properties
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
