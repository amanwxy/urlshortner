# URL Shortener Service

A production-ready Spring Boot REST API for shortening long URLs, redirecting to the original destination, and tracking simple metadata such as creation time and access count.

## Features
- Create a short URL from a long URL
- Support optional aliases
- Reject invalid URLs and duplicate aliases
- Redirect shortened links to the original URL
- Keep basic metadata for each shortened entry
- Use an in-memory cache for faster repeated lookups

## Architecture Flow
```mermaid
flowchart LR
    Client[Client] -->|POST /shorten| Controller[Controller]
    Controller --> Service[UrlShortenerService]
    Service -->|store| Entries[(In-memory entries)]
    Service -->|cache| Cache[(Cache)]
    Client -->|GET /{code}| Controller
    Controller --> Service
    Service -->|redirect| Client
```

## Running locally
```bash
./mvnw spring-boot:run
```

## Example requests
Create a short URL:
```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.com","alias":"demo"}'
```

Access a shortened link:
```bash
curl -I http://localhost:8080/demo
```

## Testing
```bash
./mvnw test
```

## CI/CD
A basic GitHub Actions workflow is included in [.github/workflows/ci.yml](.github/workflows/ci.yml).
