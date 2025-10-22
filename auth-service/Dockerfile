# syntax=docker/dockerfile:1
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY .. .
RUN mvn -q -DskipTests -pl auth-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/auth-service/target/auth-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-Duser.timezone=Asia/Kolkata","-jar","/app/app.jar"]
