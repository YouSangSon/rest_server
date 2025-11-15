# Database Schema Documentation

Complete database schema documentation for all PostgreSQL tables and MongoDB collections used in the REST Server (Trading Bot + SNS API).

**Last Updated:** 2025-11-14
**Version:** 1.0.0

---

## 📋 Table of Contents

1. [PostgreSQL Schema](#postgresql-schema)
2. [MongoDB Schema](#mongodb-schema)
3. [Indexes](#indexes)
4. [Relationships](#relationships)
5. [Data Types](#data-types)

---

## 🐘 PostgreSQL Schema

### SNS Module Tables

#### 1. sns_users

**Purpose:** User accounts and profiles

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| user_id | BIGSERIAL | PRIMARY KEY | Auto-increment user ID |
| email | VARCHAR(255) | UNIQUE, NOT NULL | User email |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Username |
| full_name | VARCHAR(100) | NOT NULL | Full name |
| bio | TEXT | NULL | User bio |
| profile_image_url | VARCHAR(500) | NULL | Profile image URL |
| follower_count | INTEGER | DEFAULT 0 | Number of followers |
| following_count | INTEGER | DEFAULT 0 | Number following |
| post_count | INTEGER | DEFAULT 0 | Number of posts |
| is_verified | BOOLEAN | DEFAULT FALSE | Verified badge |
| is_active | BOOLEAN | DEFAULT TRUE | Account active |
| email_verified | BOOLEAN | DEFAULT FALSE | Email verified |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Update timestamp |

**Indexes:**
```sql
CREATE INDEX idx_users_email ON sns_users(email);
CREATE INDEX idx_users_username ON sns_users(username);
CREATE INDEX idx_users_created_at ON sns_users(created_at DESC);
```

**Sample Data:**
```sql
INSERT INTO sns_users (email, username, full_name, bio, created_at, updated_at)
VALUES (
  'john@example.com',
  'john_doe',
  'John Doe',
  'Software developer and investor',
  NOW(),
  NOW()
);
```

---

#### 2. sns_follows

**Purpose:** User follow relationships

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| follow_id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| follower_id | BIGINT | NOT NULL, FK → sns_users(user_id) | User who follows |
| following_id | BIGINT | NOT NULL, FK → sns_users(user_id) | User being followed |
| created_at | TIMESTAMP | NOT NULL | Follow timestamp |

**Constraints:**
```sql
UNIQUE(follower_id, following_id)
CHECK(follower_id != following_id)
```

**Indexes:**
```sql
CREATE INDEX idx_follows_follower ON sns_follows(follower_id);
CREATE INDEX idx_follows_following ON sns_follows(following_id);
CREATE INDEX idx_follows_created_at ON sns_follows(created_at DESC);
```

---

#### 3. sns_conversations

**Purpose:** 1:1 chat conversations

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| conversation_id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| participant_1_id | BIGINT | NOT NULL, FK → sns_users | First participant |
| participant_2_id | BIGINT | NOT NULL, FK → sns_users | Second participant |
| last_message_at | TIMESTAMP | NULL | Last message time |
| created_at | TIMESTAMP | NOT NULL | Creation time |

**Constraints:**
```sql
CHECK(participant_1_id != participant_2_id)
```

**Indexes:**
```sql
CREATE INDEX idx_conversations_p1 ON sns_conversations(participant_1_id);
CREATE INDEX idx_conversations_p2 ON sns_conversations(participant_2_id);
CREATE INDEX idx_conversations_last_message ON sns_conversations(last_message_at DESC);
```

---

#### 4. sns_investment_portfolios

**Purpose:** Investment portfolios

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| portfolio_id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| user_id | BIGINT | NOT NULL, FK → sns_users | Portfolio owner |
| name | VARCHAR(100) | NOT NULL | Portfolio name |
| description | TEXT | NULL | Description |
| is_public | BOOLEAN | DEFAULT FALSE | Public visibility |
| total_value | DECIMAL(18,2) | DEFAULT 0 | Total portfolio value |
| total_cost | DECIMAL(18,2) | DEFAULT 0 | Total cost basis |
| total_return | DECIMAL(18,2) | DEFAULT 0 | Total return amount |
| return_rate | DECIMAL(8,2) | DEFAULT 0 | Return rate (%) |
| follower_count | INTEGER | DEFAULT 0 | Number of followers |
| created_at | TIMESTAMP | NOT NULL | Creation time |
| updated_at | TIMESTAMP | NOT NULL | Update time |

**Indexes:**
```sql
CREATE INDEX idx_portfolios_user ON sns_investment_portfolios(user_id);
CREATE INDEX idx_portfolios_public ON sns_investment_portfolios(is_public, follower_count DESC);
CREATE INDEX idx_portfolios_created_at ON sns_investment_portfolios(created_at DESC);
```

---

#### 5. sns_asset_holdings

**Purpose:** Asset positions in portfolios

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| holding_id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| portfolio_id | BIGINT | NOT NULL, FK → sns_investment_portfolios | Parent portfolio |
| asset_type | VARCHAR(20) | NOT NULL | STOCK, CRYPTO, ETF, etc. |
| symbol | VARCHAR(20) | NOT NULL | Asset symbol (AAPL, BTC, etc.) |
| quantity | DECIMAL(18,8) | NOT NULL | Quantity held |
| average_price | DECIMAL(18,2) | NOT NULL | Average buy price |
| current_price | DECIMAL(18,2) | NOT NULL | Current market price |
| created_at | TIMESTAMP | NOT NULL | Creation time |
| updated_at | TIMESTAMP | NOT NULL | Update time |

**Indexes:**
```sql
CREATE INDEX idx_holdings_portfolio ON sns_asset_holdings(portfolio_id);
CREATE INDEX idx_holdings_symbol ON sns_asset_holdings(portfolio_id, symbol);
CREATE INDEX idx_holdings_asset_type ON sns_asset_holdings(asset_type);
```

---

#### 6. sns_trade_history

**Purpose:** Trade transaction records

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| trade_id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| portfolio_id | BIGINT | NOT NULL, FK → sns_investment_portfolios | Related portfolio |
| asset_type | VARCHAR(20) | NOT NULL | Asset type |
| symbol | VARCHAR(20) | NOT NULL | Asset symbol |
| trade_type | VARCHAR(10) | NOT NULL | BUY or SELL |
| quantity | DECIMAL(18,8) | NOT NULL | Trade quantity |
| price | DECIMAL(18,2) | NOT NULL | Trade price |
| fee | DECIMAL(18,2) | DEFAULT 0 | Transaction fee |
| trade_date | TIMESTAMP | NOT NULL | Trade execution time |
| created_at | TIMESTAMP | NOT NULL | Record creation time |

**Indexes:**
```sql
CREATE INDEX idx_trades_portfolio ON sns_trade_history(portfolio_id, trade_date DESC);
CREATE INDEX idx_trades_symbol ON sns_trade_history(portfolio_id, symbol);
CREATE INDEX idx_trades_date_range ON sns_trade_history(trade_date DESC);
```

---

#### 7. sns_portfolio_followers

**Purpose:** Portfolio follow relationships

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| portfolio_follower_id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| portfolio_id | BIGINT | NOT NULL, FK → sns_investment_portfolios | Portfolio being followed |
| user_id | BIGINT | NOT NULL, FK → sns_users | User following |
| created_at | TIMESTAMP | NOT NULL | Follow time |

**Constraints:**
```sql
UNIQUE(portfolio_id, user_id)
```

**Indexes:**
```sql
CREATE INDEX idx_portfolio_followers_portfolio ON sns_portfolio_followers(portfolio_id);
CREATE INDEX idx_portfolio_followers_user ON sns_portfolio_followers(user_id);
```

---

### Trading Bot Tables

#### 8. orders

**Purpose:** Trading orders

**Columns:**

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| order_id | VARCHAR(100) | UNIQUE, NOT NULL | External order ID |
| user_id | BIGINT | NOT NULL | User ID |
| symbol | VARCHAR(20) | NOT NULL | Trading pair |
| exchange | VARCHAR(50) | NOT NULL | Exchange name |
| type | VARCHAR(20) | NOT NULL | MARKET, LIMIT, etc. |
| side | VARCHAR(10) | NOT NULL | BUY or SELL |
| quantity | DECIMAL(18,8) | NOT NULL | Order quantity |
| price | DECIMAL(18,2) | NULL | Limit price |
| status | VARCHAR(20) | NOT NULL | Order status |
| filled_quantity | DECIMAL(18,8) | DEFAULT 0 | Filled amount |
| average_fill_price | DECIMAL(18,2) | NULL | Average fill price |
| fee | DECIMAL(18,2) | DEFAULT 0 | Trading fee |
| created_at | TIMESTAMP | NOT NULL | Creation time |
| updated_at | TIMESTAMP | NOT NULL | Update time |

**Indexes:**
```sql
CREATE INDEX idx_orders_user ON orders(user_id, created_at DESC);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_symbol ON orders(symbol);
```

---

## 🍃 MongoDB Schema

### SNS Module Collections

#### 1. sns_posts

**Purpose:** Social media posts

**Schema:**
```javascript
{
  _id: ObjectId,
  postId: Long,                    // Sequential ID
  userId: Long,                    // Reference to sns_users
  caption: String,                 // Post caption
  imageUrls: [String],            // Array of image URLs (max 10)
  location: String?,              // Optional location
  hashtags: [String],             // Extracted hashtags
  likeCount: Int,                 // Number of likes
  commentCount: Int,              // Number of comments
  bookmarkCount: Int,             // Number of bookmarks
  viewCount: Int,                 // Number of views
  isHidden: Boolean,              // Soft delete flag
  createdAt: ISODate,
  updatedAt: ISODate
}
```

**Indexes:**
```javascript
db.sns_posts.createIndex({ postId: 1 }, { unique: true });
db.sns_posts.createIndex({ userId: 1, createdAt: -1 });
db.sns_posts.createIndex({ hashtags: 1 });
db.sns_posts.createIndex({ isHidden: 1, createdAt: -1 });
db.sns_posts.createIndex({ "userId": 1, "isHidden": 1, "createdAt": -1 });
```

**Sample Document:**
```json
{
  "_id": ObjectId("..."),
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
  "createdAt": ISODate("2025-11-14T10:30:00Z"),
  "updatedAt": ISODate("2025-11-14T10:30:00Z")
}
```

---

#### 2. sns_comments

**Purpose:** Post comments with nested replies

**Schema:**
```javascript
{
  _id: ObjectId,
  commentId: Long,
  postId: Long,                   // Reference to post
  userId: Long,                   // Comment author
  parentCommentId: Long?,         // Null for top-level, ID for replies
  content: String,
  likeCount: Int,
  createdAt: ISODate,
  updatedAt: ISODate
}
```

**Indexes:**
```javascript
db.sns_comments.createIndex({ commentId: 1 }, { unique: true });
db.sns_comments.createIndex({ postId: 1, createdAt: -1 });
db.sns_comments.createIndex({ parentCommentId: 1 });
db.sns_comments.createIndex({ userId: 1 });
```

---

#### 3. sns_likes

**Purpose:** Post likes

**Schema:**
```javascript
{
  _id: ObjectId,
  likeId: Long,
  postId: Long,
  userId: Long,
  createdAt: ISODate
}
```

**Indexes:**
```javascript
db.sns_likes.createIndex({ likeId: 1 }, { unique: true });
db.sns_likes.createIndex({ postId: 1, userId: 1 }, { unique: true });
db.sns_likes.createIndex({ postId: 1, createdAt: -1 });
db.sns_likes.createIndex({ userId: 1 });
```

---

#### 4. sns_notifications

**Purpose:** User notifications

**Schema:**
```javascript
{
  _id: ObjectId,
  notificationId: Long,
  userId: Long,                   // Recipient
  notificationType: String,       // LIKE, COMMENT, FOLLOW, etc.
  sourceUserId: Long?,            // Who triggered
  relatedContentType: String?,    // post, comment, etc.
  relatedContentId: Long?,
  title: String,
  message: String,
  dataPayload: {                  // JSON metadata
    symbol: String?,
    price: Number?,
    // ... flexible structure
  },
  isRead: Boolean,
  createdAt: ISODate
}
```

**Indexes:**
```javascript
db.sns_notifications.createIndex({ notificationId: 1 }, { unique: true });
db.sns_notifications.createIndex({ userId: 1, createdAt: -1 });
db.sns_notifications.createIndex({ userId: 1, isRead: 1 });
db.sns_notifications.createIndex({ notificationType: 1 });
```

---

#### 5. sns_messages

**Purpose:** Chat messages

**Schema:**
```javascript
{
  _id: ObjectId,
  messageId: Long,
  conversationId: Long,           // Reference to PostgreSQL
  senderId: Long,
  content: String,
  imageUrl: String?,
  isRead: Boolean,
  createdAt: ISODate,
  updatedAt: ISODate
}
```

**Indexes:**
```javascript
db.sns_messages.createIndex({ messageId: 1 }, { unique: true });
db.sns_messages.createIndex({ conversationId: 1, createdAt: -1 });
db.sns_messages.createIndex({ conversationId: 1, isRead: 1 });
```

---

#### 6. sns_bookmarks

**Purpose:** Saved posts

**Schema:**
```javascript
{
  _id: ObjectId,
  bookmarkId: Long,
  userId: Long,
  contentType: String,            // POST, INVESTMENT_POST
  contentId: Long,
  createdAt: ISODate
}
```

**Indexes:**
```javascript
db.sns_bookmarks.createIndex({ bookmarkId: 1 }, { unique: true });
db.sns_bookmarks.createIndex({ userId: 1, createdAt: -1 });
db.sns_bookmarks.createIndex({ userId: 1, contentType: 1, contentId: 1 }, { unique: true });
```

---

#### 7. sns_investment_posts

**Purpose:** Investment ideas and analysis

**Schema:**
```javascript
{
  _id: ObjectId,
  investmentPostId: Long,
  userId: Long,
  portfolioId: Long?,
  postType: String,               // IDEA, PERFORMANCE, TRADE, ANALYSIS
  title: String,
  content: String,
  assetReferences: [              // Array of assets mentioned
    {
      symbol: String,
      assetType: String
    }
  ],
  likeCount: Int,
  commentCount: Int,
  bookmarkCount: Int,
  viewCount: Int,
  voteCount: Int,
  createdAt: ISODate,
  updatedAt: ISODate
}
```

**Indexes:**
```javascript
db.sns_investment_posts.createIndex({ investmentPostId: 1 }, { unique: true });
db.sns_investment_posts.createIndex({ userId: 1, createdAt: -1 });
db.sns_investment_posts.createIndex({ portfolioId: 1 });
db.sns_investment_posts.createIndex({ postType: 1, createdAt: -1 });
db.sns_investment_posts.createIndex({ "assetReferences.symbol": 1 });
```

---

#### 8. sns_watchlist

**Purpose:** Asset watchlist with price alerts

**Schema:**
```javascript
{
  _id: ObjectId,
  watchlistId: Long,
  userId: Long,
  assetType: String,
  symbol: String,
  alertConditions: [
    {
      id: String,
      type: String,                // ABOVE, BELOW, CHANGE_PERCENT
      value: Decimal128
    }
  ],
  isActive: Boolean,
  createdAt: ISODate,
  updatedAt: ISODate
}
```

**Indexes:**
```javascript
db.sns_watchlist.createIndex({ watchlistId: 1 }, { unique: true });
db.sns_watchlist.createIndex({ userId: 1 });
db.sns_watchlist.createIndex({ userId: 1, symbol: 1 });
db.sns_watchlist.createIndex({ isActive: 1 });
```

---

#### 9. sns_stories

**Purpose:** 24-hour temporary stories

**Schema:**
```javascript
{
  _id: ObjectId,
  storyId: Long,
  userId: Long,
  mediaUrl: String,
  mediaType: String,              // IMAGE, VIDEO
  caption: String?,
  viewCount: Int,
  expiresAt: ISODate,
  createdAt: ISODate
}
```

**Indexes:**
```javascript
db.sns_stories.createIndex({ storyId: 1 }, { unique: true });
db.sns_stories.createIndex({ userId: 1, createdAt: -1 });
db.sns_stories.createIndex({ expiresAt: 1 });  // For cleanup
db.sns_stories.createIndex({ userId: 1, expiresAt: 1 });
```

---

#### 10. sns_story_views

**Purpose:** Story view tracking

**Schema:**
```javascript
{
  _id: ObjectId,
  storyViewId: Long,
  storyId: Long,
  viewerId: Long,
  viewedAt: ISODate
}
```

**Indexes:**
```javascript
db.sns_story_views.createIndex({ storyViewId: 1 }, { unique: true });
db.sns_story_views.createIndex({ storyId: 1 });
db.sns_story_views.createIndex({ storyId: 1, viewerId: 1 }, { unique: true });
```

---

### Trading Bot Collections

#### 11. news_articles

**Purpose:** News articles with sentiment

**Schema:**
```javascript
{
  _id: ObjectId,
  newsId: Long,
  title: String,
  description: String,
  content: String,
  url: String,
  imageUrl: String?,
  source: String,
  author: String?,
  publishedAt: ISODate,
  sentiment: {
    sentimentType: String,        // POSITIVE, NEGATIVE, NEUTRAL
    score: Double,
    confidence: Double
  },
  keywords: [String],
  createdAt: ISODate
}
```

**Indexes:**
```javascript
db.news_articles.createIndex({ newsId: 1 }, { unique: true });
db.news_articles.createIndex({ url: 1 }, { unique: true });
db.news_articles.createIndex({ publishedAt: -1 });
db.news_articles.createIndex({ "sentiment.sentimentType": 1 });
db.news_articles.createIndex({ keywords: 1 });
```

---

#### 12. market_data

**Purpose:** Candle/OHLCV data

**Schema:**
```javascript
{
  _id: ObjectId,
  symbol: String,
  exchange: String,
  interval: String,               // 1m, 5m, 1h, 1d, etc.
  timestamp: ISODate,
  open: Decimal128,
  high: Decimal128,
  low: Decimal128,
  close: Decimal128,
  volume: Decimal128
}
```

**Indexes:**
```javascript
db.market_data.createIndex({ symbol: 1, exchange: 1, interval: 1, timestamp: -1 });
db.market_data.createIndex({ timestamp: -1 });
```

---

## 📊 Database Relationships

### Cross-Database References

```
PostgreSQL                    MongoDB
┌──────────────┐            ┌──────────────┐
│  sns_users   │◄───────────│  sns_posts   │
│  (user_id)   │  userId    │              │
└──────────────┘            └──────────────┘
       ▲                           ▲
       │ user_id                   │ postId
       │                           │
┌──────────────┐            ┌──────────────┐
│ sns_follows  │            │ sns_comments │
└──────────────┘            └──────────────┘
```

### Key Relationship Patterns

1. **User → Posts:** One-to-Many (user_id reference)
2. **User → Followers:** Many-to-Many (sns_follows junction table)
3. **Post → Comments:** One-to-Many (postId reference)
4. **Comment → Replies:** Self-referential (parentCommentId)
5. **User → Portfolios:** One-to-Many
6. **Portfolio → Holdings:** One-to-Many (cascade delete)
7. **Portfolio → Trades:** One-to-Many (historical record)

---

## 🔑 Indexes Strategy

### PostgreSQL Indexes

**Purpose:** Query optimization

**Primary Indexes:**
- Primary keys (auto-indexed)
- Unique constraints
- Foreign keys

**Secondary Indexes:**
```sql
-- Frequent queries
CREATE INDEX idx_users_email ON sns_users(email);
CREATE INDEX idx_portfolios_user_public ON sns_investment_portfolios(user_id, is_public);

-- Sorting
CREATE INDEX idx_posts_created_at ON sns_posts(created_at DESC);

-- Composite indexes for complex queries
CREATE INDEX idx_follows_follower_following ON sns_follows(follower_id, following_id);
```

### MongoDB Indexes

**Purpose:** Query performance

**Common Patterns:**
```javascript
// Unique identifiers
db.collection.createIndex({ id: 1 }, { unique: true });

// Frequent lookups
db.collection.createIndex({ userId: 1, createdAt: -1 });

// Array fields
db.collection.createIndex({ hashtags: 1 });

// Compound indexes
db.collection.createIndex({ userId: 1, isHidden: 1, createdAt: -1 });

// Text search
db.collection.createIndex({ title: "text", content: "text" });
```

---

## 📐 Data Types Reference

### PostgreSQL Types

| Type | Usage | Example |
|------|-------|---------|
| BIGSERIAL | Auto-increment IDs | user_id, order_id |
| VARCHAR(n) | Variable strings | email, username |
| TEXT | Long text | bio, content |
| DECIMAL(18,2) | Monetary values | price, total_value |
| DECIMAL(18,8) | Crypto quantities | quantity |
| INTEGER | Counters | follower_count |
| BOOLEAN | Flags | is_active, is_public |
| TIMESTAMP | Date/time | created_at, updated_at |

### MongoDB Types

| Type | Usage | Example |
|------|-------|---------|
| ObjectId | MongoDB ID | _id |
| Long | Sequential IDs | postId, userId |
| String | Text | caption, title |
| Int32 | Counters | likeCount |
| Decimal128 | Precise numbers | price, quantity |
| Array | Lists | imageUrls, hashtags |
| Object | Nested data | sentiment, alertConditions |
| ISODate | Timestamps | createdAt |
| Boolean | Flags | isRead, isHidden |

---

## 🔄 Migration Scripts

### Initial Setup

**PostgreSQL:**
```sql
-- Create database
CREATE DATABASE rest_server;

-- Create user
CREATE USER rest_user WITH PASSWORD 'secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE rest_server TO rest_user;
```

**MongoDB:**
```javascript
// Create database
use rest_server;

// Create user
db.createUser({
  user: "rest_user",
  pwd: "secure_password",
  roles: [
    { role: "readWrite", db: "rest_server" }
  ]
});
```

### Sample Data

**Insert test user:**
```sql
INSERT INTO sns_users (email, username, full_name, created_at, updated_at)
VALUES ('test@example.com', 'testuser', 'Test User', NOW(), NOW())
RETURNING user_id;
```

**Insert test post:**
```javascript
db.sns_posts.insertOne({
  postId: 1,
  userId: 1,
  caption: "Test post #test",
  imageUrls: ["https://example.com/image.jpg"],
  hashtags: ["test"],
  likeCount: 0,
  commentCount: 0,
  isHidden: false,
  createdAt: new Date(),
  updatedAt: new Date()
});
```

---

## 📚 Additional Resources

- [Complete Architecture](./COMPLETE_ARCHITECTURE.md)
- [SNS Repository Adapters](./SNS_REPOSITORY_ADAPTERS.md)
- [Deployment Guide](./DEPLOYMENT_GUIDE.md)
- [Database Service Documentation](https://github.com/YouSangSon/database-service)

---

**Last Updated:** 2025-11-14
**Version:** 1.0.0
**Total Tables:** 8 (PostgreSQL)
**Total Collections:** 12 (MongoDB)
