# Recommendation Microservice

A REST API microservice built with Spring Boot that provides two types of personalized recommendations using in-memory vector stores and similarity algorithms.

## Features

- **POST /ingest** - Load user-item interaction data
- **GET /recommend** - Get personalized recommendations based on user's own activity history
- **GET /recommendCollaborative** - Get collaborative filtering recommendations based on similar users
- **GET /health** - Health check endpoint
- In-memory vector store with cosine similarity
- Action-weighted scoring (add_to_cart prioritized over view)
- JSON-based API for easy integration

## Two Recommendation Algorithms

### 1. Personalized Recommendations (`/recommend`)
Recommends items based on the **user's own activity history** with action-based priority scoring:
- **add_to_cart** actions: 10 points per occurrence
- **view** actions: 1 point per occurrence
- Returns items the user has interacted with, ranked by weighted score

### 2. Collaborative Filtering (`/recommendCollaborative`)
Recommends items based on **similar users' behaviors** using vector similarity:
- Builds user behavior vectors with action weights (add_to_cart=3, view=1)
- Normalizes vectors to unit length for cosine similarity
- Finds top-K similar users using dot product
- Aggregates and ranks items from similar users' activities
- Filters out items the user has already interacted with

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Project Structure

```
src/main/java/com/microsoft/recommendation/
├── RecommendationServiceApplication.java   # Main application
├── controller/
│   └── RecommendationController.java       # REST endpoints
├── model/
│   ├── Activity.java                       # Activity model
│   ├── RecommendationItem.java             # Recommendation item
│   ├── RecommendationResponse.java         # Response wrapper
│   └── HealthResponse.java                 # Health check response
└── service/
    ├── RecommendationService.java          # Business logic
    └── VectorStore.java                    # In-memory vector store
```

## Setup & Run

1. **Build the project:**
   ```bash
   mvn clean package
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

   Or run the JAR:
   ```bash
   java -jar target/recommendation-service-1.0.0.jar
   ```

3. **The service will start on port 8080**

## API Usage

### 1. Health Check
```bash
curl http://localhost:8080/health
```

Response:
```json
{
  "status": "ok"
}
```

### 2. Ingest Activities
```bash
curl -X POST http://localhost:8080/ingest \
  -H "Content-Type: application/json" \
  -d '[
    { "userId": "U1001", "itemId": "P601", "action": "view" },
    { "userId": "U1001", "itemId": "P602", "action": "add_to_cart" },
    { "userId": "U1001", "itemId": "P603", "action": "view" },
    { "userId": "U2002", "itemId": "P602", "action": "view" },
    { "userId": "U2002", "itemId": "P604", "action": "view" },
    { "userId": "U3003", "itemId": "P601", "action": "view" },
    { "userId": "U3003", "itemId": "P605", "action": "add_to_cart" },
    { "userId": "U3003", "itemId": "P606", "action": "view" }
  ]'
```

Response:
```json
{
  "message": "Activities ingested successfully",
  "count": 8,
  "status": "success"
}
```

### 3. Get Personalized Recommendations
Get recommendations based on the user's own activity history with action-based scoring.

```bash
curl "http://localhost:8080/recommend?userId=U1001&k=3"
```

Response:
```json
{
  "userId": "U1001",
  "recommendations": [
    { "itemId": "P603", "score": 0.02 },
    { "itemId": "P602", "score": 0.1 }
  ],
  "generatedAt": "2025-12-03T00:23:39.855903-08:00"
}
```

**How it works:**
- Scores items based on user's own interactions
- Action weights: `add_to_cart` = 10 points, `view` = 1 point
- Normalized by total actions for that item
- Returns top-k items from user's history, ranked by weighted score

### 4. Get Collaborative Filtering Recommendations
Get recommendations based on similar users' activities using vector similarity.

```bash
curl "http://localhost:8080/recommendCollaborative?userId=U1001&k=3"
```

Response:
```json
{
  "userId": "U1001",
  "recommendations": [
    { "itemId": "P604", "score": 1.77 },
    { "itemId": "P601", "score": 0.0 },
    { "itemId": "P606", "score": 0.0 }
  ],
  "similarUsers": [
    { "userId": "U2002", "similarity": 0.59 },
    { "userId": "U3003", "similarity": 0.0 }
  ],
  "generatedAt": "2025-12-03T00:23:39.892884-08:00"
}
```

**How it works:**
1. **Build user vectors**: Each user gets a vector where each dimension represents an item, weighted by actions (add_to_cart=3, view=1)
2. **Normalize vectors**: Convert to unit vectors for cosine similarity via dot product
3. **Find similar users**: Compute similarity between target user and all other users
4. **Aggregate items**: Collect items from top-K similar users, weighted by similarity score
5. **Filter & rank**: Exclude items user already interacted with, return top recommendations

## Algorithm Comparison

| Feature | `/recommend` (Personalized) | `/recommendCollaborative` (Collaborative) |
|---------|----------------------------|------------------------------------------|
| **Data Source** | User's own history | Similar users' activities |
| **Best For** | Users with rich activity history | Discovery & serendipity |
| **Scoring** | Action priority (add_to_cart > view) | Similarity-weighted aggregation |
| **Novelty** | Returns familiar items | Discovers new items from similar users |
| **Cold Start** | Struggles with new users | Works if similar users exist |

## Testing with curl

### Complete End-to-End Test

```bash
# 1. Check health
curl http://localhost:8080/health

