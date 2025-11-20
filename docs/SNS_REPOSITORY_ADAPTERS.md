# SNS Repository Adapters - Database Service Integration Guide

## 📋 Overview

This document provides comprehensive documentation for all 17 SNS Repository Adapters that integrate with the Database Service, enabling the SNS API to work with multiple databases (PostgreSQL, MongoDB) through a unified interface.

**Last Updated:** 2025-11-14
**Version:** 1.0.0

---

## 🎯 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     SNS API Layer                            │
│  (Controllers → Services → Repository Ports)                 │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ↓
┌─────────────────────────────────────────────────────────────┐
│              Repository Adapters (@Primary)                  │
│  17 Adapters implementing Repository Ports                  │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ↓
┌─────────────────────────────────────────────────────────────┐
│              DatabaseServiceClient                           │
│  Unified REST API client for all databases                  │
└─────────────────────┬───────────────────────────────────────┘
                      │
          ┌───────────┴───────────┐
          ↓                       ↓
┌──────────────────┐    ┌──────────────────┐
│   PostgreSQL     │    │     MongoDB      │
│  (Structured)    │    │   (Documents)    │
└──────────────────┘    └──────────────────┘
```

---

## 📊 Repository Adapters Summary

### PostgreSQL Repositories (7 adapters)

| Adapter | Collection/Table | Purpose |
|---------|------------------|---------|
| `SnsUserRepositoryAdapter` | `sns_users` | User profiles and authentication |
| `FollowRepositoryAdapter` | `sns_follows` | User follow relationships |
| `ConversationRepositoryAdapter` | `sns_conversations` | 1:1 chat conversations |
| `InvestmentPortfolioRepositoryAdapter` | `sns_investment_portfolios` | Investment portfolios |
| `AssetHoldingRepositoryAdapter` | `sns_asset_holdings` | Asset positions |
| `TradeHistoryRepositoryAdapter` | `sns_trade_history` | Trade transactions |
| `PortfolioFollowerRepositoryAdapter` | `sns_portfolio_followers` | Portfolio follows |

### MongoDB Repositories (10 adapters)

| Adapter | Collection | Purpose |
|---------|------------|---------|
| `PostRepositoryAdapter` | `sns_posts` | Social media posts |
| `CommentRepositoryAdapter` | `sns_comments` | Post comments |
| `LikeRepositoryAdapter` | `sns_likes` | Post likes |
| `NotificationRepositoryAdapter` | `sns_notifications` | User notifications |
| `BookmarkRepositoryAdapter` | `sns_bookmarks` | Saved content |
| `MessageRepositoryAdapter` | `sns_messages` | Chat messages |
| `InvestmentPostRepositoryAdapter` | `sns_investment_posts` | Investment ideas |
| `WatchlistRepositoryAdapter` | `sns_watchlist` | Price watchlists |
| `StoryRepositoryAdapter` | `sns_stories` | 24-hour stories |
| `StoryViewRepositoryAdapter` | `sns_story_views` | Story views |

---

## 📁 File Organization

```
src/main/kotlin/yousang/rest_server/adapter/out/persistence/sns/
├── SnsUserRepositoryAdapter.kt
├── PostRepositoryAdapter.kt
├── SocialInteractionRepositoryAdapters.kt
│   ├── CommentRepositoryAdapter
│   ├── LikeRepositoryAdapter
│   └── FollowRepositoryAdapter
├── CommunicationRepositoryAdapters.kt
│   ├── NotificationRepositoryAdapter
│   ├── BookmarkRepositoryAdapter
│   ├── ConversationRepositoryAdapter
│   └── MessageRepositoryAdapter
├── InvestmentRepositoryAdapters.kt
│   ├── InvestmentPortfolioRepositoryAdapter
│   ├── AssetHoldingRepositoryAdapter
│   └── TradeHistoryRepositoryAdapter
└── AdditionalRepositoryAdapters.kt
    ├── InvestmentPostRepositoryAdapter
    ├── PortfolioFollowerRepositoryAdapter
    ├── WatchlistRepositoryAdapter
    ├── StoryRepositoryAdapter
    └── StoryViewRepositoryAdapter
