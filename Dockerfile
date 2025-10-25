# Stage 1: Build con Maven usando cache
FROM docker.io/library/maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copiar solo pom.xml y cachear dependencias
COPY pom.xml .
# Usar BuildKit cache para Maven
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B dependency:go-offline

# Copiar el código fuente
COPY src ./src

# Compilar el proyecto usando cache de Maven
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package

# Stage 2: Imagen runtime
FROM docker.io/library/eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
