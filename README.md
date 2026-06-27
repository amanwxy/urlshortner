# URL Shortener Service

A Spring Boot REST API for shortening long URLs, redirecting to the original destination, and tracking basic metadata such as creation time and access count.

## Features
- Create a short URL from a long URL
- Support optional aliases
- Reject invalid URLs and duplicate aliases
- Redirect shortened links to the original URL
- Return metadata for each shortened entry
- Use Redis-backed caching for faster repeated lookups

## Prerequisites
- Java 21
- Maven Wrapper (included)
- PostgreSQL running locally
- Redis running locally

## Setup
1. Clone the repository
2. Start PostgreSQL and Redis locally
3. Set the required environment variables:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/urlshortener
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export DB_DRIVER_CLASS_NAME=org.postgresql.Driver
export HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

## Run the application
```bash
./mvnw spring-boot:run
```

The app will start on port 8080 by default.

## API examples with curl

### 1) Shorten a URL
```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.com","alias":"demo"}'
```

### 2) Redirect to the original URL
```bash
curl -I http://localhost:8080/demo
```

### 3) Get metadata for a short URL
```bash
curl http://localhost:8080/metadata/demo
```

### 4) Shorten a URL without an alias
```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.org"}'
```

### 5) Try a duplicate alias
```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.net","alias":"demo"}'
```

### 6) Try an invalid URL
```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"not-a-valid-url"}'
```

## Testing
```bash
./mvnw test
```

## CI/CD
A basic GitHub Actions workflow is included in [.github/workflows/ci.yml](.github/workflows/ci.yml).
