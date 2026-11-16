# Stage 1: build the Spring Boot executable JAR.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp -DskipTests clean package

# Stage 2: production runtime without Maven, as a non-root user.
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system uniroom && useradd --system --gid uniroom --no-create-home uniroom
COPY --from=build --chown=uniroom:uniroom /workspace/target/*.jar app.jar
USER uniroom
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
