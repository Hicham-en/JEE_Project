FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache python3 py3-pip

WORKDIR /app
COPY target/*.jar app.jar
COPY scripts/ scripts/

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
