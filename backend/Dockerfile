# Stage 1: Build application JAR
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Create non-root user
RUN addgroup -S marabagam && adduser -S marabagam -G marabagam

COPY --from=builder /app/target/marabagam-backend-1.0.0-SNAPSHOT.jar app.jar

RUN chown -R marabagam:marabagam /app

USER marabagam
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=20s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
