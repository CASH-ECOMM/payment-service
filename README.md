# Payment Service

Payment microservice that processes payments and exposes both HTTP (Spring Boot) and gRPC endpoints.

## Quick Start

### 1. Prerequisites
- Java 17
- Maven 3.9+
- Docker & Docker Compose (for containerized deployment)
- PostgreSQL (local or via Docker)

### 2. Running with Maven

Ensure Java 17 is active first
- macOS (zsh):
```zsh
# Use macOS Java selector to point JAVA_HOME to JDK 17 for this session
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"

# Verify
java -version
mvn -v
```

- Windows PowerShell:
```powershell
# Set for current PowerShell session (adjust the JDK path to your install)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# Verify
java -version
mvn -v
```

- Windows CMD:
```bat
REM Set for current CMD session (adjust path to where JDK 17 is installed)
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

REM Verify
java -version
mvn -v
```

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
