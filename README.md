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

Service will start on `localhost:8083` for REST API and `localhost:9090` for gRPC.

### 3. Running with Docker

```bash
docker-compose up --build
```

This starts both the payment service and PostgreSQL database.

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
