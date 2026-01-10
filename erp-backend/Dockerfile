# =========================
# BUILD
# =========================
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Maven Wrapper
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Código fonte
COPY src src

RUN ./mvnw clean package -DskipTests


# =========================
# RUNTIME
# =========================
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/erp-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
