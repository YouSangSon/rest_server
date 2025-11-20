# API Quick Reference

**빠른 API 참조 가이드** - 모든 엔드포인트의 간단한 요약

## 🤖 Trading Bot API

### Strategies (전략 관리)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/strategies` | 전략 목록 조회 | ✅ |
| POST | `/api/v1/strategies` | 새 전략 생성 | ✅ |
| GET | `/api/v1/strategies/{id}` | 전략 상세 조회 | ✅ |
| PUT | `/api/v1/strategies/{id}` | 전략 수정 | ✅ |
| DELETE | `/api/v1/strategies/{id}` | 전략 삭제 | ✅ |
| POST | `/api/v1/strategies/{id}/activate` | 전략 활성화 | ✅ |
| POST | `/api/v1/strategies/{id}/deactivate` | 전략 비활성화 | ✅ |

### Orders (주문 관리)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/orders` | 주문 생성 (매수/매도) | ✅ |
| GET | `/api/v1/orders` | 주문 목록 조회 | ✅ |
| GET | `/api/v1/orders/{id}` | 주문 상세 조회 | ✅ |
| DELETE | `/api/v1/orders/{id}` | 주문 취소 | ✅ |

### Backtesting (백테스팅)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/backtest/run` | 백테스트 실행 | ✅ |
| GET | `/api/v1/backtest/{id}/results` | 백테스트 결과 조회 | ✅ |
| GET | `/api/v1/backtest/history` | 백테스트 히스토리 | ✅ |

### Portfolio (포트폴리오)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/portfolio/balances` | 잔고 조회 | ✅ |
| GET | `/api/v1/portfolio/performance` | 성과 분석 | ✅ |
| GET | `/api/v1/portfolio/history` | 거래 히스토리 | ✅ |

### Market Data (시장 데이터)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/market/ticker/{symbol}` | 현재가 조회 | ❌ |
| GET | `/api/v1/market/orderbook/{symbol}` | 호가 조회 | ❌ |
| GET | `/api/v1/market/trades/{symbol}` | 체결 내역 조회 | ❌ |
| GET | `/api/v1/market/candles/{symbol}` | 캔들 데이터 조회 | ❌ |

---

## 📱 SNS Platform API

### Authentication (인증)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/sns/auth/register` | 회원가입 | ❌ |
| POST | `/api/v1/sns/auth/login` | 로그인 | ❌ |
| POST | `/api/v1/sns/auth/refresh` | 토큰 갱신 | ❌ |
| POST | `/api/v1/sns/auth/logout` | 로그아웃 | ✅ |
| GET | `/api/v1/sns/auth/profile` | 내 프로필 조회 | ✅ |
| PUT | `/api/v1/sns/auth/profile` | 프로필 수정 | ✅ |

### Users (사용자)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/sns/users/{id}` | 사용자 프로필 조회 | ✅ |
| GET | `/api/v1/sns/users/search` | 사용자 검색 | ✅ |
| POST | `/api/v1/sns/users/{id}/follow` | 팔로우 | ✅ |
| DELETE | `/api/v1/sns/users/{id}/follow` | 언팔로우 | ✅ |
| GET | `/api/v1/sns/users/{id}/followers` | 팔로워 목록 | ✅ |
| GET | `/api/v1/sns/users/{id}/following` | 팔로잉 목록 | ✅ |

### Posts (게시물)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/sns/posts` | 피드 조회 (팔로잉) | ✅ |
| POST | `/api/v1/sns/posts` | 게시물 작성 | ✅ |
| GET | `/api/v1/sns/posts/{id}` | 게시물 상세 조회 | ✅ |
| PUT | `/api/v1/sns/posts/{id}` | 게시물 수정 | ✅ |
| DELETE | `/api/v1/sns/posts/{id}` | 게시물 삭제 | ✅ |
| POST | `/api/v1/sns/posts/{id}/like` | 좋아요 | ✅ |
| DELETE | `/api/v1/sns/posts/{id}/like` | 좋아요 취소 | ✅ |
| GET | `/api/v1/sns/posts/{id}/likes` | 좋아요 목록 | ✅ |
| GET | `/api/v1/sns/posts/explore` | 탐색 (인기 게시물) | ✅ |
| GET | `/api/v1/sns/posts/hashtag/{tag}` | 해시태그 검색 | ✅ |

### Comments (댓글)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/sns/posts/{id}/comments` | 댓글 목록 조회 | ✅ |
| POST | `/api/v1/sns/posts/{id}/comments` | 댓글 작성 | ✅ |
| PUT | `/api/v1/sns/comments/{id}` | 댓글 수정 | ✅ |
| DELETE | `/api/v1/sns/comments/{id}` | 댓글 삭제 | ✅ |
| POST | `/api/v1/sns/comments/{id}/reply` | 답글 작성 | ✅ |

