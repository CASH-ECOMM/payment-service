# Payment Service

Payment microservice that processes payments and exposes both HTTP (Spring Boot) and gRPC endpoints.

## Quick Start

### 1. Prerequisites
- Java 17
- Maven 3.9+
- Docker & Docker Compose (for containerized deployment)
- PostgreSQL (local or via Docker)

### 2. Running with Maven

Build the application:
```bash
./mvnw clean package -DskipTests
```

Run the service:
```bash
./mvnw spring-boot:run
```

Service will start at:
- HTTP: http://localhost:8083
- gRPC: localhost:50053

### 3. Running with Docker

Build and run with Docker Compose (uses the included Dockerfile and docker-compose.yml):
```bash
cp .env.example .env
# adjust values if needed

docker compose up --build
```

Stop the service:
```bash
docker compose down
```

## API Documentation

Swagger UI:
```
http://localhost:8083/swagger-ui.html
```

OpenAPI JSON spec:
```
http://localhost:8083/api-docs
```

## Tech Stack
- Spring Boot 3 (HTTP endpoints)
- gRPC Server (Payment RPCs)
- Protocol Buffers (IDL)
- SpringDoc OpenAPI (docs)
- PostgreSQL (persistence)
- Maven (build)
- Docker / Docker Compose (containerization)