```

---

## 🔍 Detailed Adapter Documentation

### 1. SnsUserRepositoryAdapter (PostgreSQL)

**File:** `SnsUserRepositoryAdapter.kt`
**Database:** PostgreSQL
**Collection:** `sns_users`

#### Purpose
Manages user accounts, profiles, and authentication-related data.

#### Key Operations

```kotlin
@Component
@Primary
class SnsUserRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient,
    private val objectMapper: ObjectMapper
) : SnsUserRepositoryPort {

    // Create or update user
    override fun save(user: SnsUser): SnsUser

    // Find by various criteria
    override fun findById(userId: Long): SnsUser?
    override fun findByEmail(email: String): SnsUser?
    override fun findByUsername(username: String): SnsUser?

    // Search users
    override fun search(query: String, limit: Int, offset: Int): List<SnsUser>

    // Validation
    override fun existsByEmail(email: String): Boolean
    override fun existsByUsername(username: String): Boolean
}
```

#### Database Service Calls

**Save Operation:**
```kotlin
val response = databaseServiceClient.upsert(
    collection = "sns_users",
    filter = mapOf("userId" to user.userId),
    document = userDocument,
    databaseType = DatabaseServiceClient.DB_POSTGRES
)
```

**Search Operation:**
```kotlin
val response = databaseServiceClient.search<Map<String, Any>>(
    collection = "sns_users",
    searchQuery = query,
    fields = listOf("username", "fullName"),
    limit = limit,
    databaseType = DatabaseServiceClient.DB_POSTGRES
)
```

#### Document Structure
```json
{
  "userId": 1,
  "email": "user@example.com",
  "username": "john_doe",
  "fullName": "John Doe",
  "bio": "Software developer",
  "profileImageUrl": "https://...",
  "followerCount": 150,
  "followingCount": 200,
  "postCount": 45,
  "isVerified": false,
  "isActive": true,
  "emailVerified": true,
  "createdAt": "2025-11-14T10:00:00",
  "updatedAt": "2025-11-14T10:00:00"
}
```

---

### 2. PostRepositoryAdapter (MongoDB)

**File:** `PostRepositoryAdapter.kt`
**Database:** MongoDB
**Collection:** `sns_posts`

#### Purpose
Manages social media posts with images, hashtags, and engagement metrics.

#### Key Operations

```kotlin
@Component
@Primary
class PostRepositoryAdapter : PostRepositoryPort {

    // CRUD operations
    override fun save(post: Post): Post
    override fun findById(postId: Long): Post?
    override fun delete(postId: Long): Boolean  // Soft delete

    // Query operations
    override fun findByUserId(userId: Long, limit: Int, offset: Int): List<Post>
    override fun findByHashtag(hashtag: String, limit: Int, offset: Int): List<Post>
    override fun findFeed(userId: Long, followingIds: List<Long>, limit: Int, offset: Int): List<Post>

    // Search
    override fun search(query: String, limit: Int, offset: Int): List<Post>
}
```

#### Feed Generation Logic

```kotlin
override fun findFeed(
    userId: Long,
    followingIds: List<Long>,
    limit: Int,
    offset: Int
): List<Post> {
    val userIds = followingIds + userId  // Include own posts

    val response = databaseServiceClient.find<Map<String, Any>>(
        collection = "sns_posts",
        filter = mapOf(
            "userId" to mapOf("\$in" to userIds),
            "isHidden" to false
        ),
        sort = mapOf("createdAt" to -1),
        limit = limit,
        offset = offset,
        databaseType = DatabaseServiceClient.DB_MONGODB
    )

    return response.data.map { documentToPost(it) }
}
```

#### Document Structure
```json
{
  "postId": 123,
  "userId": 1,
  "caption": "Beautiful sunset! 🌅 #nature #sunset",
  "imageUrls": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ],
  "location": "Santa Monica Beach",
  "hashtags": ["nature", "sunset"],
  "likeCount": 234,
  "commentCount": 45,
  "bookmarkCount": 12,
  "viewCount": 1523,
  "isHidden": false,
  "createdAt": "2025-11-14T10:30:00",
  "updatedAt": "2025-11-14T10:30:00"
}
```

---

### 3. InvestmentPortfolioRepositoryAdapter (PostgreSQL)

**File:** `InvestmentRepositoryAdapters.kt`
**Database:** PostgreSQL
**Collection:** `sns_investment_portfolios`

#### Purpose
Manages investment portfolios with performance metrics and analytics.

#### Key Operations

```kotlin
@Component
@Primary
class InvestmentPortfolioRepositoryAdapter : InvestmentPortfolioRepositoryPort {

