# SNS API Documentation

## 📋 Overview

This is a comprehensive **Investment-Focused Social Media API** built with Kotlin + Spring Boot, supporting traditional social networking features combined with investment portfolio management.

**Technology Stack:**
- **Backend:** Kotlin 1.9+, Spring Boot 3.2
- **Database:** Database Service (MongoDB, PostgreSQL)
- **Authentication:** JWT (JSON Web Token)
- **API Style:** RESTful API

**Base URL:** `/api/v1/sns`

---

## 🔐 Authentication

All endpoints require JWT authentication via the Authorization header:

```
Authorization: Bearer <JWT_TOKEN>
```

**Token Lifespan:** 1 hour (Access Token)
**Refresh Token Lifespan:** 7 days

---

## 📌 API Endpoints Summary

### Authentication (6 endpoints)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | User registration |
| POST | `/auth/login` | User login |
| POST | `/auth/refresh` | Refresh JWT token |
| GET | `/auth/profile` | Get current user profile |
| PUT | `/auth/profile` | Update user profile |
| POST | `/auth/logout` | Logout |

### Users (7 endpoints)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users/search` | Search users |
| GET | `/users/{userId}` | Get user profile |
| GET | `/users/{userId}/followers` | Get followers list |
| GET | `/users/{userId}/following` | Get following list |
| POST | `/users/{userId}/follow` | Follow user |
| DELETE | `/users/{userId}/follow` | Unfollow user |

### Posts (11 endpoints)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/posts` | Get feed posts |
| GET | `/posts/{postId}` | Get post details |
| POST | `/posts` | Create post |
| PUT | `/posts/{postId}` | Update post |
| DELETE | `/posts/{postId}` | Delete post |
| POST | `/posts/{postId}/like` | Like post |
| DELETE | `/posts/{postId}/like` | Unlike post |
| GET | `/posts/{postId}/comments` | Get comments |
| POST | `/posts/{postId}/comments` | Add comment |
| PUT | `/comments/{commentId}` | Update comment |
| DELETE | `/comments/{commentId}` | Delete comment |

### Investment Portfolios (13 endpoints)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/portfolios` | Get user portfolios |
| GET | `/portfolios/public` | Get public portfolios |
| GET | `/portfolios/{id}` | Get portfolio details |
| POST | `/portfolios` | Create portfolio |
| PUT | `/portfolios/{id}` | Update portfolio |
| DELETE | `/portfolios/{id}` | Delete portfolio |
| POST | `/portfolios/{id}/holdings` | Add holding |
| GET | `/portfolios/{id}/holdings` | Get holdings |
| PUT | `/portfolios/{id}/holdings/{holdingId}` | Update holding |
| DELETE | `/portfolios/{id}/holdings/{holdingId}` | Delete holding |
| GET | `/portfolios/{id}/analytics` | Get analytics |
| GET | `/portfolios/{id}/trades` | Get trade history |
| POST | `/portfolios/{id}/trades` | Record trade |

---

## 📖 Detailed API Reference

### 1. Authentication APIs

#### POST `/api/v1/sns/auth/register`
Register a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "username": "john_doe",
  "fullName": "John Doe"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "userId": 1,
    "email": "user@example.com",
    "username": "john_doe",
    "fullName": "John Doe",
    "bio": null,
    "profileImageUrl": null,
    "followerCount": 0,
    "followingCount": 0,
    "postCount": 0,
    "isVerified": false
  }
}
```

#### POST `/api/v1/sns/auth/login`
User login.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": { /* user object */ }
}
```

#### POST `/api/v1/sns/auth/refresh`
Refresh expired JWT token.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",  // New access token
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",  // New refresh token
  "user": { /* user object */ }
}
```

#### GET `/api/v1/sns/auth/profile`
Get current authenticated user profile.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "userId": 1,
  "email": "user@example.com",
  "username": "john_doe",
  "fullName": "John Doe",
  "bio": "Software developer and investor",
  "profileImageUrl": "https://example.com/profile.jpg",
  "followerCount": 150,
  "followingCount": 200,
  "postCount": 45,
  "isVerified": false
}
```

#### PUT `/api/v1/sns/auth/profile`
Update user profile.

**Request Body:**
```json
{
  "fullName": "John Michael Doe",
  "bio": "Updated bio text",
  "profileImageUrl": "https://example.com/new-profile.jpg"
}
```

**Response (200 OK):**
```json
{
  "userId": 1,
  "fullName": "John Michael Doe",
  "bio": "Updated bio text",
  // ... other user fields
}
```

---

### 2. User APIs

