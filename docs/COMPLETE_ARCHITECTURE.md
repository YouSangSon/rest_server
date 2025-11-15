# Complete System Architecture

## 📋 Overview

This document provides a comprehensive overview of the complete REST Server architecture, including both the **Trading Bot** and **SNS (Investment-Focused Social Media)** systems.

**Last Updated:** 2025-11-14
**Version:** 2.0.0

---

## 🎯 System Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         REST Server API                              │
│                    (Spring Boot 3.2 + Kotlin)                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌───────────────────────┐      ┌───────────────────────────┐      │
│  │   Trading Bot APIs    │      │      SNS APIs             │      │
│  │   - News Collection   │      │   - Social Media          │      │
│  │   - Auto Trading      │      │   - Investment Portfolios │      │
│  │   - Market Data       │      │   - Community             │      │
│  │   - ML Prediction     │      │   - Messaging             │      │
│  │   - Risk Management   │      │   - Notifications         │      │
│  └───────────────────────┘      └───────────────────────────┘      │
│                                                                       │
└───────────────────────────┬───────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────────────┐
│                      Database Service                                │
│          Unified API for 6 Databases + CDC + Monitoring             │
└───────────┬─────────────────────────┬───────────────────────────────┘
            │                         │
    ┌───────┴─────┐           ┌───────┴─────┐
    ↓             ↓           ↓             ↓
┌─────────┐ ┌──────────┐ ┌─────────┐ ┌──────────┐
│PostgreSQL│ │ MongoDB  │ │ Kafka   │ │  Redis   │
└─────────┘ └──────────┘ └─────────┘ └──────────┘
```

---

## 🏗️ Layered Architecture

### Clean Architecture (Hexagonal/Ports & Adapters)

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  REST Controllers, DTOs, Request/Response Mappers           │
│  - Trading Controllers (News, Order, Strategy, etc.)        │
│  - SNS Controllers (Auth, Post, User, Portfolio, etc.)      │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│                   Application Layer                          │
│  Use Cases, Services, Business Logic                         │
│  - Trading Services (NewsService, TradingService, etc.)     │
│  - SNS Services (PostService, FollowService, etc.)          │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                             │
│  Domain Models, Business Rules, Domain Events                │
│  - Trading Domain (Order, NewsArticle, Strategy, etc.)      │
│  - SNS Domain (User, Post, Portfolio, etc.)                 │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────────┐
│                 Infrastructure Layer                         │
│  Repository Adapters, External API Clients                   │
│  - Database Service Adapters (17 SNS + 2 Trading)          │
│  - External APIs (NewsAPI, Binance, Upbit, etc.)           │
│  - ML Service Client                                         │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Module Structure

### 1. Trading Bot Module

#### Components

**Domain Models:**
- `NewsArticle` - News data with sentiment
- `Order` - Trading orders
- `TradingStrategy` - Strategy configurations
- `MarketData` - Price and candle data
- `Portfolio` - Trading portfolio
- `BacktestResult` - Backtesting results

**Services:**
- `NewsService` - News collection and sentiment analysis
- `TradingService` - Order execution
- `MarketDataService` - Price data management
- `StrategyService` - Trading strategy execution
- `RiskManagementService` - Risk checks
- `BacktestingService` - Historical testing
- `MLServiceClient` - ML predictions

**External APIs:**
- `NewsAPIAdapter` - NewsAPI integration
- `BinanceAdapter` - Binance exchange
- `UpbitAdapter` - Upbit exchange
- `TelegramBotAdapter` - Telegram notifications
- `SlackAdapter` - Slack notifications

**Scheduler:**
- `TradingScheduler` - Auto-execution
  - Strategy execution (1 min)
  - Market data collection (30 sec)
  - News collection (5 min)
  - Risk checks (1 min)
  - Stop-loss/Take-profit (10 sec)

### 2. SNS Module

#### Components

**Domain Models (15 entities):**
- Social: `SnsUser`, `Post`, `Comment`, `Like`, `Follow`
- Investment: `InvestmentPortfolio`, `AssetHolding`, `TradeHistory`
- Communication: `Notification`, `Conversation`, `Message`
- Content: `Story`, `InvestmentPost`, `Bookmark`, `WatchlistItem`

**Services (6 services):**
- `SnsAuthService` - Authentication
- `PostService` - Post management
- `CommentService` - Comments
- `FollowService` - Follow relationships
- `NotificationService` - Notifications
- `InvestmentPortfolioService` - Portfolios

**Repository Adapters (17 adapters):**
- PostgreSQL: Users, Portfolios, Follows, Conversations
- MongoDB: Posts, Comments, Notifications, Messages

**REST Controllers:**
- `SnsAuthController` - 6 endpoints
- `PostController` - 11 endpoints
- `UserController` - 7 endpoints
- `InvestmentPortfolioController` - 13 endpoints

---

## 🗄️ Database Strategy

### PostgreSQL Usage

**Characteristics:** Structured, relational data

**Trading Bot:**
- Orders (transactional data)
- Trading strategies
- Portfolios

**SNS:**
- Users (relational queries)
- Investment portfolios
- Asset holdings
- Trade history
- Follow relationships
- Conversations

### MongoDB Usage

**Characteristics:** Document-based, flexible schema

**Trading Bot:**
- News articles (varying structure)
- Market data (time-series)

**SNS:**
- Posts (arrays of images/hashtags)
- Comments (nested structure)
- Likes
- Notifications (JSON payloads)
- Messages (chat data)
- Stories (temporary content)
- Investment posts
- Watchlists (alert conditions array)

### Redis Usage

**Characteristics:** In-memory cache

**Use Cases:**
- Session storage
- Rate limiting
- Cache for frequently accessed data
- Real-time price data (optional)

### Kafka Usage

**Characteristics:** Event streaming

**Use Cases:**
- Change Data Capture (CDC)
- Event-driven architecture
- Real-time notifications
- Price update events (25+ topics)

---

## 🔐 Security Architecture

### Authentication Flow

```
1. User registers/logs in
   ↓
