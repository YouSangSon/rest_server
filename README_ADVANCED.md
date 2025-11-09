# 🚀 고급 기능 가이드

## 새로 추가된 프로 레벨 기능들

### 1. 고급 기술적 지표 📊

#### MACD (Moving Average Convergence Divergence)
```kotlin
val macd = advancedTechnicalAnalysisService.calculateMACD(candles)
val signal = advancedTechnicalAnalysisService.detectMACDSignal(macd)
// BUY: MACD가 Signal을 상향 돌파
// SELL: MACD가 Signal을 하향 돌파
```

#### Stochastic Oscillator
```kotlin
val stochastic = advancedTechnicalAnalysisService.calculateStochastic(candles)
// %K < 20: 과매도 (매수 기회)
// %K > 80: 과매수 (매도 고려)
```

#### ATR (Average True Range) - 변동성 측정
```kotlin
val atr = advancedTechnicalAnalysisService.calculateATR(candles)
// ATR이 높을수록 변동성이 크고 리스크 높음
```

#### ADX (Average Directional Index) - 트렌드 강도
```kotlin
val adx = advancedTechnicalAnalysisService.calculateADX(candles)
// ADX > 25: 강한 트렌드
// ADX > 50: 매우 강한 트렌드
```

#### Fibonacci Retracement
```kotlin
val fibonacci = advancedTechnicalAnalysisService.autoFibonacci(candles)
// 38.2%, 50%, 61.8% 레벨에서 반등/저항 예상
```

#### Ichimoku Cloud (일목균형표)
```kotlin
val ichimoku = advancedTechnicalAnalysisService.calculateIchimoku(candles)
// Tenkan-sen > Kijun-sen: 상승 신호
// 가격 > Cloud: 강세장
```

#### Volume 분석
```kotlin
val obv = advancedTechnicalAnalysisService.calculateOBV(candles)
val vwap = advancedTechnicalAnalysisService.calculateVWAP(candles)
// OBV 상승 + 가격 상승: 강한 매수세
// 가격 < VWAP: 저평가 (매수 기회)
```

### 2. 백테스팅 엔진 🧪

**과거 데이터로 전략 성과 시뮬레이션**

```kotlin
val result = backtestingService.runBacktest(
    strategyId = 1L,
    symbol = "BTC/USDT",
    exchange = "Binance",
    startDate = LocalDateTime.now().minusMonths(6),
    endDate = LocalDateTime.now(),
    initialCapital = BigDecimal(10000)
)

println("Total Return: ${result.totalReturn}%")
println("Win Rate: ${result.winRate}%")
println("Sharpe Ratio: ${result.sharpeRatio}")
println("Max Drawdown: ${result.maxDrawdown}%")
println("Profit Factor: ${result.profitFactor}")
```

**백테스트 결과 분석:**
- **Total Return**: 총 수익률 (%)
- **Win Rate**: 승률
- **Sharpe Ratio**: 위험 대비 수익률 (1.0 이상이 좋음)
- **Max Drawdown**: 최대 낙폭 (작을수록 좋음)
- **Profit Factor**: 평균 이익 / 평균 손실 (1.5 이상 권장)

### 3. ML 가격 예측 🤖

**Python FastAPI + LSTM 기반 가격 예측**

#### ML 서비스 실행
```bash
cd ml_service
pip install -r requirements.txt
python main.py
```

또는 Docker:
```bash
docker-compose up ml-service
```

#### Kotlin에서 ML 서비스 호출
```kotlin
// 가격 예측
val prediction = mlServiceClient.predictPrice(
    symbol = "BTC/USDT",
    historicalPrices = last60Prices
)

println("Predicted Price: ${prediction.predictedPrice}")
println("Expected Change: ${prediction.predictedChangePercent}%")
println("Confidence: ${prediction.confidence}")

// 24시간 다중 스텝 예측
val predictions = mlServiceClient.predictMultiStep(
    symbol = "BTC/USDT",
    historicalPrices = last60Prices,
    steps = 24
)
```

#### FinBERT 감성 분석
```kotlin
val sentiment = mlServiceClient.analyzeSentiment(
    "Bitcoin surges to new all-time high as institutions pile in"
)

println("Sentiment: ${sentiment.sentimentType}") // POSITIVE
println("Score: ${sentiment.score}") // 0.85
```

### 4. 고급 주문 전략 💎

#### TWAP (Time-Weighted Average Price)
**대량 주문을 시간에 걸쳐 분할 실행**

