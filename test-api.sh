#!/bin/bash

echo "================================"
echo "Testing Recommendation API"
echo "================================"
echo ""

# Test 1: Health Check
echo "1. Testing /health endpoint..."
curl -X GET http://localhost:8080/health
echo -e "\n"

# Test 2: Ingest activities
echo "2. Testing POST /ingest endpoint..."
curl -X POST http://localhost:8080/ingest \
  -H "Content-Type: application/json" \
  -d '[
    { "userId": "U1001", "itemId": "P603", "action": "view" },
    { "userId": "U1001", "itemId": "P602", "action": "add_to_cart" },
    { "userId": "U1001", "itemId": "P603", "action": "view" },
    { "userId": "U2002", "itemId": "P602", "action": "add_to_cart" },
    { "userId": "U2002", "itemId": "P604", "action": "add_to_cart" },
    { "userId": "U3003", "itemId": "P601", "action": "view" },
    { "userId": "U3003", "itemId": "P605", "action": "add_to_cart" },
    { "userId": "U3003", "itemId": "P606", "action": "view" }
  ]'
echo -e "\n"

# Test 3: Get recommendations for U1001
echo "3. Testing GET /recommend for U1001 (k=5)..."
curl -X GET "http://localhost:8080/recommend?userId=U1001&k=5"
echo -e "\n"

# Test 4: Get recommendations for U2002
echo "4. Testing GET /recommend for U2002 (k=3)..."
curl -X GET "http://localhost:8080/recommend?userId=U2002&k=3"
echo -e "\n"

# Test 5: Get recommendations for U3003
echo "5. Testing GET /recommend for U3003 (k=3)..."
curl -X GET "http://localhost:8080/recommend?userId=U3003&k=3"
echo -e "\n"

# Test 6: Get collaborative recommendations for U1001
echo "6. Testing GET /recommendCollaborative for U1001 (k=3)..."
curl -X GET "http://localhost:8080/recommendCollaborative?userId=U1001&k=3"
echo -e "\n"

# Test 7: Get collaborative recommendations for U2002
echo "7. Testing GET /recommendCollaborative for U2002 (k=3)..."
curl -X GET "http://localhost:8080/recommendCollaborative?userId=U2002&k=3"
echo -e "\n"

# Test 8: Get collaborative recommendations for U3003
echo "8. Testing GET /recommendCollaborative for U3003 (k=3)..."
curl -X GET "http://localhost:8080/recommendCollaborative?userId=U3003&k=3"
echo -e "\n"

echo "================================"
echo "All tests completed!"
echo "================================"
