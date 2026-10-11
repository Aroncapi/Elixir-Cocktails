# Imagen generica para los 5 microservicios Java.
# Uso: docker build --build-arg MODULE=gateway-service .
FROM maven:3.9-eclipse-temurin-25 AS build
ARG MODULE
WORKDIR /build
COPY ${MODULE}/pom.xml pom.xml
COPY ${MODULE}/src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
