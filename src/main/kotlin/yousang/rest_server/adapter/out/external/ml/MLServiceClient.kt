package yousang.rest_server.adapter.out.external.ml

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import yousang.rest_server.application.ports.out.SentimentAnalysisPort
import yousang.rest_server.domain.model.SentimentType
import java.math.BigDecimal

/**
 * ML 서비스 클라이언트
 *
 * Python FastAPI 기반 ML 서비스 호출
 */
@Component
class MLServiceClient(
    private val restTemplate: RestTemplate,
    @Value("\${ml.service.url:http://localhost:8000}") private val mlServiceUrl: String
) : SentimentAnalysisPort {

    // ==================== Sentiment Analysis ====================

    override fun analyzeArticle(title: String, content: String): SentimentResult {
        val text = "$title. $content"
        return analyzeSentiment(text)
    }

    fun analyzeSentiment(text: String): SentimentResult {
        try {
            val request = mapOf(
                "text" to text,
                "language" to "en"
            )

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            val response = restTemplate.exchange(
                "$mlServiceUrl/api/ml/analyze-sentiment",
                HttpMethod.POST,
                HttpEntity(request, headers),
                SentimentAnalysisResponse::class.java
            )

            val result = response.body ?: throw IllegalStateException("Empty response from ML service")

            val sentimentType = when (result.sentiment) {
                "POSITIVE" -> SentimentType.POSITIVE
                "NEGATIVE" -> SentimentType.NEGATIVE
                else -> SentimentType.NEUTRAL
            }

            return SentimentResult(
                score = result.score,
                sentimentType = sentimentType,
                confidence = result.confidence
            )

        } catch (e: Exception) {
            println("ML sentiment analysis failed, using fallback: ${e.message}")
            return fallbackSentimentAnalysis(text)
        }
    }

    /**
     * 배치 감성 분석
     */
    fun batchSentimentAnalysis(texts: List<String>): List<SentimentResult> {
        try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            val response = restTemplate.exchange(
                "$mlServiceUrl/api/ml/batch-sentiment",
                HttpMethod.POST,
                HttpEntity(texts, headers),
                BatchSentimentResponse::class.java
            )

            return response.body?.results?.map { result ->
                val sentimentType = when (result.sentiment) {
                    "POSITIVE" -> SentimentType.POSITIVE
                    "NEGATIVE" -> SentimentType.NEGATIVE
                    else -> SentimentType.NEUTRAL
                }

                SentimentResult(
                    score = result.score,
                    sentimentType = sentimentType,
                    confidence = result.confidence
                )
            } ?: emptyList()

        } catch (e: Exception) {
            println("Batch sentiment analysis failed: ${e.message}")
            return texts.map { fallbackSentimentAnalysis(it) }
        }
    }

    // ==================== Price Prediction ====================

    /**
     * LSTM 기반 가격 예측
     */
    fun predictPrice(symbol: String, historicalPrices: List<BigDecimal>): PricePrediction {
        try {
            val request = mapOf(
                "symbol" to symbol,
                "historical_prices" to historicalPrices.map { it.toDouble() }
            )

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            val response = restTemplate.exchange(
                "$mlServiceUrl/api/ml/predict-price",
                HttpMethod.POST,
                HttpEntity(request, headers),
                PricePredictionResponse::class.java
            )

            val result = response.body ?: throw IllegalStateException("Empty prediction response")

            return PricePrediction(
                symbol = result.symbol,
                predictedPrice = result.predicted_price.toBigDecimal(),
                predictedChangePercent = result.predicted_change_percent,
                confidence = result.confidence,
                predictionTime = result.prediction_time
            )

        } catch (e: Exception) {
            println("Price prediction failed: ${e.message}")
            // 폴백: 간단한 이동평균 기반 예측
            val avg = historicalPrices.takeLast(10).reduce { acc, price -> acc + price } / BigDecimal(10)
            return PricePrediction(
                symbol = symbol,
                predictedPrice = avg,
                predictedChangePercent = 0.0,
                confidence = 0.3,
                predictionTime = ""
            )
        }
    }

    /**
     * 다중 스텝 가격 예측 (24시간 등)
     */
    fun predictMultiStep(symbol: String, historicalPrices: List<BigDecimal>, steps: Int = 24): List<BigDecimal> {
        try {
            val request = mapOf(
                "symbol" to symbol,
                "historical_prices" to historicalPrices.map { it.toDouble() }
            )

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            val url = "$mlServiceUrl/api/ml/predict-multi-step?steps=$steps"

            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                HttpEntity(request, headers),
                MultiStepPredictionResponse::class.java
            )

            return response.body?.predictions?.map { it.toBigDecimal() } ?: emptyList()

        } catch (e: Exception) {
            println("Multi-step prediction failed: ${e.message}")
            return emptyList()
        }
    }

    // ==================== Health Check ====================

    fun isHealthy(): Boolean {
        return try {
            val response = restTemplate.getForObject("$mlServiceUrl/health", Map::class.java)
            response?.get("status") == "healthy"
        } catch (e: Exception) {
            false
        }
    }

    // ==================== Fallback Methods ====================

    private fun fallbackSentimentAnalysis(text: String): SentimentResult {
        val textLower = text.lowercase()

        val positiveKeywords = listOf("bull", "bullish", "up", "rise", "gain", "profit", "buy", "long", "positive")
        val negativeKeywords = listOf("bear", "bearish", "down", "fall", "loss", "crash", "sell", "short", "negative")

        val positiveCount = positiveKeywords.count { textLower.contains(it) }
        val negativeCount = negativeKeywords.count { textLower.contains(it) }

        return when {
            positiveCount > negativeCount -> SentimentResult(
                score = 0.5 + (positiveCount * 0.1),
                sentimentType = SentimentType.POSITIVE,
                confidence = 0.5
            )
            negativeCount > positiveCount -> SentimentResult(
                score = -0.5 - (negativeCount * 0.1),
                sentimentType = SentimentType.NEGATIVE,
                confidence = 0.5
            )
            else -> SentimentResult(
                score = 0.0,
                sentimentType = SentimentType.NEUTRAL,
                confidence = 0.5
            )
        }
    }
}

// ==================== Response Models ====================

data class SentimentAnalysisResponse(
    val sentiment: String,
    val score: Double,
    val confidence: Double
)

data class BatchSentimentResponse(
    val total: Int,
    val results: List<SentimentAnalysisResponse>,
    val average_score: Double
)

data class PricePredictionResponse(
    val symbol: String,
    val predicted_price: Double,
    val predicted_change_percent: Double,
    val confidence: Double,
    val prediction_time: String
)

data class MultiStepPredictionResponse(
    val symbol: String,
    val predictions: List<Double>,
    val prediction_time: String
)

// ==================== Domain Models ====================

data class SentimentResult(
    val score: Double,
    val sentimentType: SentimentType,
    val confidence: Double
)

data class PricePrediction(
    val symbol: String,
    val predictedPrice: BigDecimal,
    val predictedChangePercent: Double,
    val confidence: Double,
    val predictionTime: String
)