    override fun save(portfolio: InvestmentPortfolio): InvestmentPortfolio
    override fun findById(portfolioId: Long): InvestmentPortfolio?
    override fun findByUserId(userId: Long): List<InvestmentPortfolio>
    override fun findPublicPortfolios(limit: Int, offset: Int): List<InvestmentPortfolio>
    override fun search(query: String, limit: Int, offset: Int): List<InvestmentPortfolio>
}
```

#### Public Portfolios Query

```kotlin
val response = databaseServiceClient.find<Map<String, Any>>(
    collection = "sns_investment_portfolios",
    filter = mapOf("isPublic" to true),
    sort = mapOf("followerCount" to -1, "createdAt" to -1),
    limit = limit,
    offset = offset,
    databaseType = DatabaseServiceClient.DB_POSTGRES
)
```

#### Document Structure
```json
{
  "portfolioId": 789,
  "userId": 1,
  "name": "Tech Growth Portfolio",
  "description": "Long-term tech investments",
  "isPublic": true,
  "totalValue": "50000.00",
  "totalCost": "45000.00",
  "totalReturn": "5000.00",
  "returnRate": "11.11",
  "followerCount": 25,
  "createdAt": "2025-11-14T10:00:00",
  "updatedAt": "2025-11-14T14:30:00"
}
```

---

### 4. CommentRepositoryAdapter (MongoDB)

**File:** `SocialInteractionRepositoryAdapters.kt`
**Database:** MongoDB
**Collection:** `sns_comments`

#### Purpose
Manages comments with nested reply support.

#### Nested Comments Support

```kotlin
// Top-level comments (parentCommentId = null)
override fun findByPostId(postId: Long, limit: Int, offset: Int): List<Comment> {
    val response = databaseServiceClient.find<Map<String, Any>>(
        collection = "sns_comments",
        filter = mapOf("postId" to postId, "parentCommentId" to null),
        sort = mapOf("createdAt" to -1),
        limit = limit,
        offset = offset,
        databaseType = DatabaseServiceClient.DB_MONGODB
    )
    return response.data.map { documentToComment(it) }
}

