# API Test Results Summary

## ✅ All Endpoints Working Successfully!

### 1. Health Check Endpoint
**Request:**
```bash
GET http://localhost:8080/health
```

**Response:**
```json
{
  "status": "ok"
}
```
✅ **Status:** Working

---

### 2. Ingest Activities Endpoint
**Request:**
```bash
POST http://localhost:8080/ingest
Content-Type: application/json

[
  { "userId": "U1001", "itemId": "P603", "action": "view" },
  { "userId": "U1001", "itemId": "P602", "action": "add_to_cart" },
  { "userId": "U1001", "itemId": "P603", "action": "view" },
  { "userId": "U2002", "itemId": "P602", "action": "add_to_cart" },
  { "userId": "U2002", "itemId": "P604", "action": "add_to_cart" },
  { "userId": "U3003", "itemId": "P601", "action": "view" },
  { "userId": "U3003", "itemId": "P605", "action": "add_to_cart" },
  { "userId": "U3003", "itemId": "P606", "action": "view" }
]
```

**Response:**
```json
{
  "message": "Activities ingested successfully",
  "count": 8,
  "status": "success"
}
```
✅ **Status:** Working

---

### 3. Get Recommendations Endpoint

#### Test 3a: Recommendations for User U1001
**Request:**
```bash
GET http://localhost:8080/recommend?userId=U1001&k=5
```

**Response:**
```json
{
  "userId": "U1001",
  "recommendations": [
    { "itemId": "P602", "score": 0.1 },
    { "itemId": "P603", "score": 0.02 }
  ],
  "generatedAt": "2025-12-02T21:17:29.914004-08:00[America/Los_Angeles]"
}
```

**Analysis:**
- U1001 has: P602 (1 add_to_cart), P603 (2 views)
- Result: P602 first (add_to_cart priority), then P603 (views)
✅ **Status:** Working correctly with priority-based scoring

---

#### Test 3b: Recommendations for User U2002
**Request:**
```bash
GET http://localhost:8080/recommend?userId=U2002&k=3
```

**Response:**
```json
{
  "userId": "U2002",
  "recommendations": [
    { "itemId": "P602", "score": 0.1 },
    { "itemId": "P604", "score": 0.1 }
  ],
  "generatedAt": "2025-12-02T21:17:29.945406-08:00[America/Los_Angeles]"
}
```

**Analysis:**
- U2002 has: P602 (1 add_to_cart), P604 (1 add_to_cart)
- Result: Both items with equal scores (both have add_to_cart)
✅ **Status:** Working correctly

---

#### Test 3c: Recommendations for User U3003
**Request:**
```bash
GET http://localhost:8080/recommend?userId=U3003&k=3
```

**Response:**
```json
{
  "userId": "U3003",
  "recommendations": [
    { "itemId": "P605", "score": 0.1 },
    { "itemId": "P601", "score": 0.01 },
    { "itemId": "P606", "score": 0.01 }
  ],
  "generatedAt": "2025-12-02T21:17:29.969590-08:00[America/Los_Angeles]"
}
```

**Analysis:**
- U3003 has: P605 (1 add_to_cart), P601 (1 view), P606 (1 view)
- Result: P605 first (add_to_cart priority), then views
✅ **Status:** Working correctly

---

## Recommendation Algorithm Summary

The API successfully implements the priority-based recommendation system:

1. **Priority 1:** Items with `add_to_cart` action (highest weight)
2. **Priority 2:** Items with `view` action (based on frequency)
3. **Scoring:** 
   - add_to_cart = 10 points
   - view = 1 point
   - Normalized to 0-1 range

## How to Run Tests

### Start the application:
```bash
mvn spring-boot:run
```

### Run the test script:
```bash
./test-api.sh
```

### Or test manually:
```bash
# Health check
curl http://localhost:8080/health

# Ingest data
curl -X POST http://localhost:8080/ingest \
  -H "Content-Type: application/json" \
  -d @src/main/resources/interactions.json

# Get recommendations
curl "http://localhost:8080/recommend?userId=U1001&k=3"
```

---

## ✅ Conclusion

All API endpoints are working as expected with personalized recommendations based on user's own interaction history!
