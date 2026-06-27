#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ALIAS="test$(date +%s | tail -c 6)"

echo "=== 1. Create short URL ==="
CREATE=$(curl -s -w "\nHTTP:%{http_code}" -X POST "$BASE_URL/shorten" \
  -H "Content-Type: application/json" \
  -d "{\"url\":\"https://example.com\",\"alias\":\"$ALIAS\"}")
echo "$CREATE"
echo "$CREATE" | grep -q "HTTP:200" || { echo "FAIL: expected 200"; exit 1; }

echo ""
echo "=== 2. Redirect ==="
REDIRECT=$(curl -sI -w "HTTP:%{http_code}" "$BASE_URL/$ALIAS")
echo "$REDIRECT" | head -5
echo "$REDIRECT" | grep -q "HTTP/1.1 307" || echo "WARN: expected 307"
echo "$REDIRECT" | grep -qi "Location: https://example.com" || { echo "FAIL: bad Location header"; exit 1; }

echo ""
echo "=== 3. Metadata ==="
META=$(curl -s -w "\nHTTP:%{http_code}" "$BASE_URL/metadata/$ALIAS")
echo "$META"
echo "$META" | grep -q "HTTP:200" || { echo "FAIL: expected 200"; exit 1; }

echo ""
echo "=== 4. Auto-generated code ==="
AUTO=$(curl -s -w "\nHTTP:%{http_code}" -X POST "$BASE_URL/shorten" \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.org"}')
echo "$AUTO"
echo "$AUTO" | grep -q "HTTP:200" || { echo "FAIL: expected 200"; exit 1; }

echo ""
echo "=== 5. Duplicate alias (expect 409) ==="
DUP=$(curl -s -w "\nHTTP:%{http_code}" -X POST "$BASE_URL/shorten" \
  -H "Content-Type: application/json" \
  -d "{\"url\":\"https://example.net\",\"alias\":\"$ALIAS\"}")
echo "$DUP"
echo "$DUP" | grep -q "HTTP:409" || { echo "FAIL: expected 409"; exit 1; }

echo ""
echo "=== 6. Invalid URL (expect 400) ==="
BAD=$(curl -s -w "\nHTTP:%{http_code}" -X POST "$BASE_URL/shorten" \
  -H "Content-Type: application/json" \
  -d '{"url":"not-a-valid-url"}')
echo "$BAD"
echo "$BAD" | grep -q "HTTP:400" || { echo "FAIL: expected 400"; exit 1; }

echo ""
echo "=== 7. Unknown short code (expect 404) ==="
NF=$(curl -s -w "\nHTTP:%{http_code}" "$BASE_URL/doesnotexist999")
echo "$NF"
echo "$NF" | grep -q "HTTP:404" || { echo "FAIL: expected 404"; exit 1; }

echo ""
echo "All smoke tests passed against $BASE_URL"
