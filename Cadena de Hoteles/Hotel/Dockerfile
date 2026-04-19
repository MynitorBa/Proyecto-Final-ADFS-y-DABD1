# ─────────────────────────────────────────────
# ETAPA 1 — Build: compila el fat JAR con Maven
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copia el pom.xml primero para aprovechar la cache de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el codigo fuente y compila generando fat JAR (con todas las dependencias)
COPY src ./src
RUN mvn package -Dmaven.test.skip=true -B

# ─────────────────────────────────────────────
# ETAPA 2 — Runtime: solo JRE, sin Maven ni sources
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# El assembly plugin genera: Hotel-1.0-SNAPSHOT-jar-with-dependencies.jar
COPY --from=builder /app/target/Hotel-*-jar-with-dependencies.jar app.jar

# Puerto que expone Javalin
EXPOSE 7000

# Variables de entorno — sobreescritas por docker-compose
ENV DB_URL=jdbc:oracle:thin:@host.docker.internal:1521/XEPDB1
ENV DB_USER=system
ENV DB_PASS=meme1234

ENTRYPOINT ["java", "-jar", "app.jar"]