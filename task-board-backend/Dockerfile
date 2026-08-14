FROM gradle:9-jdk-25-and-26-alpine AS build
WORKDIR /app

COPY build.gradle settings.gradle /app/
COPY lombok.config lombok.config /app/
COPY src /app/src

RUN gradle build --no-daemon


FROM eclipse-temurin:26-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/task-board-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]