# Build context must be the project root:
#   docker build -f order-service/Dockerfile -t order-service .

# ─── Stage 1: dependency cache ───────────────────────────────────────────────
# Copy only POM files first. This layer is re-used on every rebuild until a
# POM changes, so Maven never re-downloads dependencies on a code-only change.
FROM maven:3.9-eclipse-temurin-21-alpine AS deps
WORKDIR /build

COPY pom.xml                    .
COPY common-dtos/pom.xml        common-dtos/
COPY api-gateway/pom.xml        api-gateway/
COPY inventory-service/pom.xml  inventory-service/
COPY order-service/pom.xml      order-service/
COPY product-service/pom.xml    product-service/
COPY service-discovery/pom.xml  service-discovery/
COPY user-service/pom.xml       user-service/

RUN mvn dependency:go-offline \
        -pl common-dtos,order-service \
        --no-transfer-progress \
        -q

# ─── Stage 2: compile & package ──────────────────────────────────────────────
FROM deps AS builder

COPY common-dtos/src    common-dtos/src
COPY order-service/src  order-service/src

RUN mvn package \
        -pl common-dtos,order-service \
        -DskipTests \
        --no-transfer-progress \
        -q

# ─── Stage 3: extract Spring Boot layers ─────────────────────────────────────
# Splitting the fat JAR into layers lets Docker cache the heavy dependencies
# layer separately from the tiny application layer (~100 KB).
FROM eclipse-temurin:21-jre-alpine AS extractor
WORKDIR /extract
COPY --from=builder /build/order-service/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ─── Stage 4: minimal runtime image ──────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy layers least-to-most volatile: dependencies rarely change, app code often does.
COPY --from=extractor /extract/dependencies/          ./
COPY --from=extractor /extract/spring-boot-loader/    ./
COPY --from=extractor /extract/snapshot-dependencies/ ./
COPY --from=extractor /extract/application/           ./

USER appuser

EXPOSE 8083

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]
