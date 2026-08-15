# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom first for better layer caching
COPY pom.xml .
COPY src ./src

# Build JAR package skipping tests for fast deployment
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the compiled JAR
COPY --from=build /app/target/*.jar app.jar

# Render supplies PORT env variable; configure memory limits for free tier
ENV JAVA_OPTS="-Xms256m -Xmx400m"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
