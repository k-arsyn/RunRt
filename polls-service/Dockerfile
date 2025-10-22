# syntax=docker/dockerfile:1
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn clean package -DskipTests -pl polls-service -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/polls-service/target/polls-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "/app/app.jar"]
