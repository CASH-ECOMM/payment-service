# Multi-stage Dockerfile for Payment Service
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Install system dependencies for building
RUN apk add --no-cache \
    postgresql-client \
    && rm -rf /var/cache/apk/*

# Copy Maven configuration files and download dependencies
COPY pom.xml .
COPY .mvn .mvn
RUN mvn dependency:go-offline -B

# Copy application code
COPY src ./src

# Generate gRPC code from proto files and build application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install system dependencies
RUN apk add --no-cache \
    postgresql-client \
    curl \
    && rm -rf /var/cache/apk/*

# Copy the built JAR from builder stage
COPY --from=builder /app/target/payment-service-1.0.0.jar app.jar

# Expose REST API and gRPC ports
EXPOSE 8083 9093

# Run the application
CMD ["java", "-jar", "app.jar"]