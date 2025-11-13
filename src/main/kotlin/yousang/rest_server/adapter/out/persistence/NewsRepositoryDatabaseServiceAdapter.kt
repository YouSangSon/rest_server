package yousang.rest_server.adapter.out.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import yousang.rest_server.adapter.out.database.DatabaseServiceClient
import yousang.rest_server.application.ports.out.NewsRepositoryPort
import yousang.rest_server.domain.model.NewsArticle
import yousang.rest_server.domain.model.SentimentType
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

/**
 * News Repository - Database Service 기반 구현
 *
 * MongoDB를 통해 뉴스 기사 저장/조회
 */
@Component
@Primary
class NewsRepositoryDatabaseServiceAdapter(
    private val databaseServiceClient: DatabaseServiceClient,
    private val objectMapper: ObjectMapper
) : NewsRepositoryPort {

    companion object {
        const val COLLECTION_NEWS = "news_articles"
        private val idGenerator = AtomicLong(System.currentTimeMillis())
    }

    override fun save(newsArticle: NewsArticle): NewsArticle {
        val id = newsArticle.id ?: idGenerator.incrementAndGet()
        val document = newsArticle.copy(id = id).toDocument()

        val response = databaseServiceClient.create(
            collection = COLLECTION_NEWS,
            document = document,
            databaseType = DatabaseServiceClient.DB_MONGODB
        )

        return if (response.success) {
            response.data?.let { objectMapper.convertValue(it, NewsArticleDocument::class.java) }?.toDomain()
                ?: newsArticle
        } else {
            throw IllegalStateException("Failed to save news article: ${response.error?.message}")
        }
    }

    override fun saveAll(articles: List<NewsArticle>): List<NewsArticle> {
        if (articles.isEmpty()) return emptyList()

        val documents = articles.map { article ->
            val id = article.id ?: idGenerator.incrementAndGet()
            article.copy(id = id).toDocument()
        }

        val response = databaseServiceClient.bulkInsert(
            collection = COLLECTION_NEWS,
            documents = documents,
            databaseType = DatabaseServiceClient.DB_MONGODB
        )

        return if (response.success && response.data != null) {
            articles.mapIndexed { index, article ->
                article.copy(id = response.data.insertedIds.getOrNull(index)?.toLongOrNull() ?: article.id)
            }
        } else {
            throw IllegalStateException("Failed to save articles: ${response.error?.message}")
        }
    }

    override fun findById(id: Long): NewsArticle? {
        val response = databaseServiceClient.findById(
            collection = COLLECTION_NEWS,
            id = id.toString(),
            databaseType = DatabaseServiceClient.DB_MONGODB,
            responseType = NewsArticleDocument::class.java
        )

        return response?.data?.let {
            objectMapper.convertValue(it, NewsArticleDocument::class.java).toDomain()
        }
    }

    override fun findByUrl(url: String): NewsArticle? {
        val filter = mapOf("url" to url)

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION_NEWS,
            filter = filter,
            limit = 1,
            databaseType = DatabaseServiceClient.DB_MONGODB
        )

        return response.data?.firstOrNull()?.let {
            objectMapper.convertValue(it, NewsArticleDocument::class.java).toDomain()
        }
    }

    override fun findByPublishedAtBetween(from: LocalDateTime, to: LocalDateTime, limit: Int): List<NewsArticle> {
        val filter = mapOf(
            "publishedAt" to mapOf(
                "\$gte" to from.toString(),
                "\$lte" to to.toString()
            )
        )

        val sort = mapOf("publishedAt" to -1) // 최신순

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION_NEWS,
            filter = filter,
            sort = sort,
            limit = limit,
            databaseType = DatabaseServiceClient.DB_MONGODB
        )

        return response.data?.map {
            objectMapper.convertValue(it, NewsArticleDocument::class.java).toDomain()
        } ?: emptyList()
    }

    override fun findByRelatedSymbolsContaining(symbol: String, limit: Int): List<NewsArticle> {
        val filter = mapOf("relatedSymbols" to mapOf("\$in" to listOf(symbol)))

        val sort = mapOf("publishedAt" to -1)

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION_NEWS,
            filter = filter,
            sort = sort,
            limit = limit,
            databaseType = DatabaseServiceClient.DB_MONGODB
        )

        return response.data?.map {
            objectMapper.convertValue(it, NewsArticleDocument::class.java).toDomain()
        } ?: emptyList()
    }

    override fun findUnanalyzed(limit: Int): List<NewsArticle> {
        val filter = mapOf(
            "\$or" to listOf(
                mapOf("sentiment" to null),
                mapOf("sentiment" to mapOf("\$exists" to false))
            )
        )

        val sort = mapOf("publishedAt" to -1)

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION_NEWS,
            filter = filter,
            sort = sort,
            limit = limit,
            databaseType = DatabaseServiceClient.DB_MONGODB
        )

        return response.data?.map {
            objectMapper.convertValue(it, NewsArticleDocument::class.java).toDomain()
        } ?: emptyList()
    }

    override fun findAll(limit: Int): List<NewsArticle> {
        val sort = mapOf("publishedAt" to -1)

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION_NEWS,
            sort = sort,
            limit = limit,
            databaseType = DatabaseServiceClient.DB_MONGODB
        )

        return response.data?.map {
            objectMapper.convertValue(it, NewsArticleDocument::class.java).toDomain()
        } ?: emptyList()
    }

    override fun aggregateSentimentBySymbol(symbol: String, hoursBack: Int): Map<SentimentType, Int> {
        val from = LocalDateTime.now().minusHours(hoursBack.toLong())

        val filter = mapOf(
            "relatedSymbols" to mapOf("\$in" to listOf(symbol)),
            "publishedAt" to mapOf("\$gte" to from.toString()),
            "sentiment" to mapOf("\$exists" to true, "\$ne" to null)
        )

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION_NEWS,
            filter = filter,
            limit = 1000,
            databaseType = DatabaseServiceClient.DB_MONGODB
        )

        val articles = response.data?.map {
            objectMapper.convertValue(it, NewsArticleDocument::class.java).toDomain()
        } ?: emptyList()

        val sentimentCounts = mutableMapOf<SentimentType, Int>()
        articles.forEach { article ->
            article.sentiment?.let { sentiment ->
                sentimentCounts[sentiment] = sentimentCounts.getOrDefault(sentiment, 0) + 1
            }
        }

        return sentimentCounts
    }

    override fun deleteOlderThan(date: LocalDateTime): Int {
        val filter = mapOf("publishedAt" to mapOf("\$lt" to date.toString()))

        val response = databaseServiceClient.deleteMany(
            collection = COLLECTION_NEWS,
            filter = filter,
            databaseType = DatabaseServiceClient.DB_MONGODB
        )

        return response.data?.deletedCount ?: 0
    }
}

// ==================== Document Model ====================

data class NewsArticleDocument(
    val _id: String? = null,
    val id: Long,
    val source: String,
    val author: String? = null,
    val title: String,
    val description: String,
    val content: String,
    val url: String,
    val publishedAt: String, // ISO DateTime String
    val sentimentScore: Double? = null,
    val sentiment: String? = null,
    val relatedSymbols: List<String> = emptyList(),
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
) {
    fun toDomain(): NewsArticle {
        return NewsArticle(
            id = id,
            source = source,
            author = author,
            title = title,
            description = description,
            content = content,
            url = url,
            publishedAt = LocalDateTime.parse(publishedAt),
            sentimentScore = sentimentScore,
            sentiment = sentiment?.let { SentimentType.valueOf(it) },
            relatedSymbols = relatedSymbols
        )
    }
}

fun NewsArticle.toDocument(): NewsArticleDocument {
    return NewsArticleDocument(
        id = this.id ?: 0L,
        source = this.source,
        author = this.author,
        title = this.title,
        description = this.description,
        content = this.content,
        url = this.url,
        publishedAt = this.publishedAt.toString(),
        sentimentScore = this.sentimentScore,
        sentiment = this.sentiment?.name,
        relatedSymbols = this.relatedSymbols
    )
}
