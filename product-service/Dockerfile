# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copy Maven wrapper and POM first to leverage Docker layer caching
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./

# Download all dependencies (cached as a separate layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copy source code and build the fat JAR (tests are skipped — CI already ran them)
COPY src ./src
RUN ./mvnw clean package -DskipTests -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app

# Run as a non-root user (security best practice)
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# Copy only the executable JAR from the build stage
COPY --chown=appuser:appgroup --from=builder /app/target/*.jar app.jar
USER appuser

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
