# =========================
# STAGE 1 - BUILD
# =========================
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copia apenas o pom primeiro (cache de dependências)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte
COPY src ./src

# Build do projeto
RUN mvn clean package -DskipTests


# =========================
# STAGE 2 - RUNTIME
# =========================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia o JAR gerado no build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
