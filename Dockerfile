FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache python3 py3-pip

WORKDIR /
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=prod"]