```kotlin
val result = advancedOrderService.executeTWAP(
    userId = 1L,
    symbol = "BTC/USDT",
    exchange = "Binance",
    side = OrderSide.BUY,
    totalQuantity = BigDecimal("10.0"), // 10 BTC
    durationMinutes = 60, // 1시간
    sliceCount = 12 // 5분마다 실행
)
// 대량 주문을 12번 나눠서 5분 간격으로 실행
```

**장점:**
- 시장 충격 최소화
- 평균 진입가 개선
- 슬리피지 감소

#### Iceberg Order (빙산 주문)
**대량 주문을 숨기고 일부만 공개**

```kotlin
val result = advancedOrderService.executeIceberg(
    userId = 1L,
    symbol = "ETH/USDT",
    exchange = "Binance",
    side = OrderSide.BUY,
    totalQuantity = BigDecimal("100.0"), // 100 ETH
    visibleQuantity = BigDecimal("5.0"), // 5 ETH씩만 공개
    limitPrice = BigDecimal("2500.00")
)
// 100 ETH를 사려는 의도를 숨기고 5 ETH씩만 노출
```

#### Trailing Stop (추적 손절)
**가격이 유리하게 움직이면 손절가도 자동 이동**

```kotlin
val order = advancedOrderService.createTrailingStop(
    userId = 1L,
    symbol = "BTC/USDT",
    exchange = "Binance",
    quantity = BigDecimal("1.0"),
    trailingPercent = 3.0 // 3% 되돌림 시 매도
)

// 가격 업데이트마다 호출 (WebSocket에서)
advancedOrderService.updateTrailingStops(
    symbol = "BTC/USDT",
    exchange = "Binance",
    currentPrice = latestPrice
)
```

**예시:**
- 진입: $40,000
- 가격 상승: $45,000
- Trailing Stop: $43,650 (3% 아래)
- 가격이 $43,650 밑으로 내려가면 자동 매도
- **수익 보호 + 추가 상승 기회 활용**

#### Kelly Criterion 포지션 사이징
**통계적으로 최적화된 투자 금액 계산**

```kotlin
val optimalPosition = advancedOrderService.calculateKellyPosition(
    winRate = 60.0, // 승률 60%
    avgWinPercent = 15.0, // 평균 이익 15%
    avgLossPercent = 10.0, // 평균 손실 10%
    totalCapital = BigDecimal(10000)
)
// 최적 투자 금액 계산 (파산 리스크 최소화)
```

### 5. WebSocket 실시간 스트리밍 ⚡

**클라이언트에게 실시간 가격 푸시**

#### JavaScript 클라이언트 예제
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/market-data');

ws.onopen = () => {
  // 구독 요청
  ws.send(JSON.stringify({
    action: 'subscribe',
    symbols: ['BTC/USDT:Binance', 'ETH/USDT:Binance']
  }));
};

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);

  if (data.action === 'market_data') {
    console.log(`${data.symbol}: $${data.data.price}`);
    console.log(`24h Change: ${data.data.priceChangePercent24h}%`);
  }
};
```

**특징:**
- 1초마다 실시간 가격 업데이트
- 여러 심볼 동시 구독 가능
- 낮은 지연시간 (<100ms)

### 6. 멀티 타임프레임 분석 🎯

**여러 시간대를 동시 분석하여 신뢰도 높은 신호 생성**

```kotlin
val mtfResult = multiTimeframeAnalysisService.analyzeMultiTimeframeTrend(
    symbol = "BTC/USDT",
    exchange = "Binance",
    timeframes = listOf(
        CandleInterval.FIFTEEN_MINUTES,
        CandleInterval.ONE_HOUR,
        CandleInterval.FOUR_HOURS,
        CandleInterval.ONE_DAY
    )
)

println("Overall Trend: ${mtfResult.overallTrend}")
println("Confluence Score: ${mtfResult.confluenceScore}%")
println("Recommendation: ${mtfResult.recommendation}")

// 예시 출력:
// Overall Trend: BULLISH
// Confluence Score: 85%
// Recommendation: STRONG BULLISH - High confidence setup
```

**Confluence Score 해석:**
- **80-100%**: 모든 시간대 일치 → 매우 강한 신호
- **60-80%**: 대부분 일치 → 좋은 신호
- **40-60%**: 약한 신호 → 확인 대기
- **<40%**: 신호 불일치 → 관망

#### 타임프레임 간 발산 감지
```kotlin
val divergence = multiTimeframeAnalysisService.detectDivergence(
    symbol = "BTC/USDT",
    exchange = "Binance",
    shortTF = CandleInterval.FIFTEEN_MINUTES,
    longTF = CandleInterval.FOUR_HOURS
)

