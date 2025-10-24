FROM docker.io/library/maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM docker.io/library/eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
