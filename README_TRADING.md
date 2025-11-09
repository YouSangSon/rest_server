# 자동 매매 시스템 사용 가이드

## 🎯 개요

이 프로젝트는 **완전 자동화된 암호화폐 트레이딩 시스템**입니다. Telegram 봇을 통해 쉽게 제어하고, 다양한 전략을 실행할 수 있습니다.

## 🚀 빠른 시작

### 1. 필수 인프라 설정

```bash
# Docker Compose로 PostgreSQL, MongoDB, Kafka, Redis 실행
docker-compose up -d
```

### 2. 환경 변수 설정

`.env` 파일 생성:

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/trading_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

# MongoDB
MONGODB_URI=mongodb://localhost:27017/trading_db

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Telegram Bot
TELEGRAM_BOT_TOKEN=your_bot_token_from_botfather

# Slack (선택)
SLACK_WEBHOOK_URL=your_slack_webhook_url

# NewsAPI
NEWSAPI_KEY=your_newsapi_key

# Binance
BINANCE_API_KEY=your_binance_api_key
BINANCE_SECRET_KEY=your_binance_secret_key

# Upbit
UPBIT_ACCESS_KEY=your_upbit_access_key
UPBIT_SECRET_KEY=your_upbit_secret_key
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 4. Telegram Bot 설정

1. **BotFather에서 봇 생성**
   ```
   /newbot
   봇 이름 입력: My Trading Bot
   봇 사용자명 입력: mytradingbot
   ```

2. **토큰 복사** 후 `.env`에 설정

3. **웹훅 설정** (애플리케이션 실행 후)
   ```
   GET http://your-domain.com/api/telegram/webhook/setup?url=https://your-domain.com/api/telegram/webhook
   ```

4. **텔레그램에서 봇과 대화 시작**
   ```
   /start
   ```

## 📱 Telegram Bot 사용법

### 메인 메뉴 버튼

- **📈 현재 시황 분석**: BTC, ETH 등 주요 코인 가격, 24h 변동, 뉴스 감성
- **💼 포트폴리오**: 보유 포지션, 손익(P&L), 평가액
- **🤖 자동매매 시작**: 모든 활성 전략 자동 실행 시작 (1분마다)
- **⏸️ 자동매매 중지**: 자동 실행 중지
- **📋 전략 관리**: 등록된 전략 조회 및 즉시 실행
- **⚠️ 리스크 현황**: 포트폴리오 리스크, 알림 확인
- **📰 최신 뉴스**: 암호화폐 관련 뉴스 + 감성 분석
- **⚙️ 설정**: 봇 설정 확인
- **ℹ️ 도움말**: 사용 가이드

### 명령어

```
/start 또는 /menu - 메인 메뉴 표시
/portfolio - 포트폴리오 조회
/market - 시장 분석
/strategies - 전략 관리
/start_trading - 자동매매 시작
/stop_trading - 자동매매 중지
/risk - 리스크 현황
/news - 최신 뉴스
/help - 도움말
```

## 🎯 트레이딩 전략

### 1. Momentum Strategy (모멘텀)
- **로직**: SMA20이 SMA50을 상향 돌파 + RSI < 70 → 매수
- **매도**: SMA20이 SMA50을 하향 돌파 OR RSI > 80
- **적합**: 트렌드 강한 시장

### 2. Mean Reversion (평균회귀)
- **로직**: 가격이 Bollinger Bands 하단 이탈 → 매수
- **매도**: 가격이 상단 돌파
- **적합**: 박스권 시장

### 3. Sentiment-Based (감성 기반)
- **로직**: 뉴스 감성 점수 > 0.5 + 기사 5개 이상 → 매수
- **매도**: 감성 점수 < -0.5
- **적합**: 뉴스에 민감한 코인

### 4. DCA (Dollar Cost Averaging)
- **로직**: 정해진 간격으로 고정 금액 매수
- **매도**: 수동 또는 목표가 도달
- **적합**: 장기 투자

### 5. Grid Trading (그리드)
- **로직**: 가격 구간을 나누어 각 레벨에서 매수/매도
- **적합**: 변동성 높은 시장

### 6. Arbitrage (차익거래)
- **로직**: Binance-Upbit 간 가격 차이 활용
- **적합**: 거래소 간 가격 차이 발생 시

## 🛡️ 리스크 관리

### 자동 보호 기능

1. **포지션 크기 제한**
   - 단일 포지션: 총 자산의 20% 이하

