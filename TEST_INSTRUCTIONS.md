# Test Instructions

Step-by-step guide to test the URL Shortener API locally, on the deployed droplet, and with automated tests.

---

## Environments

| Environment | Base URL |
|-------------|----------|
| Local | `http://localhost:8080` |
| Deployed (DigitalOcean Droplet) | `http://168.144.149.214:8080` |

---

## Part 1 — Automated tests (JUnit)

Run from the project root:

```bash
cd urlshortner
./mvnw test
```

Run a specific test class:

```bash
./mvnw test -Dtest=UrlShortenerControllerTest
./mvnw test -Dtest=UrlServiceTest
./mvnw test -Dtest=UrlshortnerApplicationTests
```

**What gets tested automatically:**

| Test | Verifies |
|------|----------|
| `UrlShortenerControllerTest` | Create URL, redirect, duplicate alias (409), invalid URL (400) |
| `UrlServiceTest` | Duplicate alias throws correct exception |
| `UrlshortnerApplicationTests` | Application starts successfully |

**Expected output:** `BUILD SUCCESS` with all tests passing.

---

## Part 2 — Manual testing with curl

### Option A: Test locally

Start the app first:

```bash
./mvnw spring-boot:run
```

Then run the curl commands below using `http://localhost:8080`.

---

### Option B: Test on deployed droplet

Use `http://168.144.149.214:8080` in place of `http://localhost:8080` in every command below.

Quick reachability check:

```bash
curl -s -o /dev/null -w "HTTP %{http_code}\n" http://168.144.149.214:8080/doesnotexist999
```

Expected: `HTTP 404` (means the app is running).

---

## Sample curl commands

### 1) Create a short URL (with custom alias)

**Local:**

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.com","alias":"demo"}'
```

**Deployed:**

```bash
curl -X POST http://168.144.149.214:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.com","alias":"demo"}'
```

**Sample response (200 OK):**

```json
{
  "shortUrl": "http://localhost:8080/demo",
  "alias": "demo",
  "accessCount": 0,
  "createdAt": "2026-06-27T07:28:06.112344777Z"
}
```

---

### 2) Redirect to the original URL

**Local:**

```bash
curl -I http://localhost:8080/demo
```

**Deployed:**

```bash
curl -I http://168.144.149.214:8080/demo
```

**Sample response:**

```text
HTTP/1.1 307
Location: https://example.com
```

Follow the redirect end-to-end:

```bash
curl -sL -o /dev/null -w "Final URL: %{url_effective}\n" http://168.144.149.214:8080/demo
```

---

### 3) Get metadata for a short URL

**Local:**

```bash
curl http://localhost:8080/metadata/demo
```

**Deployed:**

```bash
curl http://168.144.149.214:8080/metadata/demo
```

**Sample response (200 OK):**

```json
{
  "shortCode": "demo",
  "originalUrl": "https://example.com",
  "createdAt": "2026-06-27T07:28:06.112345Z",
  "accessCount": 0
}
```

---

### 4) Create a short URL without an alias (auto-generated code)

**Local:**

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.org"}'
```

**Deployed:**

```bash
curl -X POST http://168.144.149.214:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.org"}'
```

**Sample response (200 OK):**

```json
{
  "shortUrl": "http://localhost:8080/d",
  "alias": "d",
  "accessCount": 0,
  "createdAt": "2026-06-27T07:28:07.316530611Z"
}
```

---

### 5) Duplicate alias (should fail)

Run **after** test 1 has created `demo`.

**Local:**

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.net","alias":"demo"}'
```

**Deployed:**

```bash
curl -X POST http://168.144.149.214:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.net","alias":"demo"}'
```

**Sample response (409 Conflict):**

```json
{
  "message": "Short code already exists"
}
```

---

### 6) Invalid URL (should fail)

**Local:**

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"not-a-valid-url"}'
```

**Deployed:**

```bash
curl -X POST http://168.144.149.214:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"not-a-valid-url"}'
```

**Sample response (400 Bad Request):**

```json
{
  "message": "Invalid URL"
}
```

---

### 7) Invalid alias format (should fail)