### Investment Portfolios (투자 포트폴리오)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/sns/portfolios` | 내 포트폴리오 목록 | ✅ |
| POST | `/api/v1/sns/portfolios` | 포트폴리오 생성 | ✅ |
| GET | `/api/v1/sns/portfolios/{id}` | 포트폴리오 상세 | ✅ |
| PUT | `/api/v1/sns/portfolios/{id}` | 포트폴리오 수정 | ✅ |
| DELETE | `/api/v1/sns/portfolios/{id}` | 포트폴리오 삭제 | ✅ |
| GET | `/api/v1/sns/portfolios/{id}/analytics` | 포트폴리오 분석 | ✅ |
| GET | `/api/v1/sns/portfolios/discover` | 공개 포트폴리오 탐색 | ✅ |
| POST | `/api/v1/sns/portfolios/{id}/follow` | 포트폴리오 팔로우 | ✅ |
| DELETE | `/api/v1/sns/portfolios/{id}/follow` | 포트폴리오 언팔로우 | ✅ |

### Asset Holdings (보유 자산)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/sns/portfolios/{id}/holdings` | 보유 자산 목록 | ✅ |
| POST | `/api/v1/sns/portfolios/{id}/holdings` | 자산 추가 | ✅ |
| PUT | `/api/v1/sns/holdings/{id}` | 자산 수정 | ✅ |
| DELETE | `/api/v1/sns/holdings/{id}` | 자산 삭제 | ✅ |

### Trade History (거래 내역)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/sns/portfolios/{id}/trades` | 거래 내역 조회 | ✅ |
| POST | `/api/v1/sns/portfolios/{id}/trades` | 거래 기록 | ✅ |
| DELETE | `/api/v1/sns/trades/{id}` | 거래 삭제 | ✅ |

### Investment Posts (투자 아이디어)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/sns/investment-posts` | 투자 아이디어 피드 | ✅ |
| POST | `/api/v1/sns/investment-posts` | 투자 아이디어 작성 | ✅ |
| GET | `/api/v1/sns/investment-posts/{id}` | 투자 아이디어 상세 | ✅ |
| PUT | `/api/v1/sns/investment-posts/{id}` | 투자 아이디어 수정 | ✅ |
| DELETE | `/api/v1/sns/investment-posts/{id}` | 투자 아이디어 삭제 | ✅ |

### Messages (메시지)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/sns/conversations` | 대화 목록 조회 | ✅ |
| GET | `/api/v1/sns/conversations/{id}` | 대화 상세 조회 | ✅ |
| GET | `/api/v1/sns/conversations/{id}/messages` | 메시지 목록 | ✅ |
| POST | `/api/v1/sns/messages` | 메시지 전송 | ✅ |
| DELETE | `/api/v1/sns/messages/{id}` | 메시지 삭제 | ✅ |

### Notifications (알림)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/sns/notifications` | 알림 목록 조회 | ✅ |
| PUT | `/api/v1/sns/notifications/{id}/read` | 알림 읽음 처리 | ✅ |
| PUT | `/api/v1/sns/notifications/read-all` | 모든 알림 읽음 처리 | ✅ |
| DELETE | `/api/v1/sns/notifications/{id}` | 알림 삭제 | ✅ |

### Bookmarks (북마크)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/sns/bookmarks` | 북마크 목록 조회 | ✅ |
| POST | `/api/v1/sns/posts/{id}/bookmark` | 북마크 추가 | ✅ |
| DELETE | `/api/v1/sns/posts/{id}/bookmark` | 북마크 삭제 | ✅ |

### Watchlist (관심 종목)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/sns/watchlist` | 관심 종목 목록 | ✅ |
| POST | `/api/v1/sns/watchlist` | 관심 종목 추가 | ✅ |
| DELETE | `/api/v1/sns/watchlist/{id}` | 관심 종목 삭제 | ✅ |
| PUT | `/api/v1/sns/watchlist/{id}/alert` | 가격 알림 설정 | ✅ |

### Stories (스토리)
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/sns/stories` | 스토리 피드 조회 | ✅ |
| POST | `/api/v1/sns/stories` | 스토리 작성 | ✅ |
| DELETE | `/api/v1/sns/stories/{id}` | 스토리 삭제 | ✅ |
| POST | `/api/v1/sns/stories/{id}/view` | 스토리 조회 기록 | ✅ |
| GET | `/api/v1/sns/stories/{id}/views` | 스토리 조회자 목록 | ✅ |

---

## 🔧 System API

### Health & Monitoring
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/actuator/health` | 헬스 체크 | ❌ |
| GET | `/actuator/metrics` | 메트릭 조회 | ✅ |
| GET | `/actuator/prometheus` | Prometheus 메트릭 | ❌ |
| GET | `/actuator/info` | 애플리케이션 정보 | ❌ |

