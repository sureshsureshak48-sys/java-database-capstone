# Stage 1: Build the Spring Boot application using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and source code from the app directory
COPY app/pom.xml .
COPY app/src ./src

# Package the application (skip tests for faster container build)
RUN mvn clean package -DskipTests

# Stage 2: Run the compiled JAR in a lightweight JRE container
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