#### GET `/api/v1/sns/users/search?q={query}&limit=20&offset=0`
Search for users.

**Query Parameters:**
- `q` (required): Search query string
- `limit` (optional, default: 20): Results per page
- `offset` (optional, default: 0): Pagination offset

**Response (200 OK):**
```json
{
  "data": [
    {
      "userId": 1,
      "username": "john_doe",
      "fullName": "John Doe",
      "profileImageUrl": "https://example.com/profile.jpg",
      "followerCount": 150,
      "isVerified": false
    }
  ],
  "meta": {
    "limit": 20,
    "offset": 0,
    "hasMore": true
  }
}
```

#### POST `/api/v1/sns/users/{userId}/follow`
Follow a user.

**Response (200 OK):**
```json
{
  "message": "User followed successfully"
}
```

#### DELETE `/api/v1/sns/users/{userId}/follow`
Unfollow a user.

**Response (200 OK):**
```json
{
  "message": "User unfollowed successfully"
}
```

---

### 3. Post APIs

#### GET `/api/v1/sns/posts?limit=20&offset=0`
Get feed posts (from following users).

**Response (200 OK):**
```json
{
  "data": [
    {
      "postId": 1,
      "userId": 5,
      "caption": "Beautiful sunset! 🌅 #nature #sunset",
      "imageUrls": [
        "https://example.com/image1.jpg",
        "https://example.com/image2.jpg"
      ],
      "location": "Santa Monica Beach, CA",
      "hashtags": ["nature", "sunset"],
      "likeCount": 234,
      "commentCount": 45,
      "viewCount": 1523,
      "createdAt": "2025-11-13T10:30:00"
    }
  ],
  "meta": {
    "limit": 20,
    "offset": 0,
    "hasMore": true
  }
}
```

#### POST `/api/v1/sns/posts`
Create a new post.

**Request Body:**
```json
{
  "caption": "Check out my new post! #amazing",
  "imageUrls": [
    "https://example.com/image1.jpg"
  ],
  "location": "New York, NY"
}
```

**Response (201 Created):**
```json
{
  "postId": 123,
  "userId": 1,
  "caption": "Check out my new post! #amazing",
  "imageUrls": ["https://example.com/image1.jpg"],
  "location": "New York, NY",
  "hashtags": ["amazing"],
  "likeCount": 0,
  "commentCount": 0,
  "viewCount": 0,
  "createdAt": "2025-11-13T14:20:00"
}
```

#### POST `/api/v1/sns/posts/{postId}/like`
Like a post.

**Response (200 OK):**
```json
{
  "message": "Post liked"
}
```

#### POST `/api/v1/sns/posts/{postId}/comments`
Add a comment to a post.

**Request Body:**
```json
{
  "content": "Great post! Love it 😍",
  "parentCommentId": null  // Set to comment ID for replies
}
```

**Response (201 Created):**
```json
{
  "commentId": 456,
  "postId": 123,
  "userId": 1,
  "parentCommentId": null,
  "content": "Great post! Love it 😍",
  "likeCount": 0,
  "createdAt": "2025-11-13T14:25:00"
}
```

---

### 4. Investment Portfolio APIs

#### POST `/api/v1/sns/portfolios`
Create a new investment portfolio.

**Request Body:**
```json
{
  "name": "Tech Growth Portfolio",
  "description": "Long-term growth portfolio focused on technology stocks",
  "isPublic": true
}
```

**Response (201 Created):**
```json
{
  "portfolioId": 789,
  "userId": 1,
  "name": "Tech Growth Portfolio",
  "description": "Long-term growth portfolio focused on technology stocks",
  "isPublic": true,
  "totalValue": 0.00,
  "totalReturn": 0.00,
  "returnRate": 0.00,
  "followerCount": 0
}
```

#### POST `/api/v1/sns/portfolios/{portfolioId}/holdings`
Add an asset holding to a portfolio.

**Request Body:**
```json
{
  "assetType": "STOCK",
  "symbol": "AAPL",
  "quantity": 10.5,
  "averagePrice": 170.50,
  "currentPrice": 175.43
}
```

**Response (201 Created):**
```json
{
  "holdingId": 1001,
  "portfolioId": 789,
  "assetType": "STOCK",
  "symbol": "AAPL",
  "quantity": 10.5,
  "averagePrice": 170.50,
  "currentPrice": 175.43,
  "totalValue": 1842.01,
  "unrealizedGain": 51.76,
  "returnRate": 2.89
}
```

#### GET `/api/v1/sns/portfolios/{portfolioId}/analytics`
Get portfolio analytics and metrics.

