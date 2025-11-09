package yousang.rest_server.application.ports.`in`

import yousang.rest_server.domain.model.NewsArticle
import yousang.rest_server.domain.model.SentimentType
import java.time.LocalDateTime

/**
 * 뉴스 수집 Use Case
 */
interface CollectNewsUseCase {
    fun collectNews(
        keywords: List<String>,
        language: String = "en",
        from: LocalDateTime? = null,
        to: LocalDateTime? = null
    ): List<NewsArticle>

    fun collectNewsBySymbol(symbol: String, limit: Int = 10): List<NewsArticle>
}

/**
 * 감성 분석 Use Case
 */
interface AnalyzeSentimentUseCase {
    fun analyzeSentiment(newsArticle: NewsArticle): NewsArticle
    fun getAggregateSentiment(symbol: String, hoursBack: Int): AggregatedSentiment
}

/**
 * 뉴스 조회 Use Case
 */
interface GetNewsArticlesUseCase {
    fun getLatestNews(limit: Int): List<NewsArticle>
    fun getNewsBySymbol(symbol: String, limit: Int): List<NewsArticle>
    fun getNewsByDateRange(from: LocalDateTime, to: LocalDateTime): List<NewsArticle>
}

/**
 * 집계된 감성 분석 결과
 */
data class AggregatedSentiment(
    val symbol: String,
    val averageScore: Double,
    val sentimentType: SentimentType,
    val positiveCount: Int,
    val neutralCount: Int,
    val negativeCount: Int,
    val totalArticles: Int
)