2. Server generates JWT token (1 hour)
   + Refresh token (7 days)
   ↓
3. Client stores tokens securely
   ↓
4. Client includes JWT in Authorization header
   ↓
5. JwtTokenProvider validates token
   ↓
6. Request proceeds to service layer
```

### JWT Structure

```json
{
  "header": {
    "alg": "HS512",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user_id",
    "iat": 1699920000,
    "exp": 1699923600
  },
  "signature": "..."
}
```

### Security Filters

```
HTTP Request
    ↓
JwtAuthenticationFilter
    ↓
CorsFilter
    ↓
RateLimitFilter
    ↓
Controller
```

---

## 📡 External Integrations

### 1. Database Service

**Purpose:** Unified database access
**URL:** `http://localhost:8080`
**Features:**
- Multi-database support (6 DBs)
- CDC via Kafka
- Optimistic locking
- Monitoring (Grafana/Jaeger)

### 2. ML Service (Python FastAPI)

**Purpose:** AI predictions
**URL:** `http://localhost:8000`
**Features:**
- LSTM price prediction
- FinBERT sentiment analysis
- Multi-step forecasting

### 3. NewsAPI

**Purpose:** News collection
**URL:** `https://newsapi.org/v2`
**Rate Limit:** 100 requests/day (free)

### 4. Binance API

**Purpose:** Crypto trading
**URL:** `https://api.binance.com`
**Auth:** HMAC SHA256

### 5. Upbit API

**Purpose:** Crypto trading (Korea)
**URL:** `https://api.upbit.com`
**Auth:** JWT + SHA512

### 6. Telegram Bot API

**Purpose:** Notifications
**URL:** `https://api.telegram.org`
**Features:** Interactive buttons

### 7. Slack Webhooks

**Purpose:** Notifications
**URL:** Custom webhook URL

---

## 🔄 Data Flow Examples

### Example 1: Create Post Flow

```
1. Client → POST /api/v1/sns/posts
   {caption, imageUrls, location}
   ↓
2. PostController validates request
   ↓
3. PostService.createPost()
   ├─ Extract hashtags from caption
   ├─ Create Post domain object
   └─ Call PostRepositoryPort.save()
       ↓
4. PostRepositoryAdapter (MongoDB)
   └─ DatabaseServiceClient.create()
       ↓
5. Database Service → MongoDB
   └─ Insert document
       ↓
6. Response: PostDto
```

### Example 2: Auto Trading Flow

