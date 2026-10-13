# Step 1: Build the jar using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests
 
# Step 2: Run the jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Render free plan has only 512 MB RAM, so we limit Java memory
ENTRYPOINT ["java", "-Xmx350m", "-jar", "app.jar"]
 