**Local:**

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.com","alias":"AB"}'
```

**Deployed:**

```bash
curl -X POST http://168.144.149.214:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.com","alias":"AB"}'
```

**Sample response (400 Bad Request):**

```json
{
  "message": "Validation failed",
  "errors": {
    "alias": "Alias must be 3-30 lowercase letters, numbers, or dashes"
  }
}
```

---

### 8) Unknown short code (should fail)

**Local:**

```bash
curl http://localhost:8080/doesnotexist999
```

**Deployed:**

```bash
curl http://168.144.149.214:8080/doesnotexist999
```

**Sample response (404 Not Found):**

```json
{
  "message": "Short code not found"
}
```

---

## Part 3 — Run all manual tests at once (smoke test)

Use the included script to run every scenario automatically:

**Local:**

```bash
export BASE_URL=http://localhost:8080
bash scripts/smoke-test.sh
```

**Deployed:**

```bash
export BASE_URL=http://168.144.149.214:8080
bash scripts/smoke-test.sh
```

**Expected output:**

```text
All smoke tests passed against http://168.144.149.214:8080
```

---

## Part 4 — Test checklist

Use this checklist when verifying the service:

| # | Test | Command | Expected |
|---|------|---------|----------|
| 1 | Create with alias | POST `/shorten` | 200 |
| 2 | Redirect | GET `/{alias}` | 307 + Location header |
| 3 | Metadata | GET `/metadata/{alias}` | 200 + JSON body |
| 4 | Auto-generated code | POST `/shorten` (no alias) | 200 |
| 5 | Duplicate alias | POST `/shorten` (same alias) | 409 |
| 6 | Invalid URL | POST `/shorten` | 400 |
| 7 | Invalid alias | POST `/shorten` | 400 |
| 8 | Unknown code | GET `/unknown` | 404 |
| 9 | Automated tests | `./mvnw test` | BUILD SUCCESS |

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Connection refused on droplet | Ensure app is running and port `8080` is open in firewall |
| `shortUrl` shows `localhost` on droplet | Set `APP_BASE_URL=http://168.144.149.214:8080` and restart the app |
| Port 80 does not work | App runs on port `8080` — include `:8080` in the URL |
| 409 on first create | Alias already exists — use a different alias like `demo2` |

---

## Related files

- [README.md](README.md) — project setup and run instructions
- [TESTING.md](TESTING.md) — extended testing guide (CI, test matrix, troubleshooting)
- [scripts/smoke-test.sh](scripts/smoke-test.sh) — automated smoke test script

---

## Quick copy-paste — one curl per case (deployed)

Base URL: `http://168.144.149.214:8080`

**1) Create short URL (custom alias) — expect 200**

```bash
curl -X POST http://168.144.149.214:8080/shorten -H "Content-Type: application/json" -d '{"url":"https://example.com","alias":"demo"}'
```

**2) Redirect — expect 307**

```bash
curl -I http://168.144.149.214:8080/demo
```

**3) Metadata — expect 200**

```bash
curl http://168.144.149.214:8080/metadata/demo
```

**4) Auto-generated code (no alias) — expect 200**

```bash
curl -X POST http://168.144.149.214:8080/shorten -H "Content-Type: application/json" -d '{"url":"https://example.org"}'
```

**5) Duplicate alias — expect 409**

```bash
curl -X POST http://168.144.149.214:8080/shorten -H "Content-Type: application/json" -d '{"url":"https://example.net","alias":"demo"}'
```

**6) Invalid URL — expect 400**

```bash
curl -X POST http://168.144.149.214:8080/shorten -H "Content-Type: application/json" -d '{"url":"not-a-valid-url"}'
```

**7) Invalid alias — expect 400**

```bash
curl -X POST http://168.144.149.214:8080/shorten -H "Content-Type: application/json" -d '{"url":"https://example.com","alias":"AB"}'
```

**8) Unknown short code — expect 404**

```bash
curl http://168.144.149.214:8080/doesnotexist999
```

**9) Automated tests — expect BUILD SUCCESS**

```bash
./mvnw test
```