if (divergence.hasDivergence) {
    println("Warning: ${divergence.type}")
    println(divergence.description)
}
```

**Divergence 패턴:**
- **Bullish Divergence**: 단기 상승, 장기 하락 → 반전 신호
- **Bearish Divergence**: 단기 하락, 장기 상승 → 조정 임박

#### 최적 진입점 찾기
```kotlin
val optimalEntry = multiTimeframeAnalysisService.findOptimalEntry(
    symbol = "BTC/USDT",
    exchange = "Binance",
    targetSide = OrderSide.BUY
)

if (optimalEntry.isAligned && optimalEntry.confidence > 70) {
    println("Great entry opportunity!")
    println("Suggested Entry: $${optimalEntry.suggestedEntryPrice}")
    println(optimalEntry.reasoning)

    // 주문 실행
    tradingService.placeOrder(...)
}
```

## 전략 조합 예시 💪

### 1. 프로 모멘텀 전략
```kotlin
// 1. 멀티 타임프레임 확인
val mtf = multiTimeframeAnalysisService.analyzeMultiTimeframeTrend(...)
if (mtf.confluenceScore < 70) return // 신뢰도 낮으면 패스

// 2. MACD + Stochastic 확인
val macd = advancedTechnicalAnalysisService.calculateMACD(candles)
val stochastic = advancedTechnicalAnalysisService.calculateStochastic(candles)

val macdSignal = advancedTechnicalAnalysisService.detectMACDSignal(macd)
val stochasticSignal = advancedTechnicalAnalysisService.detectStochasticSignal(stochastic)

if (macdSignal == TradingSignal.BUY && stochasticSignal == TradingSignal.BUY) {
    // 3. Kelly Criterion으로 포지션 크기 계산
    val position = advancedOrderService.calculateKellyPosition(...)

    // 4. TWAP로 진입
    advancedOrderService.executeTWAP(
        totalQuantity = position,
        durationMinutes = 30
    )

    // 5. Trailing Stop 설정
    advancedOrderService.createTrailingStop(
        quantity = position,
        trailingPercent = 3.0
    )
}
```

### 2. AI 기반 전략
```kotlin
// 1. ML 가격 예측
val prediction = mlServiceClient.predictPrice(symbol, historicalPrices)

if (prediction.confidence > 0.7 && prediction.predictedChangePercent > 2.0) {
    // 2. 뉴스 감성 분석
    val sentiment = newsService.getAggregateSentiment(symbol, 24)

    if (sentiment.sentimentType == SentimentType.POSITIVE) {
        // 3. 멀티 타임프레임 확인
        val mtf = multiTimeframeAnalysisService.analyzeMultiTimeframeTrend(...)

        if (mtf.overallTrend == TrendDirection.BULLISH) {
            // 강한 매수 신호!
            executeBuyOrder(...)
        }
    }
}
```

## 성능 최적화 팁 ⚡

1. **백테스팅으로 전략 검증**
   - 최소 6개월 데이터로 테스트
   - Sharpe Ratio > 1.0 목표
   - Max Drawdown < 20% 유지

2. **Kelly Criterion 사용**
   - 과도한 레버리지 방지
   - 장기적 자본 증식

3. **TWAP/Iceberg로 슬리피지 감소**
   - 대량 주문 시 필수
   - 평균 진입가 개선

4. **멀티 타임프레임 필터링**
   - 모든 시간대 일치 시에만 진입
   - 승률 10-20% 향상

5. **Trailing Stop으로 수익 보호**
   - 큰 수익 지키기
   - 감정적 판단 배제

## 모니터링 대시보드 📈

### Grafana + Prometheus 설정
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'trading-bot'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
```

### 주요 메트릭
- 실시간 P&L
- 전략별 성과
- 리스크 지표
- API 응답 시간
- 주문 체결률

## 다음 단계 🚀

1. **전략 최적화**: 백테스팅으로 파라미터 튜닝
2. **ML 모델 학습**: 실제 LSTM 모델 훈련
3. **웹 대시보드**: React로 시각화 UI 구축
4. **알림 확장**: Discord, Email 추가
5. **고빈도 거래**: 마이크로초 최적화

---

**경고:** 이 기능들은 매우 강력하지만 더 큰 리스크를 수반합니다. 항상 소액으로 먼저 테스트하세요!
