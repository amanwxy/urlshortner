# URL Shortener Design Document

## 1. Overview
This project implements a URL shortener service with a layered Spring Boot architecture:

- Controller layer: exposes REST endpoints for shortening URLs, redirecting short codes, and returning metadata.
- Service layer: contains business logic for URL validation, short-code generation, alias handling, and access-count updates.
- Repository layer: persists URL records in a relational database.
- Cache layer: uses Redis for fast lookup of redirect targets.

The system is designed to support:
- creating short URLs
- redirecting short codes to original URLs
- retrieving metadata for a short URL
- basic concurrency-safe creation behavior
- optional Redis-backed caching for hot lookups

## 2. Requirements

### 2.1 Functional Requirements
- The system shall accept a long URL and create a short URL.
- The system shall support optional custom aliases for short URLs.
- The system shall redirect a short URL to its original long URL.
- The system shall return metadata for a short URL, including creation time and access count.
- The system shall prevent duplicate short codes or aliases from being created.
- The system shall validate the input URL format before storing it.

### 2.2 Non-Functional Requirements
- The system shall be accessible through a REST API.
- The system shall support concurrent requests safely.
- The system shall provide fast redirect resolution using Redis cache.
- The system shall persist URL records reliably in a relational database.
- The system shall be easy to run locally for development and testing.
- The system shall be maintainable through a layered Spring Boot architecture.

## 3. Architecture

### 3.1 Components
- Spring Boot application entrypoint
- REST controller for HTTP endpoints
- Service for business rules and orchestration
- JPA repository for persistence
- Redis cache service for fast reads
- Entity model representing stored URL data

### 3.2 Request Flow
1. Client sends a POST request to /shorten.
2. Controller forwards the request to the service layer.
3. Service validates the URL and resolves the short code.
4. If the short code is new, the service creates a record in the database.
5. The service stores the redirect target in Redis for quick access.
6. On redirect requests, the service checks Redis first, then falls back to the database.

## 4. API Endpoints
- POST /shorten
  - Creates a short URL from a long URL.
- GET /{shortCode}
  - Redirects to the original URL.
- GET /metadata/{shortCode}
  - Returns metadata such as the original URL, creation time, and access count.

## 5. Database Schema (PostgreSQL / JPA)
The application persists URL data in a relational table. The current entity model is designed around a single table:

### 5.1 Table: urls
| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| id | BIGINT | PRIMARY KEY, GENERATED | Unique identifier |
| short_code | VARCHAR(255) | UNIQUE, NOT NULL | Short code used in the URL |
| original_url | TEXT | NOT NULL | The original long URL |
| created_at | TIMESTAMP | NOT NULL | When the record was created |
| access_count | INTEGER | NOT NULL, DEFAULT 0 | Number of redirects served |

### 5.2 Notes
- The short code is used as the lookup key for redirect requests.
- The original URL is stored as text to allow long URLs.
- The access count is incremented when a redirect is resolved.

### 5.3 Example SQL
```sql
CREATE TABLE urls (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(255) NOT NULL UNIQUE,
    original_url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    access_count INTEGER NOT NULL DEFAULT 0
);
```

## 6. Redis Schema
Redis is used as a cache for hot redirect lookups.

### 6.1 Key Pattern
- `url:{shortCode}` -> original URL value
- `hit:{shortCode}` -> count of cache hits (optional operational metric)

### 6.2 Example Redis Entries
```text
url:abc123 -> https://example.com/some/long/path
hit:abc123 -> 12
```

### 6.3 Cache Behavior
- On shorten request, the new mapping is written to Redis with a TTL.
- On redirect request, the service checks Redis first.
- If the value is missing, it loads from the database and repopulates Redis.

## 7. Data Flow
### Create Short URL
1. Validate input URL.
2. Generate or accept a short code.
3. Save the record in the database.
4. Cache the mapping in Redis.

### Resolve Short URL
1. Check Redis for the short code.
2. If found, return the URL.
3. If not found, query the database.
4. Update the database access count and refresh Redis.

## 8. Concurrency Considerations
- The service should prevent duplicate short codes from being inserted.
- A unique constraint on the database schema protects against race conditions at the persistence layer.
- The application should rely on the database uniqueness rule for correctness even if multiple requests arrive simultaneously.

## 9. Future Improvements
- Add expiration/cleanup policies for old records.
- Add user/account-based ownership of short URLs.
- Add analytics and click tracking.
- Add rate limiting and abuse protection.
- Move from simple Redis string values to structured entries if advanced metadata is needed.