**Response (200 OK):**
```json
{
  "portfolioId": 789,
  "totalValue": 50000.00,
  "totalCost": 45000.00,
  "totalReturn": 5000.00,
  "returnRate": 11.11,
  "holdingsCount": 15,
  "assetAllocation": {
    "STOCK": 65.50,
    "CRYPTO": 20.00,
    "ETF": 14.50
  }
}
```

#### POST `/api/v1/sns/portfolios/{portfolioId}/trades`
Record a trade transaction.

**Request Body:**
```json
{
  "assetType": "CRYPTO",
  "symbol": "BTCUSDT",
  "tradeType": "BUY",
  "quantity": 0.5,
  "price": 43500.00,
  "fee": 21.75,
  "tradeDate": "2025-11-13T10:00:00"
}
```

**Response (201 Created):**
```json
{
  "tradeId": 2001,
  "portfolioId": 789,
  "assetType": "CRYPTO",
  "symbol": "BTCUSDT",
  "tradeType": "BUY",
  "quantity": 0.5,
  "price": 43500.00,
  "fee": 21.75,
  "tradeDate": "2025-11-13T10:00:00"
}
```

---

## 🚀 Getting Started

### 1. Setup

```bash
# Clone the repository
cd /home/user/rest_server

# Configure environment variables
cp .env.example .env
nano .env

# Set required variables:
JWT_SECRET=your-secure-secret-key
DATABASE_SERVICE_URL=http://localhost:8080
DATABASE_SERVICE_ENABLED=true
```

### 2. Run the Application

```bash
# Using Gradle
./gradlew bootRun

# Or using Docker Compose
docker-compose up -d
```

### 3. Test the API

```bash
# Register a new user
curl -X POST http://localhost:8080/api/v1/sns/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "username": "testuser",
    "fullName": "Test User"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/sns/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }'

# Create a post (use token from login response)
curl -X POST http://localhost:8080/api/v1/sns/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "caption": "My first post!",
    "imageUrls": ["https://example.com/image.jpg"],
    "location": null
  }'
```

---

## 📊 Domain Models

### Core Entities

1. **SnsUser** - User account and profile
2. **Post** - Social media post with images
3. **Comment** - Comments and replies
4. **Like** - Post likes
5. **Follow** - User follow relationships
6. **InvestmentPortfolio** - Investment portfolio
7. **AssetHolding** - Portfolio holdings
8. **TradeHistory** - Trade transactions
9. **InvestmentPost** - Investment ideas/analysis
10. **Bookmark** - Saved posts
11. **WatchlistItem** - Asset watchlist with price alerts
12. **Notification** - User notifications
13. **Conversation** - Direct messages (1:1)
14. **Message** - Chat messages
15. **Story** - 24-hour stories

---

## 🔒 Security

### Authentication Flow

1. **Register/Login** → Receive JWT token + Refresh token
2. **API Requests** → Include `Authorization: Bearer <token>` header
3. **Token Expires** → Use refresh token to get new access token
4. **Logout** → Client removes tokens

### Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character

### Rate Limiting

- **General endpoints:** 100 requests/minute
- **Search endpoints:** 30 requests/minute
- **Upload endpoints:** 10 requests/minute

---

## 📝 Error Responses

All errors follow a consistent format:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {
      "field": "email",
      "reason": "Email already exists"
    }
  }
}
```

### Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `INVALID_INPUT` | 400 | Invalid request data |
| `UNAUTHORIZED` | 401 | Missing or invalid authentication |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `CONFLICT` | 409 | Resource conflict (e.g., duplicate) |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `INTERNAL_ERROR` | 500 | Server error |

---

## 🎯 Best Practices

### Pagination

Always use pagination for list endpoints:

```
GET /api/v1/sns/posts?limit=20&offset=0
GET /api/v1/sns/posts?limit=20&offset=20  // Next page
```

### Image Upload

1. Upload image to storage service first
2. Get image URL
3. Include URL in post creation request

### Real-time Updates

For real-time features, consider:
- **WebSocket** for price updates
- **Polling** (every 5-10s) for notifications
- **Server-Sent Events** for live updates

---

## 📚 Additional Resources

- **Original Project:** [https://github.com/YouSangSon/sns_project](https://github.com/YouSangSon/sns_project)
- **Database Service:** [https://github.com/YouSangSon/database-service](https://github.com/YouSangSon/database-service)
- **API Testing:** Use Postman or Swagger UI at `/swagger-ui.html`

---

**Last Updated:** 2025-11-13
**API Version:** 1.0.0
**Status:** ✅ Production Ready