```
1. TradingScheduler triggers (1 min interval)
   ↓
2. StrategyService.executeStrategies()
   ├─ Get active strategies
   └─ For each strategy:
       ├─ Fetch market data
       ├─ Calculate indicators (SMA, RSI, etc.)
       ├─ Check sentiment (if sentiment strategy)
       ├─ Generate trading signals
       └─ If signal found:
           ├─ RiskManagementService.canOpenPosition()
           ├─ TradingService.placeOrder()
           │   └─ BinanceAdapter.createOrder()
           └─ NotificationService.sendTradingSignal()
               ├─ TelegramBotAdapter.sendMessage()
               └─ SlackAdapter.sendMessage()
```

### Example 3: Portfolio Analytics Flow

```
1. Client → GET /api/v1/sns/portfolios/123/analytics
   ↓
2. InvestmentPortfolioController
   ↓
3. InvestmentPortfolioService.getPortfolioAnalytics()
   ├─ Get portfolio
   ├─ Get all holdings
   ├─ Calculate:
   │   ├─ Total value
   │   ├─ Total return
   │   ├─ Return rate
   │   └─ Asset allocation
   └─ Return PortfolioAnalytics
       ↓
4. Response: JSON with analytics
```

---

## 📊 Performance Optimization

### 1. Caching Strategy

```kotlin
@Cacheable(value = "users", key = "#userId")
fun getUserProfile(userId: Long): SnsUser

@Cacheable(value = "portfolios", key = "#portfolioId")
fun getPortfolio(portfolioId: Long): InvestmentPortfolio

@CacheEvict(value = "users", key = "#userId")
fun updateUserProfile(userId: Long, data: UpdateData)
```

### 2. Bulk Operations

```kotlin
// Trading Bot - bulk news insert
newsRepository.saveAll(articles)  // → bulkInsert API

// SNS - bulk like query
likeRepository.findByPostId(postId, limit = 100)
```

### 3. Pagination

All list endpoints support pagination:
```
GET /api/v1/sns/posts?limit=20&offset=0
GET /api/v1/sns/portfolios/public?limit=20&offset=20
```

### 4. Database Indexing

**PostgreSQL:**
```sql
CREATE INDEX idx_users_email ON sns_users(email);
CREATE INDEX idx_users_username ON sns_users(username);
CREATE INDEX idx_portfolios_user_public ON sns_investment_portfolios(user_id, is_public);
```

**MongoDB:**
```javascript
db.sns_posts.createIndex({userId: 1, createdAt: -1});
db.sns_posts.createIndex({hashtags: 1});
db.sns_comments.createIndex({postId: 1, createdAt: -1});
```

---

## 📈 Monitoring & Observability

### 1. Spring Boot Actuator

**Endpoints:**
- `/actuator/health` - Health checks
- `/actuator/metrics` - JVM metrics
- `/actuator/prometheus` - Prometheus format
- `/actuator/info` - Application info

### 2. Database Service Monitoring

**Grafana Dashboards:**
- Database connection pools
- Query latency (P50, P95, P99)
- Request throughput
- Error rates

**Jaeger Tracing:**
- Distributed trace visualization
- Bottleneck detection
- Error tracking

### 3. Application Metrics

**Custom Metrics:**
```kotlin
@Timed(value = "post.create", description = "Time to create post")
fun createPost(...): Post

@Counted(value = "orders.executed", description = "Trading orders executed")
fun placeOrder(...): Order
```

---

## 🚀 Deployment Architecture

### Development Environment

```
┌──────────────┐
│  Developer   │
│   Machine    │
│              │
│ - IntelliJ   │
│ - Gradle     │
│ - Docker     │
└──────┬───────┘
       │
       ↓
┌──────────────────────────────────┐
│    Docker Compose                 │
│                                   │
│  - PostgreSQL                     │
│  - MongoDB                        │
│  - Redis                          │
│  - Kafka + Zookeeper             │
│  - ML Service (Python)           │
│  - Database Service (optional)   │
└──────────────────────────────────┘
```

### Production Environment

```
┌─────────────────────────────────────────┐
│         Load Balancer (Nginx)            │
└──────────────┬──────────────────────────┘
               │
       ┌───────┴────────┐
       ↓                ↓
┌─────────────┐  ┌─────────────┐
│ REST Server │  │ REST Server │
│  Instance 1 │  │  Instance 2 │
└──────┬──────┘  └──────┬───────┘
       │                │
       └────────┬───────┘
                ↓
┌─────────────────────────────────┐
│      Database Service            │
│    (HA Cluster Mode)            │
└──────────┬──────────────────────┘
           │
   ┌───────┴────────┐
   ↓                ↓
┌─────────┐    ┌──────────┐
│PostgreSQL│    │ MongoDB  │
│ (Primary +│    │(Replica  │
│ Replica) │    │  Set)    │
└─────────┘    └──────────┘
```

