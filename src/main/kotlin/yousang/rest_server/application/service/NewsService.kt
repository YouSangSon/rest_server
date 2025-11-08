package yousang.rest_server.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.application.ports.`in`.*
import yousang.rest_server.application.ports.out.*
import yousang.rest_server.domain.event.NewsCollectedEvent
import yousang.rest_server.domain.event.SentimentAnalyzedEvent
import yousang.rest_server.domain.model.NewsArticle
import yousang.rest_server.domain.model.SentimentType
import java.time.LocalDateTime

/**
 * 뉴스 서비스
 *
 * 뉴스 수집, 감성 분석 통합 서비스
 */
@Service
@Transactional
class NewsService(
    private val newsApiPort: List<NewsApiPort>,
    private val newsRepositoryPort: NewsRepositoryPort,
    private val sentimentAnalysisPort: SentimentAnalysisPort,
    private val eventPublisherPort: EventPublisherPort
) : CollectNewsUseCase, AnalyzeSentimentUseCase, GetNewsArticlesUseCase {

    override fun collectNews(
        keywords: List<String>,
        language: String,
        from: LocalDateTime?,
        to: LocalDateTime?
    ): List<NewsArticle> {
        val allArticles = mutableListOf<NewsArticle>()

        newsApiPort.forEach { api ->
            try {
                val articles = api.fetchNews(keywords, language, from, to)
                val newArticles = articles.filter { article ->
                    newsRepositoryPort.findByUrl(article.url) == null
                }

                if (newArticles.isNotEmpty()) {
                    val saved = newsRepositoryPort.saveAll(newArticles)
                    allArticles.addAll(saved)

                    // 이벤트 발행
                    saved.forEach { article ->
                        eventPublisherPort.publish(
                            NewsCollectedEvent(
                                newsId = article.id!!,
                                source = article.source,
                                title = article.title,
                                url = article.url,
                                relatedSymbols = article.relatedSymbols
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // 로깅만 하고 계속 진행
                println("Failed to collect news from ${api.getSourceName()}: ${e.message}")
            }
        }

        return allArticles
    }

    override fun collectNewsBySymbol(symbol: String): List<NewsArticle> {
        val keywords = extractKeywordsFromSymbol(symbol)
        return collectNews(keywords)
    }

    override fun collectLatestNews(): List<NewsArticle> {
        val cryptoKeywords = listOf("Bitcoin", "Ethereum", "cryptocurrency", "crypto market")
        return collectNews(cryptoKeywords)
    }

    override fun analyzeSentiment(newsArticle: NewsArticle): NewsArticle {
        if (newsArticle.isAnalyzed()) {
            return newsArticle
        }

        val result = sentimentAnalysisPort.analyzeArticle(newsArticle.title, newsArticle.content)
        val analyzed = newsArticle.withSentimentAnalysis(result.score, result.sentimentType)
        val saved = newsRepositoryPort.save(analyzed)

        // 이벤트 발행
        eventPublisherPort.publish(
            SentimentAnalyzedEvent(
                newsId = saved.id!!,
                sentimentScore = result.score,
                sentiment = result.sentimentType,
                relatedSymbols = saved.relatedSymbols
            )
        )

        return saved
    }

    override fun analyzeSentimentBatch(articles: List<NewsArticle>): List<NewsArticle> {
        return articles.map { analyzeSentiment(it) }
    }

    override fun analyzeSentimentScore(text: String): Double {
        val result = sentimentAnalysisPort.analyze(text)
        return result.score
    }

    override fun getAggregateSentiment(symbol: String, hoursBack: Int): AggregatedSentiment {
        val sentimentCounts = newsRepositoryPort.aggregateSentimentBySymbol(symbol, hoursBack)

        val total = sentimentCounts.values.sum()
        if (total == 0) {
            return AggregatedSentiment(
                symbol = symbol,
                averageScore = 0.0,
                sentimentType = SentimentType.NEUTRAL,
                positiveCount = 0,
                neutralCount = 0,
                negativeCount = 0,
                totalArticles = 0
            )
        }

        val positiveCount = sentimentCounts[SentimentType.POSITIVE] ?: 0
        val neutralCount = sentimentCounts[SentimentType.NEUTRAL] ?: 0
        val negativeCount = sentimentCounts[SentimentType.NEGATIVE] ?: 0

        val averageScore = (positiveCount * 0.7 + neutralCount * 0.0 + negativeCount * -0.7) / total
        val dominantSentiment = SentimentType.fromScore(averageScore)

        return AggregatedSentiment(
            symbol = symbol,
            averageScore = averageScore,
            sentimentType = dominantSentiment,
            positiveCount = positiveCount,
            neutralCount = neutralCount,
            negativeCount = negativeCount,
            totalArticles = total
        )
    }

    override fun getNewsById(id: Long): NewsArticle? {
        return newsRepositoryPort.findById(id)
    }

    override fun getNewsBySymbol(symbol: String, limit: Int): List<NewsArticle> {
        return newsRepositoryPort.findBySymbol(symbol, limit)
    }

    override fun getNewsBySentiment(sentiment: SentimentType, limit: Int): List<NewsArticle> {
        return newsRepositoryPort.findBySentiment(sentiment, limit)
    }

    override fun getNewsByDateRange(from: LocalDateTime, to: LocalDateTime, limit: Int): List<NewsArticle> {
        return newsRepositoryPort.findByDateRange(from, to, limit)
    }

    override fun getLatestNews(limit: Int): List<NewsArticle> {
        return newsRepositoryPort.findLatest(limit)
    }

    override fun getUnanalyzedNews(limit: Int): List<NewsArticle> {
        return newsRepositoryPort.findUnanalyzed(limit)
    }

    private fun extractKeywordsFromSymbol(symbol: String): List<String> {
        // BTC/USDT -> Bitcoin
        return when {
            symbol.contains("BTC") -> listOf("Bitcoin", "BTC")
            symbol.contains("ETH") -> listOf("Ethereum", "ETH")
            symbol.contains("BNB") -> listOf("Binance", "BNB")
            else -> listOf(symbol)
        }
    }
}
