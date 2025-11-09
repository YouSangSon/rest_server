"""
ML 가격 예측 서비스 (FastAPI)

LSTM 기반 가격 예측 및 FinBERT 감성 분석
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import numpy as np
import pandas as pd
from datetime import datetime
import torch
import torch.nn as nn
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import uvicorn

app = FastAPI(title="ML Trading Service", version="1.0.0")

# ==================== Models ====================

class PricePredictionRequest(BaseModel):
    symbol: str
    historical_prices: List[float]  # 최근 60개 가격
    features: Optional[dict] = None

class PricePredictionResponse(BaseModel):
    symbol: str
    predicted_price: float
    predicted_change_percent: float
    confidence: float
    prediction_time: str

class SentimentAnalysisRequest(BaseModel):
    text: str
    language: str = "en"

class SentimentAnalysisResponse(BaseModel):
    sentiment: str  # POSITIVE, NEGATIVE, NEUTRAL
    score: float  # -1.0 ~ 1.0
    confidence: float

# ==================== LSTM Price Prediction Model ====================

class LSTMPricePredictor(nn.Module):
    def __init__(self, input_size=1, hidden_size=50, num_layers=2, output_size=1):
        super(LSTMPricePredictor, self).__init__()
        self.hidden_size = hidden_size
        self.num_layers = num_layers

        self.lstm = nn.LSTM(input_size, hidden_size, num_layers, batch_first=True)
        self.fc = nn.Linear(hidden_size, output_size)

    def forward(self, x):
        h0 = torch.zeros(self.num_layers, x.size(0), self.hidden_size)
        c0 = torch.zeros(self.num_layers, x.size(0), self.hidden_size)

        out, _ = self.lstm(x, (h0, c0))
        out = self.fc(out[:, -1, :])
        return out

# 모델 초기화 (실제로는 훈련된 모델 로드)
lstm_model = LSTMPricePredictor()
# lstm_model.load_state_dict(torch.load('lstm_model.pth'))  # 실제 모델 로드
lstm_model.eval()

# FinBERT 모델 로드
try:
    finbert_tokenizer = AutoTokenizer.from_pretrained("ProsusAI/finbert")
    finbert_model = AutoModelForSequenceClassification.from_pretrained("ProsusAI/finbert")
    finbert_available = True
except Exception as e:
    print(f"FinBERT not available: {e}")
    finbert_available = False

# ==================== Price Prediction Endpoints ====================

@app.post("/api/ml/predict-price", response_model=PricePredictionResponse)
async def predict_price(request: PricePredictionRequest):
    """
    LSTM 기반 가격 예측
    """
    try:
        if len(request.historical_prices) < 60:
            raise HTTPException(
                status_code=400,
                detail="Need at least 60 historical prices for prediction"
            )

        # 데이터 정규화
        prices = np.array(request.historical_prices)
        mean_price = np.mean(prices)
        std_price = np.std(prices)
        normalized_prices = (prices - mean_price) / std_price

        # LSTM 입력 형태로 변환
        sequence = normalized_prices[-60:].reshape(1, 60, 1)
        sequence_tensor = torch.FloatTensor(sequence)

        # 예측
        with torch.no_grad():
            normalized_prediction = lstm_model(sequence_tensor).item()

        # 역정규화
        predicted_price = normalized_prediction * std_price + mean_price

        # 변화율 계산
        current_price = request.historical_prices[-1]
        change_percent = ((predicted_price - current_price) / current_price) * 100

        # Confidence 계산 (간단한 버전 - 실제로는 더 정교한 계산 필요)
        volatility = std_price / mean_price
        confidence = max(0.5, 1.0 - volatility)

        return PricePredictionResponse(
            symbol=request.symbol,
            predicted_price=round(predicted_price, 2),
            predicted_change_percent=round(change_percent, 2),
            confidence=round(confidence, 2),
            prediction_time=datetime.now().isoformat()
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")

@app.post("/api/ml/predict-multi-step")
async def predict_multi_step(request: PricePredictionRequest, steps: int = 24):
    """
    다중 스텝 예측 (예: 24시간 후까지)
    """
    try:
        predictions = []
        current_prices = list(request.historical_prices)

        for _ in range(steps):
            # 현재까지의 가격으로 다음 가격 예측
            prices = np.array(current_prices[-60:])
            mean_price = np.mean(prices)
            std_price = np.std(prices)
            normalized_prices = (prices - mean_price) / std_price

            sequence = normalized_prices.reshape(1, -1, 1)
            sequence_tensor = torch.FloatTensor(sequence)

            with torch.no_grad():
                normalized_pred = lstm_model(sequence_tensor).item()

            predicted_price = normalized_pred * std_price + mean_price
            predictions.append(predicted_price)
            current_prices.append(predicted_price)

        return {
            "symbol": request.symbol,
            "predictions": [round(p, 2) for p in predictions],
            "prediction_time": datetime.now().isoformat()
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Multi-step prediction failed: {str(e)}")

# ==================== Sentiment Analysis Endpoints ====================

@app.post("/api/ml/analyze-sentiment", response_model=SentimentAnalysisResponse)
async def analyze_sentiment(request: SentimentAnalysisRequest):
    """
    FinBERT 기반 감성 분석
    """
    if not finbert_available:
        # FinBERT 없으면 간단한 키워드 기반 분석
        return simple_sentiment_analysis(request.text)

    try:
        # FinBERT 토큰화
        inputs = finbert_tokenizer(
            request.text,
            return_tensors="pt",
            truncation=True,
            max_length=512,
            padding=True
        )

        # 예측
        with torch.no_grad():
            outputs = finbert_model(**inputs)
            predictions = torch.nn.functional.softmax(outputs.logits, dim=-1)

        # FinBERT 레이블: [positive, negative, neutral]
        sentiment_scores = predictions[0].tolist()
        positive_score = sentiment_scores[0]
        negative_score = sentiment_scores[1]
        neutral_score = sentiment_scores[2]

        # 최종 감성 결정
        max_score = max(sentiment_scores)
        if max_score == positive_score:
            sentiment = "POSITIVE"
            score = positive_score
        elif max_score == negative_score:
            sentiment = "NEGATIVE"
            score = -negative_score
        else:
            sentiment = "NEUTRAL"
            score = 0.0

        return SentimentAnalysisResponse(
            sentiment=sentiment,
            score=round(score, 3),
            confidence=round(max_score, 3)
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Sentiment analysis failed: {str(e)}")

def simple_sentiment_analysis(text: str) -> SentimentAnalysisResponse:
    """
    간단한 키워드 기반 감성 분석 (FinBERT 대체)
    """
    text_lower = text.lower()

    positive_keywords = [
        'bull', 'bullish', 'up', 'rise', 'gain', 'profit', 'surge', 'rally',
        'buy', 'long', 'moon', 'pump', 'positive', 'good', 'great', 'excellent'
    ]

    negative_keywords = [
        'bear', 'bearish', 'down', 'fall', 'loss', 'crash', 'dump', 'drop',
        'sell', 'short', 'negative', 'bad', 'terrible', 'decline', 'plunge'
    ]

    positive_count = sum(1 for word in positive_keywords if word in text_lower)
    negative_count = sum(1 for word in negative_keywords if word in text_lower)

    if positive_count > negative_count:
        sentiment = "POSITIVE"
        score = min(0.9, 0.5 + (positive_count * 0.1))
    elif negative_count > positive_count:
        sentiment = "NEGATIVE"
        score = max(-0.9, -0.5 - (negative_count * 0.1))
    else:
        sentiment = "NEUTRAL"
        score = 0.0

    return SentimentAnalysisResponse(
        sentiment=sentiment,
        score=round(score, 3),
        confidence=0.6  # 키워드 기반이므로 낮은 신뢰도
    )

@app.post("/api/ml/batch-sentiment")
async def batch_sentiment_analysis(texts: List[str]):
    """
    배치 감성 분석
    """
    results = []
    for text in texts:
        request = SentimentAnalysisRequest(text=text)
        result = await analyze_sentiment(request)
        results.append(result)

    return {
        "total": len(results),
        "results": results,
        "average_score": sum(r.score for r in results) / len(results) if results else 0.0
    }

# ==================== Feature Engineering ====================

@app.post("/api/ml/extract-features")
async def extract_features(prices: List[float], volumes: List[float]):
    """
    가격/거래량 데이터에서 ML 피처 추출
    """
    try:
        df = pd.DataFrame({
            'price': prices,
            'volume': volumes
        })

        # 기술적 지표 계산
        df['returns'] = df['price'].pct_change()
        df['volatility'] = df['returns'].rolling(window=20).std()
        df['sma_20'] = df['price'].rolling(window=20).mean()
        df['sma_50'] = df['price'].rolling(window=50).mean()

        # RSI
        delta = df['price'].diff()
        gain = delta.where(delta > 0, 0).rolling(window=14).mean()
        loss = -delta.where(delta < 0, 0).rolling(window=14).mean()
        rs = gain / loss
        df['rsi'] = 100 - (100 / (1 + rs))

        # Volume features
        df['volume_sma'] = df['volume'].rolling(window=20).mean()
        df['volume_ratio'] = df['volume'] / df['volume_sma']

        features = {
            'returns': df['returns'].iloc[-1],
            'volatility': df['volatility'].iloc[-1],
            'sma_20': df['sma_20'].iloc[-1],
            'sma_50': df['sma_50'].iloc[-1],
            'rsi': df['rsi'].iloc[-1],
            'volume_ratio': df['volume_ratio'].iloc[-1]
        }

        return {
            "features": {k: round(float(v), 4) if pd.notna(v) else None for k, v in features.items()}
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Feature extraction failed: {str(e)}")

# ==================== Health Check ====================

@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "lstm_loaded": lstm_model is not None,
        "finbert_available": finbert_available,
        "timestamp": datetime.now().isoformat()
    }

@app.get("/")
async def root():
    return {
        "service": "ML Trading Service",
        "version": "1.0.0",
        "endpoints": [
            "/api/ml/predict-price",
            "/api/ml/predict-multi-step",
            "/api/ml/analyze-sentiment",
            "/api/ml/batch-sentiment",
            "/api/ml/extract-features",
            "/health"
        ]
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
