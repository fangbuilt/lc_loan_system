# =========================
# Stage 1 - Build
# =========================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

RUN ./mvnw -B dependency:go-offline

COPY src src

RUN ./mvnw clean package -DskipTests


# =========================
# Stage 2 - Runtime
# =========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

RUN mkdir -p /app/logs && chown -R spring:spring /app

COPY --from=builder /app/target/*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]
