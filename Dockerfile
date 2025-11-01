# syntax=docker/dockerfile:1

# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only pom first to leverage Docker layer caching for dependencies
COPY pom.xml ./
# Pre-fetch dependencies to improve build caching
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application (skip tests for faster container builds)
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package


# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre

# Create non-root user for security
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

WORKDIR /app

# Copy the fat jar from the build stage
# NOTE: Ensure the artifact name matches pom.xml (artifactId-version.jar)
COPY --from=build /workspace/target/payment-service-1.0.0.jar app.jar

# Expose HTTP and gRPC ports
EXPOSE 8083 50053

# Configurable runtime options via env vars
ENV JAVA_OPTS=""
ENV SERVER_PORT=8083
ENV GRPC_SERVER_PORT=50053

# Start the service
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${SERVER_PORT} -Dgrpc.server.port=${GRPC_SERVER_PORT} -jar app.jar"]