// Replies to a comment
override fun findReplies(parentCommentId: Long): List<Comment> {
    val response = databaseServiceClient.find<Map<String, Any>>(
        collection = "sns_comments",
        filter = mapOf("parentCommentId" to parentCommentId),
        sort = mapOf("createdAt" to 1),  // Oldest first for replies
        limit = 100,
        databaseType = DatabaseServiceClient.DB_MONGODB
    )
    return response.data.map { documentToComment(it) }
}
```

---

### 5. WatchlistRepositoryAdapter (MongoDB)

**File:** `AdditionalRepositoryAdapters.kt`
**Database:** MongoDB
**Collection:** `sns_watchlist`

#### Purpose
Manages asset watchlists with price alert conditions.

#### Complex Document Structure

```kotlin
override fun save(watchlistItem: WatchlistItem): WatchlistItem {
    val document = mapOf(
        "watchlistId" to watchlistItem.watchlistId,
        "userId" to watchlistItem.userId,
        "assetType" to watchlistItem.assetType.name,
        "symbol" to watchlistItem.symbol,
        "alertConditions" to watchlistItem.alertConditions.map {
            mapOf(
                "id" to it.id,
                "type" to it.type.name,  // ABOVE, BELOW, CHANGE_PERCENT
                "value" to it.value.toString()
            )
        },
        "isActive" to watchlistItem.isActive,
        "createdAt" to watchlistItem.createdAt.toString(),
        "updatedAt" to watchlistItem.updatedAt.toString()
    )

    val response = databaseServiceClient.upsert(/*...*/)
    return documentToWatchlistItem(response.data)
}
```

#### Document Example
```json
{
  "watchlistId": 456,
  "userId": 1,
  "assetType": "STOCK",
  "symbol": "AAPL",
  "alertConditions": [
    {
      "id": "alert-1",
      "type": "ABOVE",
      "value": "180.00"
    },
    {
      "id": "alert-2",
      "type": "BELOW",
      "value": "170.00"
    }
  ],
  "isActive": true,
  "createdAt": "2025-11-14T10:00:00",
  "updatedAt": "2025-11-14T10:00:00"
}
```

---

## 🔄 Common Patterns

### 1. Upsert Pattern (Insert or Update)

```kotlin
val response = if (entity.id == 0L) {
    // New entity - create
    databaseServiceClient.create(COLLECTION, document, DB_TYPE)
} else {
    // Existing entity - upsert
    databaseServiceClient.upsert(
        collection = COLLECTION,
        filter = mapOf("entityId" to entity.id),
        document = document,
        databaseType = DB_TYPE
    )
}
```

### 2. Soft Delete Pattern

```kotlin
override fun delete(postId: Long): Boolean {
    val response = databaseServiceClient.update<Map<String, Any>>(
        collection = "sns_posts",
        id = postId.toString(),
        updates = mapOf(
            "isHidden" to true,
            "updatedAt" to LocalDateTime.now().toString()
        ),
        databaseType = DB_TYPE
    )
    return response.success
}
```

### 3. Search Pattern

```kotlin
override fun search(query: String, limit: Int, offset: Int): List<Entity> {
    val response = databaseServiceClient.search<Map<String, Any>>(
        collection = COLLECTION,
        searchQuery = query,
        fields = listOf("field1", "field2"),
        limit = limit,
        databaseType = DB_TYPE
    )
    return response.data.map { documentToEntity(it) }
}
```

### 4. Complex Query Pattern

```kotlin
// MongoDB $in operator
val response = databaseServiceClient.find<Map<String, Any>>(
    collection = COLLECTION,
    filter = mapOf(
        "userId" to mapOf("\$in" to userIds),
        "status" to "active"
    ),
    sort = mapOf("createdAt" to -1),
    limit = limit,
    offset = offset,
    databaseType = DB_TYPE
)
```

### 5. Aggregation Pattern

```kotlin
// Count documents
val response = databaseServiceClient.count(
    collection = COLLECTION,
    filter = mapOf("userId" to userId, "isRead" to false),
    databaseType = DB_TYPE
)
val unreadCount = (response.data as? Number)?.toLong() ?: 0L
```

---

## 🎨 Data Type Conversion

### BigDecimal Handling

```kotlin
// To Document
"price" to holding.price.toString()

// From Document
price = BigDecimal(doc["price"].toString())
```

### LocalDateTime Handling

```kotlin
// To Document
"createdAt" to entity.createdAt.toString()

// From Document
createdAt = LocalDateTime.parse(doc["createdAt"] as String)
```

### Enum Handling

```kotlin
// To Document
"assetType" to holding.assetType.name

// From Document
assetType = AssetType.valueOf(doc["assetType"] as String)
```

### List/Array Handling

```kotlin
// To Document
"imageUrls" to post.imageUrls

// From Document
imageUrls = (doc["imageUrls"] as? List<*>)?.map { it.toString() } ?: emptyList()
```

---

## 🧪 Testing Guide

### Unit Test Example

```kotlin
@Test
fun `should save and retrieve user`() {
    // Given
    val user = SnsUser(
        userId = 0,
        email = "test@example.com",
        username = "testuser",
        fullName = "Test User"
    )

    // When
    val saved = userRepository.save(user)
    val retrieved = userRepository.findById(saved.userId)

    // Then
    assertThat(retrieved).isNotNull
    assertThat(retrieved?.email).isEqualTo("test@example.com")
}
```

### Integration Test Example

```kotlin
@SpringBootTest
@Testcontainers
class PostRepositoryIntegrationTest {

