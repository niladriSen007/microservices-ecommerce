
# Stage 1 — Build
FROM maven:3.9.6-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 — Runtime
FROM eclipse-temurin:25-jre-alpine
COPY --from=build /app/target/inventory-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]