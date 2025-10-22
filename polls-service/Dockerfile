# syntax=docker/dockerfile:1
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY .. .
RUN mvn -q -DskipTests -pl polls-service -am package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/polls-service/target/polls-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-Duser.timezone=Asia/Kolkata","-jar","/app/app.jar"]
