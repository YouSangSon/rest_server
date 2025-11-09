# 자동 투자 시스템 (Algorithmic Trading Platform) 아키텍처

## 📋 목차
1. [시스템 개요](#시스템-개요)
2. [전체 아키텍처](#전체-아키텍처)
3. [Phase 1: 데이터 수집 인프라](#phase-1-데이터-수집-인프라)
4. [Phase 2: AI/ML 분석 엔진](#phase-2-aiml-분석-엔진)
5. [Phase 3: 자동 매매 엔진](#phase-3-자동-매매-엔진)
6. [Phase 4: 모니터링 & 알림](#phase-4-모니터링--알림)
7. [데이터 플로우](#데이터-플로우)
8. [보안 & 리스크 관리](#보안--리스크-관리)
9. [배포 전략](#배포-전략)
10. [API 명세](#api-명세)

---

## 시스템 개요

### 비전
**실시간 글로벌 데이터 기반 지능형 자동 투자 플랫폼**

전 세계 뉴스, SNS, 경제 지표, 시장 데이터를 실시간으로 수집·분석하여 AI/ML 기반 예측과 전략으로 자동 매수/매도를 실행하는 엔터프라이즈급 트레이딩 시스템.

### 핵심 기능
- ✅ **실시간 다중 소스 데이터 수집**: 뉴스, SNS, 거래소, 경제 지표
- ✅ **AI/ML 분석**: 감성 분석, 가격 예측, 연관성 분석
- ✅ **자동 매매**: 다중 전략 엔진, 리스크 관리, 백테스팅
- ✅ **인터랙티브 알림**: Telegram Bot (버튼 UI), Slack 통합
- ✅ **실시간 모니터링**: Grafana 대시보드, 성과 분석

### 기술 스택
| 카테고리 | 기술 |
|---------|------|
| **Backend** | Kotlin + Spring Boot 3.2 + Java 21 Virtual Threads |
| **AI/ML** | Python FastAPI + TensorFlow/PyTorch + Hugging Face |
| **Database** | PostgreSQL (거래 데이터) + MongoDB (비정형 데이터) + Redis (캐시) |
| **Streaming** | Apache Kafka (실시간 이벤트 스트리밍) |
| **Messaging** | Telegram Bot API + Slack Webhook |
| **Monitoring** | Prometheus + Grafana + Spring Boot Actuator |
| **Container** | Docker + Kubernetes (AWS EKS) |

---

## 전체 아키텍처

### 시스템 컨텍스트 다이어그램

```mermaid
graph TB
    subgraph "외부 데이터 소스"
        News[뉴스 API<br/>NewsAPI, Alpha Vantage]
        Social[SNS<br/>Twitter/X API, Reddit API]
        Exchange[거래소 API<br/>Binance, Upbit, Bithumb]
        Economic[경제 지표<br/>World Bank, 한국은행]
    end

    subgraph "Trading Platform Core"
        subgraph "데이터 수집 레이어"
            Collector[Data Collectors<br/>Virtual Threads]
            Kafka[Apache Kafka<br/>실시간 스트리밍]
        end

        subgraph "데이터 저장 레이어"
            PG[(PostgreSQL<br/>거래/주문/포트폴리오)]
            Mongo[(MongoDB<br/>뉴스/SNS/분석)]
            Redis[(Redis<br/>실시간 시세/캐시)]
        end

        subgraph "분석 레이어"
            Sentiment[감성 분석<br/>NLP/BERT]
            Prediction[가격 예측<br/>LSTM/Transformer]
            Correlation[연관성 분석<br/>상관계수/인과관계]
        end

        subgraph "트레이딩 레이어"
            Strategy[전략 엔진<br/>다중 전략 실행]
            Risk[리스크 관리<br/>손절/익절/포지션]
            Execution[주문 실행<br/>스마트 라우팅]
            Backtest[백테스팅<br/>성과 검증]
        end

        subgraph "알림 레이어"
            TelegramBot[Telegram Bot<br/>인터랙티브 UI]
            SlackBot[Slack Webhook<br/>알림 발송]
        end
    end

    subgraph "사용자"
        User[투자자<br/>Telegram/Slack]
        Admin[관리자<br/>Grafana Dashboard]
    end

    News & Social & Exchange & Economic --> Collector
    Collector --> Kafka
    Kafka --> PG & Mongo & Redis
    PG & Mongo & Redis --> Sentiment & Prediction & Correlation
    Sentiment & Prediction & Correlation --> Strategy
    Strategy --> Risk
    Risk --> Execution
    Execution --> Exchange
    Execution --> Backtest

    Strategy --> TelegramBot & SlackBot
    Execution --> TelegramBot & SlackBot
    Risk --> TelegramBot & SlackBot

    User <-->|메시지/버튼| TelegramBot
    User <-->|알림 수신| SlackBot
    Admin -->|모니터링| Grafana[Grafana<br/>대시보드]

    style Kafka fill:#231F20,stroke:#000,color:#fff
    style Strategy fill:#E74C3C,stroke:#C0392B,color:#fff
    style TelegramBot fill:#0088CC,stroke:#006699,color:#fff
    style Execution fill:#2ECC71,stroke:#27AE60,color:#fff
```

### 레이어별 책임

```mermaid
graph LR
    subgraph "Layer 1: Data Collection"
        L1[데이터 수집기<br/>뉴스/SNS/거래소/경제]
    end

    subgraph "Layer 2: Data Processing"
        L2[Kafka 스트리밍<br/>실시간 처리]
    end

    subgraph "Layer 3: Data Storage"
        L3[Polyglot Persistence<br/>PG/Mongo/Redis]
    end

    subgraph "Layer 4: AI/ML Analysis"
        L4[분석 엔진<br/>감성/예측/연관성]
    end

    subgraph "Layer 5: Trading Engine"
        L5[매매 엔진<br/>전략/리스크/실행]
    end

    subgraph "Layer 6: Notification"
        L6[알림 시스템<br/>Telegram/Slack]
    end

    L1 --> L2
    L2 --> L3
    L3 --> L4
    L4 --> L5
    L5 --> L6

    style L1 fill:#3498DB,stroke:#2980B9,color:#fff
    style L2 fill:#231F20,stroke:#000,color:#fff
    style L3 fill:#95A5A6,stroke:#7F8C8D,color:#fff
    style L4 fill:#9B59B6,stroke:#8E44AD,color:#fff
    style L5 fill:#E74C3C,stroke:#C0392B,color:#fff
    style L6 fill:#0088CC,stroke:#006699,color:#fff
```

---

## Phase 1: 데이터 수집 인프라

### 1-1. 도메인 모델 설계

#### 뉴스 도메인

```mermaid
classDiagram
    class NewsArticle {
        +Long id
        +String source
        +String title
        +String content
        +String url
        +LocalDateTime publishedAt
        +String language
        +List~String~ keywords
        +Double sentimentScore
        +SentimentType sentiment
        +List~String~ relatedSymbols
    }

    class SentimentType {
        <<enumeration>>
        POSITIVE
        NEUTRAL
        NEGATIVE
    }

    class NewsSource {
        +Long id
        +String name
        +String apiKey
        +String baseUrl
        +Integer requestLimit
        +Boolean isActive
    }

    NewsArticle --> SentimentType
    NewsArticle --> NewsSource
```

#### 거래소 도메인

```mermaid
classDiagram
    class MarketData {
        +String symbol
        +String exchange
        +BigDecimal price
        +BigDecimal volume
        +BigDecimal high24h
        +BigDecimal low24h
        +BigDecimal change24h
        +LocalDateTime timestamp
    }

    class TradingPair {
        +String symbol
        +String baseAsset
        +String quoteAsset
        +String exchange
        +BigDecimal minOrderSize
        +BigDecimal tickSize
        +Boolean isActive
    }

    class Order {
        +Long id
        +String orderId
        +String symbol
        +OrderType type
        +OrderSide side
        +BigDecimal price
        +BigDecimal quantity
        +BigDecimal executedQty
        +OrderStatus status
        +LocalDateTime createdAt
    }

    class OrderType {
        <<enumeration>>
        MARKET
        LIMIT
        STOP_LOSS
        TAKE_PROFIT
    }

    class OrderSide {
        <<enumeration>>
        BUY
        SELL
    }

    class OrderStatus {
        <<enumeration>>
        PENDING
        FILLED
        PARTIALLY_FILLED
        CANCELLED
        REJECTED
    }

    Order --> OrderType
    Order --> OrderSide
    Order --> OrderStatus
    Order --> TradingPair
```

### 1-2. Kafka 토픽 설계

```mermaid
mindmap
  root((Kafka Topics))
    Market Data
      trading.market.price
        실시간 가격 데이터
      trading.market.orderbook
        호가창 데이터
      trading.market.trade
        체결 데이터
    News & Sentiment
      news.article.collected
        수집된 뉴스
      news.sentiment.analyzed
        감성 분석 완료
      social.post.collected
        SNS 포스트
    Trading Events
      trading.signal.generated
        매매 신호 생성
      trading.order.created
        주문 생성
      trading.order.filled
        주문 체결
      trading.order.cancelled
        주문 취소
    Analysis
      analysis.prediction.completed
        가격 예측 완료
      analysis.correlation.updated
        연관성 분석 업데이트
    Alerts
      alert.price.threshold
        가격 임계값 알림
      alert.risk.warning
        리스크 경고
      alert.trade.completed
        거래 완료 알림
```

### 1-3. 데이터베이스 스키마

#### PostgreSQL (관계형 데이터)

```sql
-- 거래 쌍 정보
CREATE TABLE trading_pairs (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    base_asset VARCHAR(10) NOT NULL,
    quote_asset VARCHAR(10) NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    min_order_size DECIMAL(20, 8),
    tick_size DECIMAL(20, 8),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_symbol_exchange (symbol, exchange)
);

-- 주문 내역
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    order_side VARCHAR(10) NOT NULL,
    price DECIMAL(20, 8),
    quantity DECIMAL(20, 8) NOT NULL,
    executed_qty DECIMAL(20, 8) DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    strategy_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_symbol (user_id, symbol),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- 포트폴리오
CREATE TABLE portfolios (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    quantity DECIMAL(20, 8) NOT NULL,
    avg_buy_price DECIMAL(20, 8),
    current_price DECIMAL(20, 8),
    unrealized_pnl DECIMAL(20, 8),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, symbol)
);

-- 거래 전략
CREATE TABLE trading_strategies (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    strategy_type VARCHAR(50) NOT NULL,
    config JSONB NOT NULL,
    is_active BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 백테스팅 결과
CREATE TABLE backtest_results (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    initial_capital DECIMAL(20, 8) NOT NULL,
    final_capital DECIMAL(20, 8) NOT NULL,
    total_return DECIMAL(10, 4),
    sharpe_ratio DECIMAL(10, 4),
    max_drawdown DECIMAL(10, 4),
    win_rate DECIMAL(10, 4),
    total_trades INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### MongoDB (비정형 데이터)

```javascript
// 뉴스 기사 컬렉션
db.news_articles.createIndex({ "publishedAt": -1 });
db.news_articles.createIndex({ "relatedSymbols": 1 });
db.news_articles.createIndex({ "sentiment": 1 });

{
  _id: ObjectId,
  source: "Reuters",
  title: "Bitcoin surges to new high",
  content: "...",
  url: "https://...",
  publishedAt: ISODate("2025-11-08T10:00:00Z"),
  language: "en",
  keywords: ["bitcoin", "cryptocurrency", "surge"],
  sentimentScore: 0.85,
  sentiment: "POSITIVE",
  relatedSymbols: ["BTC/USDT", "ETH/USDT"],
  analyzedAt: ISODate("2025-11-08T10:01:00Z")
}

// SNS 포스트 컬렉션
db.social_posts.createIndex({ "createdAt": -1 });
db.social_posts.createIndex({ "platform": 1, "author": 1 });

{
  _id: ObjectId,
  platform: "twitter",
  author: "elonmusk",
  content: "Dogecoin to the moon!",
  url: "https://twitter.com/...",
  likes: 50000,
  retweets: 10000,
  createdAt: ISODate("2025-11-08T09:00:00Z"),
  sentimentScore: 0.92,
  sentiment: "POSITIVE",
  relatedSymbols: ["DOGE/USDT"]
}

// 시장 데이터 (1분봉)
db.market_candles.createIndex({ "symbol": 1, "timestamp": -1 });
db.market_candles.createIndex({ "exchange": 1, "timestamp": -1 });

{
  _id: ObjectId,
  symbol: "BTC/USDT",
  exchange: "binance",
  interval: "1m",
  timestamp: ISODate("2025-11-08T10:00:00Z"),
  open: 45000.00,
  high: 45500.00,
  low: 44800.00,
  close: 45200.00,
  volume: 123.45
}
```

#### Redis (캐시 & 실시간 데이터)

```
# 실시간 가격
price:{symbol}:{exchange} → {"price": 45000, "timestamp": 1699437600}
TTL: 60초

# 뉴스 감성 점수 (최근 1시간)
sentiment:news:{symbol}:1h → 0.75
TTL: 3600초

# 거래 전략 상태
strategy:{strategyId}:status → "RUNNING"
TTL: 무제한 (명시적 삭제)

# Rate Limiting
ratelimit:api:{userId}:{endpoint} → 100
TTL: 60초

# 실시간 포트폴리오
portfolio:{userId} → {"BTC": 0.5, "ETH": 10.0}
TTL: 300초
```

### 1-4. 데이터 수집기 구현 구조

```mermaid
sequenceDiagram
    autonumber
    participant Scheduler as Task Scheduler
    participant Collector as Data Collector
    participant External as External API
    participant Kafka as Kafka Producer
    participant DB as Database

    Scheduler->>Collector: 1분마다 실행
    Collector->>External: 뉴스/시세 요청
    External->>Collector: 데이터 반환
    Collector->>Collector: 데이터 검증 & 변환
    Collector->>Kafka: 이벤트 발행
    Kafka->>DB: Consumer가 저장
    Collector->>Collector: 다음 소스로
```

---

## Phase 2: AI/ML 분석 엔진

### 2-1. 감성 분석 아키텍처

```mermaid
graph LR
    subgraph "데이터 입력"
        News[뉴스 기사]
        Social[SNS 포스트]
    end

    subgraph "전처리"
        Clean[텍스트 정제<br/>HTML 제거, 특수문자]
        Tokenize[토큰화<br/>BERT Tokenizer]
    end

    subgraph "감성 분석 모델"
        BERT[FinBERT<br/>금융 특화 BERT]
        Score[감성 점수<br/>-1.0 ~ +1.0]
    end

    subgraph "후처리"
        Classify[분류<br/>POSITIVE/NEUTRAL/NEGATIVE]
        Store[저장<br/>MongoDB + Kafka]
    end

    News & Social --> Clean
    Clean --> Tokenize
    Tokenize --> BERT
    BERT --> Score
    Score --> Classify
    Classify --> Store

    style BERT fill:#9B59B6,stroke:#8E44AD,color:#fff
```

**모델**: FinBERT (금융 도메인 특화 BERT)
- Hugging Face: `ProsusAI/finbert`
- 입력: 뉴스 제목 + 본문 (최대 512 토큰)
- 출력: 감성 점수 (-1.0 ~ +1.0) + 라벨 (positive/neutral/negative)

### 2-2. 가격 예측 아키텍처

```mermaid
graph TD
    subgraph "입력 데이터"
        Price[가격 데이터<br/>OHLCV]
        Volume[거래량]
        Sentiment[감성 점수]
        Macro[거시 경제 지표]
    end

    subgraph "특징 엔지니어링"
        Technical[기술적 지표<br/>RSI, MACD, BB]
        Normalize[정규화<br/>MinMax Scaling]
    end

    subgraph "예측 모델"
        LSTM[LSTM<br/>시계열 예측]
        Attention[Attention<br/>중요도 가중치]
    end

    subgraph "출력"
        Forecast[가격 예측<br/>1h, 4h, 24h]
        Confidence[신뢰도<br/>0.0 ~ 1.0]
    end

    Price & Volume & Sentiment & Macro --> Technical
    Technical --> Normalize
    Normalize --> LSTM
    LSTM --> Attention
    Attention --> Forecast
    Forecast --> Confidence

    style LSTM fill:#9B59B6,stroke:#8E44AD,color:#fff
```

**모델**: LSTM + Attention Mechanism
- 입력: 과거 168시간 (7일) 데이터
- 특징: OHLCV + 기술적 지표 (20개) + 감성 점수
- 출력: 1시간, 4시간, 24시간 후 가격 예측 + 신뢰도

### 2-3. 연관성 분석

```mermaid
graph TB
    subgraph "데이터 소스"
        BTC[BTC 가격]
        ETH[ETH 가격]
        Stock[주식 지수<br/>NASDAQ, S&P500]
        News[뉴스 감성]
    end

    subgraph "상관 분석"
        Pearson[Pearson 상관계수<br/>선형 관계]
        Granger[Granger 인과관계<br/>시계열 인과성]
    end

    subgraph "결과"
        Matrix[상관 행렬<br/>히트맵]
        Causality[인과 그래프<br/>A→B 영향도]
    end

    BTC & ETH & Stock & News --> Pearson
    BTC & ETH & Stock & News --> Granger
    Pearson --> Matrix
    Granger --> Causality

    style Pearson fill:#9B59B6,stroke:#8E44AD,color:#fff
    style Granger fill:#9B59B6,stroke:#8E44AD,color:#fff
```

---

## Phase 3: 자동 매매 엔진

### 3-1. 트레이딩 전략 엔진

```mermaid
stateDiagram-v2
    [*] --> Idle: 전략 생성
    Idle --> Analyzing: 시장 데이터 수집
    Analyzing --> SignalGenerated: 매수/매도 신호
    SignalGenerated --> RiskCheck: 리스크 검증

    RiskCheck --> OrderPlaced: 통과
    RiskCheck --> Analyzing: 거부 (리스크 초과)

    OrderPlaced --> PositionOpen: 주문 체결
    PositionOpen --> Monitoring: 포지션 모니터링

    Monitoring --> TakeProfitTriggered: 익절 조건 충족
    Monitoring --> StopLossTriggered: 손절 조건 충족
    Monitoring --> SignalReversed: 반대 신호 발생

    TakeProfitTriggered --> OrderClosed: 익절 실행
    StopLossTriggered --> OrderClosed: 손절 실행
    SignalReversed --> OrderClosed: 포지션 청산

    OrderClosed --> Analyzing: 다음 기회 탐색

    Idle --> [*]: 전략 중지
```

**지원 전략 타입**:
1. **Sentiment-Based**: 뉴스/SNS 감성 기반
2. **Technical**: 기술적 지표 (RSI, MACD, Bollinger Bands)
3. **ML-Prediction**: 머신러닝 가격 예측 기반
4. **Hybrid**: 복합 전략 (감성 + 기술적 + 예측)
5. **Arbitrage**: 거래소간 차익거래

### 3-2. 리스크 관리 시스템

```mermaid
graph TD
    A[주문 요청] --> B{포트폴리오 한도?}
    B -->|초과| Z[주문 거부]
    B -->|허용| C{단일 포지션 한도?}
    C -->|초과| Z
    C -->|허용| D{일일 손실 한도?}
    D -->|초과| Z
    D -->|허용| E{레버리지 한도?}
    E -->|초과| Z
    E -->|허용| F[주문 승인]

    F --> G[포지션 오픈]
    G --> H{실시간 모니터링}
    H -->|손절 조건| I[강제 청산]
    H -->|익절 조건| J[익절 실행]
    H -->|정상| H

    style Z fill:#E74C3C,stroke:#C0392B,color:#fff
    style F fill:#2ECC71,stroke:#27AE60,color:#fff
    style I fill:#E74C3C,stroke:#C0392B,color:#fff
    style J fill:#2ECC71,stroke:#27AE60,color:#fff
```

**리스크 파라미터**:
- **최대 포지션 크기**: 포트폴리오의 10%
- **손절 (Stop Loss)**: 진입가 대비 -5%
- **익절 (Take Profit)**: 진입가 대비 +15%
- **일일 최대 손실**: 총 자산의 -3%
- **최대 동시 포지션**: 5개
- **레버리지**: 최대 3x (선물 거래 시)

### 3-3. 주문 실행 시스템

```mermaid
sequenceDiagram
    autonumber
    participant Strategy as 전략 엔진
    participant Risk as 리스크 관리
    participant Router as 스마트 라우터
    participant Binance as Binance
    participant Upbit as Upbit
    participant DB as Database
    participant Kafka as Kafka

    Strategy->>Risk: 매수 신호 (BTC, 0.1개)
    Risk->>Risk: 리스크 검증
    Risk->>Router: 주문 승인
    Router->>Router: 최적 거래소 선택<br/>(가격, 수수료, 유동성)
    Router->>Binance: 주문 전송
    Binance->>Router: 주문 ID 반환
    Router->>DB: 주문 저장 (PENDING)
    Router->>Kafka: 주문 생성 이벤트

    Binance->>Router: 체결 알림 (WebSocket)
    Router->>DB: 주문 상태 업데이트 (FILLED)
    Router->>Kafka: 주문 체결 이벤트
    Kafka->>Strategy: 포지션 업데이트
```

**스마트 오더 라우팅**:
- 거래소별 실시간 가격 비교
- 수수료 최적화
- 유동성 체크 (Slippage 최소화)
- Fallback: 1순위 거래소 실패 시 2순위로 자동 전환

### 3-4. 백테스팅 프레임워크

```mermaid
flowchart TD
    A[백테스팅 시작] --> B[과거 데이터 로드<br/>2024-01-01 ~ 2024-12-31]
    B --> C[초기 자본 설정<br/>$10,000]
    C --> D{시뮬레이션 루프}
    D --> E[시장 데이터 가져오기<br/>t시점]
    E --> F[전략 실행<br/>매수/매도 신호 생성]
    F --> G{신호 발생?}
    G -->|Yes| H[가상 주문 실행]
    G -->|No| I[다음 시점으로]
    H --> J[포트폴리오 업데이트]
    J --> I
    I --> K{마지막 시점?}
    K -->|No| D
    K -->|Yes| L[성과 계산]
    L --> M[총 수익률<br/>Sharpe Ratio<br/>Max Drawdown<br/>Win Rate]
    M --> N[리포트 생성]

    style H fill:#3498DB,stroke:#2980B9,color:#fff
    style M fill:#2ECC71,stroke:#27AE60,color:#fff
```

**백테스팅 메트릭**:
- **Total Return**: (최종 자산 - 초기 자산) / 초기 자산
- **Sharpe Ratio**: (평균 수익률 - 무위험 수익률) / 수익률 표준편차
- **Max Drawdown**: 최고점 대비 최대 하락률
- **Win Rate**: 수익 거래 수 / 전체 거래 수
- **Average Trade**: 거래당 평균 손익

---

## Phase 4: 모니터링 & 알림

### 4-1. Telegram Bot 아키텍처

```mermaid
graph TB
    subgraph "Telegram Bot"
        Bot[Telegram Bot<br/>Long Polling]
        Handler[Command Handler]
        Keyboard[Inline Keyboard<br/>인터랙티브 버튼]
    end

    subgraph "백엔드 API"
        API[REST API<br/>Trading Service]
        Strategy[전략 서비스]
        Portfolio[포트폴리오 서비스]
        Analysis[분석 서비스]
    end

    subgraph "알림 트리거"
        Kafka[Kafka Consumer<br/>거래 이벤트]
        Scheduler[스케줄러<br/>정기 리포트]
    end

    User[사용자] <-->|메시지| Bot
    Bot <--> Handler
    Handler --> Keyboard
    Handler <--> API
    API <--> Strategy & Portfolio & Analysis

    Kafka --> Bot
    Scheduler --> Bot

    style Bot fill:#0088CC,stroke:#006699,color:#fff
    style Keyboard fill:#0088CC,stroke:#006699,color:#fff
```

#### Telegram Bot 기능

**1. 인터랙티브 메뉴 (Inline Keyboard)**

```
┌─────────────────────────────┐
│  자동투자 시스템 Bot        │
├─────────────────────────────┤
│  📊 현재 시황 분석          │
│  💼 내 포트폴리오           │
│  🎯 전략 관리               │
│  📈 실시간 차트              │
│  ⚙️ 설정                    │
└─────────────────────────────┘
```

**2. 전략 관리 메뉴**

```
┌─────────────────────────────┐
│  전략 관리                   │
├─────────────────────────────┤
│  ▶️ 자동매매 시작           │
│  ⏸️ 자동매매 일시정지        │
│  ⏹️ 자동매매 중지           │
│  ➕ 새 전략 추가            │
│  📋 전략 목록 보기           │
└─────────────────────────────┘
```

**3. 실시간 알림 예시**

```
🔔 주문 체결 알림
───────────────
심볼: BTC/USDT
거래소: Binance
타입: 매수 (MARKET)
수량: 0.05 BTC
가격: $45,000
총액: $2,250

전략: Sentiment Strategy
시간: 2025-11-08 10:30:15

[포트폴리오 보기] [차트 보기]
```

**4. 시황 분석 리포트**

```
📊 현재 시황 분석
───────────────
🪙 BTC/USDT: $45,200 (+2.3%)

📰 뉴스 감성: 긍정 (0.78)
- "Bitcoin surges..." (+0.85)
- "Institutions buy..." (+0.92)

🤖 AI 예측:
- 1시간 후: $45,800 (신뢰도 75%)
- 24시간 후: $47,000 (신뢰도 62%)

📈 기술적 지표:
- RSI: 68 (과매수 근접)
- MACD: 골든크로스

💡 추천: 매수 신호 (강도: 8/10)

[자동매매 시작] [더보기]
```

#### 주요 명령어

| 명령어 | 설명 |
|-------|------|
| `/start` | 봇 시작 및 메인 메뉴 |
| `/status` | 현재 시스템 상태 |
| `/portfolio` | 포트폴리오 조회 |
| `/strategies` | 전략 목록 |
| `/start_trading [전략ID]` | 자동매매 시작 |
| `/stop_trading [전략ID]` | 자동매매 중지 |
| `/analysis [심볼]` | 종목 분석 |
| `/backtest [전략ID]` | 백테스팅 실행 |
| `/report daily|weekly` | 성과 리포트 |
| `/settings` | 알림 설정 |

### 4-2. Slack 알림 통합

```mermaid
sequenceDiagram
    autonumber
    participant Kafka as Kafka
    participant Consumer as Slack Consumer
    participant Builder as Message Builder
    participant Slack as Slack Webhook

    Kafka->>Consumer: 거래 이벤트
    Consumer->>Builder: 이벤트 파싱
    Builder->>Builder: 메시지 포맷팅<br/>(Blocks, Attachments)
    Builder->>Slack: Webhook POST
    Slack->>Slack: #trading-alerts 채널에 메시지
```

**Slack 알림 타입**:
- ✅ **주문 체결**: 매수/매도 완료 시
- ⚠️ **리스크 경고**: 손절 임박, 일일 손실 한도 근접
- 📊 **일일 리포트**: 매일 오후 6시 자동 발송
- 🚨 **긴급 알림**: 급등락, 시스템 오류

**Slack 메시지 예시**:
```json
{
  "blocks": [
    {
      "type": "header",
      "text": {
        "type": "plain_text",
        "text": "🎯 매수 주문 체결"
      }
    },
    {
      "type": "section",
      "fields": [
        {"type": "mrkdwn", "text": "*심볼:*\nBTC/USDT"},
        {"type": "mrkdwn", "text": "*가격:*\n$45,000"},
        {"type": "mrkdwn", "text": "*수량:*\n0.05 BTC"},
        {"type": "mrkdwn", "text": "*총액:*\n$2,250"}
      ]
    },
    {
      "type": "section",
      "text": {
        "type": "mrkdwn",
        "text": "*전략:* Sentiment Strategy\n*신뢰도:* 85%"
      }
    },
    {
      "type": "actions",
      "elements": [
        {
          "type": "button",
          "text": {"type": "plain_text", "text": "포트폴리오 보기"},
          "url": "https://dashboard.example.com/portfolio"
        }
      ]
    }
  ]
}
```

### 4-3. Grafana 대시보드

```mermaid
graph TB
    subgraph "Data Sources"
        Prometheus[Prometheus<br/>메트릭 수집]
        PostgreSQL[(PostgreSQL<br/>거래 데이터)]
        MongoDB[(MongoDB<br/>분석 데이터)]
    end

    subgraph "Grafana Dashboards"
        Overview[Overview<br/>전체 요약]
        Portfolio[Portfolio<br/>포트폴리오 현황]
        Strategy[Strategy<br/>전략 성과]
        Risk[Risk<br/>리스크 모니터링]
        System[System<br/>시스템 성능]
    end

    Prometheus --> Overview & System
    PostgreSQL --> Portfolio & Strategy
    MongoDB --> Risk

    style Prometheus fill:#E6522C,stroke:#C43C1F,color:#fff
    style Grafana fill:#F46800,stroke:#D35400,color:#fff
```

**주요 대시보드**:

1. **Overview Dashboard**
   - 총 자산 (실시간)
   - 일일/주간/월간 수익률
   - 활성 전략 수
   - 오픈 포지션 수
   - 최근 거래 내역

2. **Portfolio Dashboard**
   - 자산 분포 (파이 차트)
   - 종목별 수익률
   - 미실현 손익
   - 실현 손익 추이

3. **Strategy Performance Dashboard**
   - 전략별 수익률 비교
   - Win Rate
   - Sharpe Ratio
   - Max Drawdown
   - 거래 빈도

4. **Risk Monitoring Dashboard**
   - 실시간 리스크 레벨
   - 포지션 크기 분포
   - 레버리지 사용률
   - 손절/익절 실행 내역

5. **System Performance Dashboard**
   - API 응답 시간
   - Kafka Lag
   - JVM 메모리 사용률
   - 데이터베이스 연결 수

---

## 데이터 플로우

### 실시간 트레이딩 플로우

```mermaid
sequenceDiagram
    autonumber
    participant NewsAPI as 뉴스 API
    participant Collector as 데이터 수집기
    participant Kafka as Kafka
    participant Sentiment as 감성 분석
    participant Prediction as 가격 예측
    participant Strategy as 전략 엔진
    participant Risk as 리스크 관리
    participant Exchange as 거래소
    participant Telegram as Telegram Bot

    NewsAPI->>Collector: 뉴스 기사
    Collector->>Kafka: news.article.collected
    Kafka->>Sentiment: 감성 분석 요청
    Sentiment->>Kafka: news.sentiment.analyzed (0.85)

    Kafka->>Prediction: 가격 예측 요청
    Prediction->>Kafka: prediction.completed (+5%)

    Kafka->>Strategy: 매매 신호 판단
    Strategy->>Strategy: 감성(0.85) + 예측(+5%) → 매수 신호
    Strategy->>Risk: 주문 요청 (BTC, 0.1개)
    Risk->>Risk: 리스크 검증 통과
    Risk->>Exchange: 주문 전송
    Exchange->>Risk: 체결 완료
    Risk->>Kafka: order.filled
    Kafka->>Telegram: 📱 체결 알림 발송
```

---

## 보안 & 리스크 관리

### 보안 설계

```mermaid
graph TD
    A[API 요청] --> B{JWT 인증}
    B -->|실패| Z[401 Unauthorized]
    B -->|성공| C{권한 확인}
    C -->|실패| Y[403 Forbidden]
    C -->|성공| D{Rate Limiting}
    D -->|초과| X[429 Too Many Requests]
    D -->|허용| E[거래소 API Key 암호화]
    E --> F[주문 실행]
    F --> G[감사 로그 기록]

    style Z fill:#E74C3C,stroke:#C0392B,color:#fff
    style Y fill:#E74C3C,stroke:#C0392B,color:#fff
    style X fill:#E74C3C,stroke:#C0392B,color:#fff
```

**보안 기능**:
- ✅ **API Key 암호화**: AES-256으로 거래소 API Key 저장
- ✅ **2FA**: Google Authenticator 통합
- ✅ **IP Whitelist**: 허용된 IP에서만 접근
- ✅ **감사 로그**: 모든 거래 기록 MongoDB에 저장
- ✅ **자동 로그아웃**: 비활동 30분 후
- ✅ **주문 확인**: 대량 주문 시 추가 인증

### 재해 복구 전략

- **RTO**: 5분 이내
- **RPO**: 1분 이내
- **백업**: PostgreSQL 매시간 백업, MongoDB 실시간 복제
- **Failover**: 거래소 API 장애 시 자동 대체 거래소로 전환

---

## 배포 전략

### Kubernetes 배포

```yaml
# Trading System Pods
- rest-server: 3-10 pods (HPA)
- ml-service: 2-5 pods (GPU)
- telegram-bot: 1 pod
- kafka: 3 brokers
- postgresql: 1 primary + 2 replicas
- mongodb: 3 replicas
- redis: 1 master + 2 replicas
```

### 환경별 설정

| 환경 | 용도 | 자동매매 |
|-----|------|---------|
| **dev** | 개발/테스트 | 비활성 (Paper Trading) |
| **staging** | 통합 테스트 | Paper Trading (모의 거래) |
| **prod** | 실제 운영 | 활성 (실제 거래) |

---

## API 명세

### 1. 시장 분석 API

```http
GET /api/v1/analysis/sentiment/{symbol}
Response: {
  "symbol": "BTC/USDT",
  "sentiment": "POSITIVE",
  "score": 0.85,
  "sources": [
    {"source": "Reuters", "score": 0.92},
    {"source": "Bloomberg", "score": 0.78}
  ]
}

GET /api/v1/analysis/prediction/{symbol}
Response: {
  "symbol": "BTC/USDT",
  "currentPrice": 45000,
  "predictions": {
    "1h": {"price": 45800, "confidence": 0.75},
    "24h": {"price": 47000, "confidence": 0.62}
  }
}

GET /api/v1/analysis/correlation?symbols=BTC,ETH,NASDAQ
Response: {
  "correlations": {
    "BTC-ETH": 0.92,
    "BTC-NASDAQ": 0.45
  }
}
```

### 2. 트레이딩 전략 API

```http
POST /api/v1/strategies
Request: {
  "name": "Sentiment Strategy",
  "type": "SENTIMENT_BASED",
  "symbols": ["BTC/USDT", "ETH/USDT"],
  "config": {
    "buyThreshold": 0.7,
    "sellThreshold": 0.3,
    "stopLoss": 0.05,
    "takeProfit": 0.15
  }
}

POST /api/v1/strategies/{id}/start
Response: {"status": "RUNNING"}

POST /api/v1/strategies/{id}/stop
Response: {"status": "STOPPED"}

GET /api/v1/strategies/{id}/performance
Response: {
  "totalReturn": 0.45,
  "sharpeRatio": 1.8,
  "maxDrawdown": 0.15,
  "winRate": 0.62
}
```

### 3. 포트폴리오 API

```http
GET /api/v1/portfolio
Response: {
  "totalValue": 12500.50,
  "positions": [
    {
      "symbol": "BTC/USDT",
      "quantity": 0.25,
      "avgBuyPrice": 42000,
      "currentPrice": 45000,
      "unrealizedPnL": 750.00
    }
  ]
}
```

### 4. 백테스팅 API

```http
POST /api/v1/backtest
Request: {
  "strategyId": 123,
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "initialCapital": 10000
}
Response: {
  "totalReturn": 0.45,
  "sharpeRatio": 1.8,
  "maxDrawdown": 0.15,
  "winRate": 0.62,
  "totalTrades": 150
}
```

---

## 다음 단계

1. ✅ Phase 1 구현 시작 (데이터 수집 인프라)
2. ✅ Phase 2 구현 (AI/ML 분석 엔진)
3. ✅ Phase 3 구현 (자동 매매 엔진)
4. ✅ Phase 4 구현 (Telegram Bot + Slack)
5. 📊 통합 테스트 & Paper Trading
6. 🚀 프로덕션 배포

---

**작성일**: 2025-11-08
**버전**: v1.0.0
**작성자**: Trading System Team