2. **일일 손실 제한**
   - 하루 최대 손실: 5%
   - 초과 시 모든 거래 중지

3. **자동 손절/익절**
   - 기본 손절: -3%
   - 기본 익절: +10%
   - 각 포지션마다 자동 적용

4. **리스크 알림**
   - Critical: 즉시 텔레그램 + Slack 알림
   - High/Medium: 정기 알림

### 리스크 모니터링

```
/risk 명령어로 확인 가능:
- 총 자산
- 총 리스크
- 리스크 비율
- 분산 점수 (HHI 기반)
- 활성 알림
```

## 🔄 자동 실행 스케줄

애플리케이션은 다음 작업을 자동으로 수행합니다:

| 작업 | 주기 | 설명 |
|------|------|------|
| 전략 실행 | 1분 | 모든 활성 전략 체크 및 주문 생성 |
| 시장 데이터 수집 | 30초 | BTC, ETH 가격 업데이트 |
| 뉴스 수집 | 5분 | 최신 뉴스 + 감성 분석 |
| 리스크 체크 | 1분 | 포지션 한도, 일일 손실 확인 |
| 손절/익절 체크 | 10초 | 빠른 응답 필요 |
| 캔들 수집 | 1분 | OHLCV 데이터 저장 |
| 데이터 정리 | 매일 자정 | 30일 이전 데이터 삭제 |

## 🔧 전략 생성 예제

### REST API로 전략 생성

```bash
POST /api/strategies
Content-Type: application/json

{
  "userId": 1,
  "name": "BTC Momentum Strategy",
  "strategyType": "MOMENTUM",
  "symbols": ["BTC/USDT", "ETH/USDT"],
  "exchange": "Binance",
  "parameters": {
    "sma_short": "20",
    "sma_long": "50",
    "rsi_period": "14"
  }
}
```

## 📊 모니터링

### Grafana 대시보드 (선택)

1. **포트폴리오 가치**: 시간별 변화
2. **전략 성과**: 각 전략별 수익률
3. **거래 내역**: 주문 체결 현황
4. **리스크 지표**: VaR, Sharpe Ratio

### Prometheus 메트릭

```
http://localhost:8080/actuator/prometheus
```

## 🚨 알림 설정

### Telegram
- 주문 체결/실패
- 거래 신호 생성
- 리스크 경고
- 손익 실현

### Slack
- 포트폴리오 요약
- 시스템 상태
- Critical 알림

## 🔐 보안 주의사항

1. **API 키 보안**
   - `.env` 파일은 Git에 커밋하지 말 것
   - 프로덕션에서는 AWS Secrets Manager 사용 권장

2. **거래소 API 권한**
   - Binance/Upbit에서 "거래" 권한만 부여
   - "출금" 권한은 절대 부여하지 말 것
   - IP 화이트리스트 설정

3. **Telegram Bot**
   - Bot Token 노출 주의
   - 봇과의 대화는 1:1만 가능하도록 설정

## 🐛 문제 해결

### 자동매매가 시작되지 않음
```bash
# 로그 확인
tail -f logs/spring.log

# 전략이 활성화되어 있는지 확인
/strategies

# 스케줄러 상태 확인 (로그)
💚 [2025-01-09 10:00:00] System health check - Active users: 1
```

### 주문이 체결되지 않음
1. 거래소 API 키 확인
2. 잔고 부족 확인
3. 최소 주문 수량 미달 확인

### Telegram 봇 응답 없음
1. 웹훅 설정 확인
2. Bot Token 확인
3. 애플리케이션 로그 확인

## 📚 추가 자료

- [아키텍처 문서](./TRADING_SYSTEM_ARCHITECTURE.md)
- [API 문서](http://localhost:8080/swagger-ui.html)
- Binance API: https://binance-docs.github.io/apidocs/
- Upbit API: https://docs.upbit.com/

## ⚠️ 면책 조항

**이 시스템은 교육 및 연구 목적으로 제공됩니다.**

- 실제 자금 투자 시 손실 위험이 있습니다
- 과거 성과가 미래 수익을 보장하지 않습니다
- 모든 투자 결정은 본인의 책임입니다
- 프로덕션 사용 전 충분한 백테스팅을 수행하세요

## 📞 지원

문제가 발생하면:
1. GitHub Issues 등록
2. 로그 파일 첨부 (`logs/spring.log`)
3. 환경 정보 제공 (OS, Java 버전 등)

---

**Happy Trading! 🚀📈**