---

## 🔧 Configuration Management

### Environment Variables

```bash
# Server
APP_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Database Service
DATABASE_SERVICE_URL=http://database-service:8080
DATABASE_SERVICE_ENABLED=true

# JWT
JWT_SECRET=your-256-bit-secret
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000

# External APIs
NEWSAPI_KEY=xxx
BINANCE_API_KEY=xxx
BINANCE_SECRET_KEY=xxx
UPBIT_ACCESS_KEY=xxx
UPBIT_SECRET_KEY=xxx

# Notifications
TELEGRAM_BOT_TOKEN=xxx
SLACK_WEBHOOK_URL=xxx

# ML Service
ML_SERVICE_URL=http://ml-service:8000
```

### Profile-based Configuration

**application-dev.yml:**
```yaml
logging:
  level:
    yousang.rest: DEBUG
```

**application-prod.yml:**
```yaml
logging:
  level:
    yousang.rest: INFO

spring:
  datasource:
    hikari:
      maximum-pool-size: 50
```

---

## 📚 Technology Stack Summary

### Backend
- **Language:** Kotlin 1.9+
- **Framework:** Spring Boot 3.2
- **JDK:** Java 21 (Virtual Threads)
- **Build Tool:** Gradle 8.x

### Databases
- **PostgreSQL:** 15+ (relational data)
- **MongoDB:** 7+ (document data)
- **Redis:** 7+ (cache)
- **Kafka:** 7.5+ (events)

### External Services
- **Database Service:** Multi-DB unified API
- **ML Service:** Python FastAPI + LSTM + FinBERT

### Monitoring
- **Actuator:** Spring Boot metrics
- **Prometheus:** Metrics collection
- **Grafana:** Visualization
- **Jaeger:** Distributed tracing

### API Documentation
- **Swagger/OpenAPI:** 3.0
- **SpringDoc:** Auto-generation

---

## 🎯 Design Patterns Used

1. **Clean Architecture** - Separation of concerns
2. **Repository Pattern** - Data access abstraction
3. **Adapter Pattern** - External service integration
4. **Strategy Pattern** - Trading strategies
5. **Factory Pattern** - Object creation
6. **Builder Pattern** - Complex object construction
7. **Observer Pattern** - Event handling
8. **Singleton Pattern** - Service beans
9. **Dependency Injection** - Spring IoC

---

## 📊 Metrics & KPIs

### System Metrics
- **Response Time:** P95 < 500ms
- **Throughput:** 1000+ req/s
- **Availability:** 99.9% uptime
- **Error Rate:** < 0.1%

### Business Metrics
- **Trading:**
  - Orders executed per day
  - Strategy performance (win rate, Sharpe ratio)
  - Risk violations

- **SNS:**
  - Daily active users
  - Posts created per day
  - Engagement rate (likes, comments)
  - Portfolio views

---

## 🔮 Future Enhancements

### Planned Features
1. **GraphQL API** - Alternative to REST
2. **WebSocket Support** - Real-time updates
3. **Microservices Split** - Trading + SNS separation
4. **Kubernetes Deployment** - Container orchestration
5. **Multi-tenancy** - SaaS support
6. **Advanced Analytics** - ML-driven insights
7. **Mobile Apps** - React Native / Flutter
8. **Blockchain Integration** - DeFi support

### Scalability Roadmap
1. **Phase 1:** Vertical scaling (current)
2. **Phase 2:** Horizontal scaling (load balancer)
3. **Phase 3:** Microservices architecture
4. **Phase 4:** Serverless components
5. **Phase 5:** Global CDN distribution

---

## 📖 Additional Documentation

- [SNS API Documentation](./SNS_API_DOCUMENTATION.md)
- [SNS Repository Adapters Guide](./SNS_REPOSITORY_ADAPTERS.md)
- [Database Service Setup](./DATABASE_SERVICE_SETUP.md)
- [Deployment Guide](./DEPLOYMENT_GUIDE.md)
- [Developer Guide](./DEVELOPER_GUIDE.md)
- [Database Schema](./DATABASE_SCHEMA.md)

---

**Last Updated:** 2025-11-14
**Version:** 2.0.0
**Total Endpoints:** 50+
**Total Services:** 15+
**Total Domain Models:** 25+
