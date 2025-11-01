# Payment Service

gRPC-based microservice for managing payment operations, including transaction processing, payment validation, and integration with external payment gateways.

## Quick Start

### 1. Setup Environment Variables

```bash
cp .env.example .env
```

Edit `.env` with your configuration (database credentials, payment gateway keys, etc.).

### 2. Running with CLI

**Build the project:**
```bash
mvn clean package
```

**Run the service:**
```bash
java -jar target/payment-service-*.jar
```

Service will start on:
- HTTP (Spring Boot): http://localhost:8083
- gRPC: localhost:50053

### 3. Running with Docker

This project ships with a multi-stage Dockerfile. You only need Docker installed; Java/Maven are built inside the image.

Stage A — Build the image locally
```bash
docker build -t payment-service:dev .
```

Stage B — Run using Postgres on your host (macOS/Windows)
- The container reaches your host Postgres via `host.docker.internal`.
```bash
docker run --rm \
  -p 8083:8083 \
  -p 50053:50053 \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/payment_db" \
  -e SPRING_DATASOURCE_USERNAME="postgres" \
  -e SPRING_DATASOURCE_PASSWORD="2025" \
  -e SERVER_PORT=8083 \
  -e GRPC_SERVER_PORT=50053 \
  --name payment-service \
  payment-service:dev
```
- Linux note: add this flag so `host.docker.internal` resolves:
```bash
docker run ... --add-host=host.docker.internal:host-gateway
```

Stage C — Run Postgres in Docker and network them (no dependency on host DB)
```bash
# Create a shared network (idempotent)
docker network create ecommerce 2>/dev/null || true

# Start Postgres (adjust creds/db as needed)
docker run -d --name postgres \
  --network ecommerce \
  -e POSTGRES_PASSWORD=2025 \
  -e POSTGRES_DB=payment_db \
  -p 5432:5432 \
  postgres:16

# Start the payment service on the same network
docker run --rm \
  --network ecommerce \
  -p 8083:8083 \
  -p 50053:50053 \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://postgres:5432/payment_db" \
  -e SPRING_DATASOURCE_USERNAME="postgres" \
  -e SPRING_DATASOURCE_PASSWORD="2025" \
  -e SERVER_PORT=8083 \
  -e GRPC_SERVER_PORT=50053 \
  --name payment-service \
  payment-service:dev
```

Stage D — Logs and stopping
```bash
# Follow logs
docker logs -f payment-service

# Stop the service
docker stop payment-service
```

Stage E — Multi-arch build and push (optional)
If you want to publish a multi-arch image (arm64/amd64) to Docker Hub:
```bash
# Login first
docker login

# Ensure a builder exists and is active
docker buildx create --use --name multi 2>/dev/null || docker buildx use multi
docker buildx inspect --bootstrap

# Replace YOUR_DOCKERHUB_USERNAME with your username
docker buildx build --platform linux/amd64,linux/arm64 \
  -t YOUR_DOCKERHUB_USERNAME/payment-service:dev \
  --push .
```

Environment overrides
- HTTP port: `SERVER_PORT` (default 8083)
- gRPC port: `GRPC_SERVER_PORT` (default 50053)
- DB URL: `SPRING_DATASOURCE_URL` (e.g., `jdbc:postgresql://host.docker.internal:5432/payment_db`)
- DB user/pass: `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`

## Features

- **ProcessPayment** - Handle payment transactions securely.
- **ValidatePayment** - Verify payment details and status.
- **RefundPayment** - Process refunds for completed transactions.
- **GetPaymentDetails** - Retrieve payment information by transaction ID.
- **Integration** - Connect with external payment gateways (e.g., Stripe, PayPal).

## Tech Stack

- Java / Spring Boot
- gRPC / Protocol Buffers
- PostgreSQL
- Docker / Docker Compose
- Maven for build automation