    @Autowired
    lateinit var postRepository: PostRepositoryPort

    @Test
    fun `should find feed posts for user`() {
        // Given
        val userId = 1L
        val followingIds = listOf(2L, 3L, 4L)

        // When
        val feed = postRepository.findFeed(userId, followingIds, 20, 0)

        // Then
        assertThat(feed).isNotEmpty
        assertThat(feed).allMatch {
            it.userId in (followingIds + userId)
        }
    }
}
```

---

## 🔧 Configuration

### Application Properties

```yaml
# Database Service Configuration
database-service:
  url: ${DATABASE_SERVICE_URL:http://localhost:8080}
  enabled: ${DATABASE_SERVICE_ENABLED:true}
  timeout: ${DATABASE_SERVICE_TIMEOUT:30000}
```

### Bean Configuration

```kotlin
@Configuration
class RepositoryConfiguration {

    @Bean
    fun databaseServiceClient(): DatabaseServiceClient {
        return DatabaseServiceClient(
            baseUrl = databaseServiceUrl,
            restTemplate = restTemplate(),
            objectMapper = objectMapper()
        )
    }
}
```

---

## 📊 Performance Considerations

### 1. Indexing Strategy

**PostgreSQL:**
```sql
-- Create indexes for frequently queried fields
CREATE INDEX idx_users_email ON sns_users(email);
CREATE INDEX idx_users_username ON sns_users(username);
CREATE INDEX idx_follows_follower ON sns_follows(follower_id);
CREATE INDEX idx_follows_following ON sns_follows(following_id);
```

**MongoDB:**
```javascript
// Create indexes via Database Service
db.sns_posts.createIndex({ userId: 1, createdAt: -1 });
db.sns_posts.createIndex({ hashtags: 1 });
db.sns_comments.createIndex({ postId: 1, createdAt: -1 });
```

### 2. Bulk Operations

```kotlin
// Instead of saving one by one
posts.forEach { postRepository.save(it) }  // ❌ Slow

// Use bulk insert via Database Service
val documents = posts.map { postToDocument(it) }
databaseServiceClient.bulkInsert(
    collection = "sns_posts",
    documents = documents,
    databaseType = DatabaseServiceClient.DB_MONGODB
)  // ✅ Fast
```

### 3. Pagination

Always use limit and offset:
```kotlin
postRepository.findByUserId(userId, limit = 20, offset = 0)  // First page
postRepository.findByUserId(userId, limit = 20, offset = 20) // Second page
```

---

## 🐛 Troubleshooting

### Common Issues

#### Issue 1: Database Service Connection Error
```
Failed to connect to database-service: Connection refused
```

**Solution:**
```bash
# Check if Database Service is running
curl http://localhost:8080/health

# Start Database Service
cd /path/to/database-service
docker-compose up -d
```

#### Issue 2: Document Conversion Error
```
ClassCastException: Cannot cast Map to SnsUser
```

**Solution:**
Check document structure and ensure all fields are properly mapped:
```kotlin
private fun documentToUser(doc: Map<*, *>): SnsUser {
    return SnsUser(
        userId = (doc["userId"] as Number).toLong(),  // Safe casting
        email = doc["email"] as String,
        // ... rest of fields
    )
}
```

#### Issue 3: Duplicate Key Error
```
409 Conflict: Unique constraint violation
```

**Solution:**
Use upsert instead of create for updates:
```kotlin
val response = databaseServiceClient.upsert(
    collection = COLLECTION,
    filter = mapOf("userId" to user.userId),
    document = document,
    databaseType = DB_TYPE
)
```

---

## 📚 Additional Resources

- [Database Service Documentation](https://github.com/YouSangSon/database-service)
- [Database Service REST API Spec](https://github.com/YouSangSon/database-service/blob/main/docs/REST_API_SPECIFICATION.md)
- [SNS API Documentation](./SNS_API_DOCUMENTATION.md)
- [Complete Architecture Guide](./COMPLETE_ARCHITECTURE.md)

---

**Last Updated:** 2025-11-14
**Version:** 1.0.0
**Total Adapters:** 17
