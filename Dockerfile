# ─── Stage 1 : Build ──────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Layer dépendances séparé du code source (mise en cache Docker optimale)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilation + packaging sans tests (les tests tournent en CI)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2 : Runtime ────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copier uniquement le JAR final depuis le stage builder
COPY --from=builder /app/target/*.jar app.jar

# Render expose le port via $PORT ; Spring le lit via server.port=${PORT:8080}
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