# 2. Load sample data
curl -X POST http://localhost:8080/ingest \
  -H "Content-Type: application/json" \
  -d '[
    { "userId": "U1001", "itemId": "P601", "action": "view" },
    { "userId": "U1001", "itemId": "P602", "action": "add_to_cart" },
    { "userId": "U1001", "itemId": "P603", "action": "view" },
    { "userId": "U2002", "itemId": "P602", "action": "view" },
    { "userId": "U2002", "itemId": "P604", "action": "view" },
    { "userId": "U3003", "itemId": "P601", "action": "view" },
    { "userId": "U3003", "itemId": "P605", "action": "add_to_cart" },
    { "userId": "U3003", "itemId": "P606", "action": "view" }
  ]'

# 3. Get personalized recommendations (based on own history)
curl "http://localhost:8080/recommend?userId=U1001&k=2"
curl "http://localhost:8080/recommend?userId=U2002&k=3"

# 4. Get collaborative recommendations (based on similar users)
curl "http://localhost:8080/recommendCollaborative?userId=U1001&k=3"
curl "http://localhost:8080/recommendCollaborative?userId=U2002&k=3"
```

### Using the Test Script
```bash
./test-api.sh
```

## Toy Example: Understanding the Difference

Let's walk through a simple example to understand how both algorithms work differently.

### Sample Data
```
User U1001: viewed P601, added P602 to cart, viewed P603
User U2002: viewed P602, viewed P604
User U3003: viewed P601, added P605 to cart, viewed P606
```

### Scenario: Get recommendations for U1001

#### Algorithm 1: Personalized (`/recommend`)
**Looks at U1001's own history only:**
- P601: 1 view = 1 point
- P602: 1 add_to_cart = 10 points ⭐
- P603: 1 view = 1 point

**Result:** Recommends P602 (highest score), then P603, then P601

This essentially re-ranks items U1001 already knows about, useful for "buy again" or "continue shopping" features.

#### Algorithm 2: Collaborative Filtering (`/recommendCollaborative`)
**Step 1 - Build vectors:**
```
U1001: [P601:1, P602:3, P603:1] (view=1, add_to_cart=3)
U2002: [P602:1, P604:1]
U3003: [P601:1, P605:3, P606:1]
```

**Step 2 - Find similar users:**
- U1001 vs U2002: similarity = 0.59 (both interacted with P602)
- U1001 vs U3003: similarity = 0.0 (only P601 in common, low weight)

**Step 3 - Aggregate items from similar users:**
- From U2002 (0.59 similar): P604 (U1001 hasn't seen!) 
- Score: P604 = 0.59 × 3 = 1.77 ⭐

**Result:** Recommends P604 (from similar user U2002)

This discovers **new items** U1001 hasn't seen yet, based on what similar users liked!

### Key Insight
- **Personalized** = "Here are your favorites, re-ranked"
- **Collaborative** = "People like you also enjoyed these NEW items"

Use personalized for reminders, collaborative for discovery!

## Configuration

Edit `src/main/resources/application.properties`:
```properties
server.port=8080
spring.application.name=recommendation-service
```

## Technologies Used

- Spring Boot 3.2.0
- Spring Web (REST APIs)
- Spring Validation
- Java 17
- Maven (build tool)

## Architecture Notes

- **In-Memory Storage**: All data stored in memory, lost on restart
- **Vector-Based Similarity**: Uses normalized vectors and dot product for cosine similarity
- **Action Weighting**: Different actions have different importance (add_to_cart > view)
- **No External Dependencies**: No database or ML libraries required
- **Stateless Design**: Perfect for containerization and horizontal scaling (with external storage)