### Documentation
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/swagger-ui.html` | Swagger UI | ❌ |
| GET | `/v3/api-docs` | OpenAPI JSON | ❌ |

---

## 📝 Request/Response Examples

### Register (회원가입)
```bash
POST /api/v1/sns/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123!",
  "username": "johndoe",
  "fullName": "John Doe"
}

# Response
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "userId": 1,
    "email": "user@example.com",
    "username": "johndoe",
    "fullName": "John Doe"
  }
}
```

### Create Post (게시물 작성)
```bash
POST /api/v1/sns/posts
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "caption": "Just bought $AAPL at $150! #investing #stocks",
  "imageUrls": [
    "https://cdn.example.com/image1.jpg",
    "https://cdn.example.com/image2.jpg"
  ],
  "location": "New York, NY"
}

# Response
{
  "postId": 123,
  "userId": 1,
  "caption": "Just bought $AAPL at $150! #investing #stocks",
  "imageUrls": ["..."],
  "hashtags": ["investing", "stocks"],
  "likeCount": 0,
  "commentCount": 0,
  "createdAt": "2025-11-15T10:30:00Z"
}
```

### Create Portfolio (포트폴리오 생성)
```bash
POST /api/v1/sns/portfolios
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "name": "My Tech Portfolio",
  "description": "Long-term tech investments",
  "isPublic": true,
  "currency": "USD"
}

# Response
{
  "portfolioId": 456,
  "userId": 1,
  "name": "My Tech Portfolio",
  "description": "Long-term tech investments",
  "totalValue": 0.00,
  "totalReturn": 0.00,
  "returnRate": 0.00,
  "isPublic": true,
  "currency": "USD"
}
```

### Add Holding (자산 추가)
```bash
POST /api/v1/sns/portfolios/456/holdings
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "assetSymbol": "AAPL",
  "assetName": "Apple Inc.",
  "assetType": "STOCK",
  "quantity": 10,
  "averagePrice": 150.00,
  "currentPrice": 155.00
}

# Response
{
  "holdingId": 789,
  "portfolioId": 456,
  "assetSymbol": "AAPL",
  "quantity": 10.00,
  "averagePrice": 150.00,
  "currentPrice": 155.00,
  "totalValue": 1550.00,
  "profitLoss": 50.00,
  "profitLossRate": 3.33
}
```

### Create Trading Strategy (전략 생성)
```bash
POST /api/v1/strategies
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "name": "RSI Momentum Strategy",
  "description": "Buy when RSI < 30, Sell when RSI > 70",
  "symbol": "BTC-KRW",
  "exchange": "UPBIT",
  "strategyType": "MOMENTUM",
  "parameters": {
    "rsiPeriod": 14,
    "oversoldLevel": 30,
    "overboughtLevel": 70,
    "stopLossPercent": 5.0,
    "takeProfitPercent": 10.0
  }
}

# Response
{
  "strategyId": 101,
  "name": "RSI Momentum Strategy",
  "symbol": "BTC-KRW",
  "exchange": "UPBIT",
  "strategyType": "MOMENTUM",
  "isActive": false,
  "createdAt": "2025-11-15T10:30:00Z"
}
```

---

## 🔐 Authentication

### JWT Token 사용
모든 보호된 엔드포인트는 JWT 토큰이 필요합니다:

```bash
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMjMiLCJpYXQiOjE2MzI...
```

### Token Refresh
Access Token이 만료되면 Refresh Token으로 갱신:

```bash
POST /api/v1/sns/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

---

## 📊 Pagination

목록 조회 API는 페이지네이션을 지원합니다:

```bash
GET /api/v1/sns/posts?limit=20&offset=0
```

**Parameters:**
- `limit`: 한 페이지당 항목 수 (기본값: 20, 최대: 100)
- `offset`: 시작 위치 (기본값: 0)

**Response:**
```json
{
  "data": [...],
  "meta": {
    "limit": 20,
    "offset": 0,
    "hasMore": true
  }
}
```

---

## ⚡ Rate Limiting

API 호출 제한:
- **인증된 사용자**: 1000 requests/hour
- **비인증 사용자**: 100 requests/hour

Rate limit 초과 시:
```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again in 3600 seconds."
}
```

---

## 📖 상세 문서

- **SNS API 상세 문서**: [SNS_API_DOCUMENTATION.md](SNS_API_DOCUMENTATION.md)
- **Trading Bot API**: [TRADING_BOT_API.md](TRADING_BOT_API.md)
- **시스템 아키텍처**: [COMPLETE_ARCHITECTURE.md](COMPLETE_ARCHITECTURE.md)
- **데이터베이스 스키마**: [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md)

---

**Last Updated**: 2025-11-15 | **Version**: v3.0.0
