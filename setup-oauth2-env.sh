#!/bin/bash

# OAuth2 소셜 로그인 환경 변수 설정 스크립트
# 사용법: source setup-oauth2-env.sh

echo "========================================="
echo "OAuth2 Environment Variables Setup"
echo "========================================="
echo ""

# .env 파일이 있는지 확인
if [ -f .env ]; then
    echo "✓ Found .env file. Loading variables..."
    export $(cat .env | grep -v '^#' | xargs)
    echo "✓ Variables loaded from .env file"
else
    echo "⚠ .env file not found. Creating template..."
    cat > .env << 'ENVEOF'
# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Naver OAuth2
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret

# Kakao OAuth2
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret

# Database
DB_URL=jdbc:postgresql://localhost:5432/rest_dev
DB_USERNAME=postgres
DB_PASSWORD=postgres

# MongoDB
MONGODB_URI=mongodb://localhost:27017/rest_server
MONGODB_DATABASE=rest_server

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_HEALTH_ENABLED=true

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_CONSUMER_GROUP=rest-server-group

# JWT
JWT_SECRET=your-very-secure-secret-key-that-is-at-least-256-bits-long-for-production-use
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Rate Limiting
RATE_LIMIT_ENABLED=true
RATE_LIMIT_RPM=100

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
ENVEOF
    echo "✓ Created .env template file"
    echo ""
    echo "⚠ Please edit .env file with your actual OAuth2 credentials:"
    echo "   1. Get Google credentials from: https://console.cloud.google.com/"
    echo "   2. Get Naver credentials from: https://developers.naver.com/"
    echo "   3. Get Kakao credentials from: https://developers.kakao.com/"
    echo ""
    echo "   Then run: source setup-oauth2-env.sh"
    exit 1
fi

# 환경 변수 확인
echo ""
echo "========================================="
echo "Current OAuth2 Configuration:"
echo "========================================="
echo ""

# Google
echo "📧 Google OAuth2:"
if [ "$GOOGLE_CLIENT_ID" = "your-google-client-id" ] || [ -z "$GOOGLE_CLIENT_ID" ]; then
    echo "   ❌ Not configured"
else
    echo "   ✓ Client ID: ${GOOGLE_CLIENT_ID:0:20}..."
    echo "   ✓ Client Secret: ${GOOGLE_CLIENT_SECRET:0:10}..."
fi
echo ""

# Naver
echo "🟢 Naver OAuth2:"
if [ "$NAVER_CLIENT_ID" = "your-naver-client-id" ] || [ -z "$NAVER_CLIENT_ID" ]; then
    echo "   ❌ Not configured"
else
    echo "   ✓ Client ID: ${NAVER_CLIENT_ID:0:20}..."
    echo "   ✓ Client Secret: ${NAVER_CLIENT_SECRET:0:10}..."
fi
echo ""

# Kakao
echo "💬 Kakao OAuth2:"
if [ "$KAKAO_CLIENT_ID" = "your-kakao-client-id" ] || [ -z "$KAKAO_CLIENT_ID" ]; then
    echo "   ❌ Not configured"
else
    echo "   ✓ Client ID: ${KAKAO_CLIENT_ID:0:20}..."
    echo "   ✓ Client Secret: ${KAKAO_CLIENT_SECRET:0:10}..."
fi
echo ""

echo "========================================="
echo "Infrastructure Configuration:"
echo "========================================="
echo ""
echo "🗄️  PostgreSQL: $DB_URL"
echo "📊 MongoDB: $MONGODB_URI"
echo "🔴 Redis: $REDIS_HOST:$REDIS_PORT"
echo "📨 Kafka: $KAFKA_BOOTSTRAP_SERVERS"
echo ""

echo "========================================="
echo "✓ Environment variables loaded!"
echo "========================================="
echo ""
echo "Now you can run:"
echo "  - ./gradlew bootRun           (Run application locally)"
echo "  - docker-compose up -d        (Run with Docker)"
echo ""
