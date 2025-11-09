package yousang.rest_server.adapter.out.persistence.mongodb

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import yousang.rest_server.domain.model.NewsArticle
import yousang.rest_server.domain.model.SentimentType
import java.time.LocalDateTime

/**
 * 뉴스 기사 MongoDB Document
 */
@Document(collection = "news_articles")
data class NewsArticleDocument(
    @Id
    val id: String? = null,

    @Indexed
    val source: String,

    val title: String,
    val content: String,
    val url: String,

    @Indexed
    val publishedAt: LocalDateTime,

    val language: String = "en",
    val keywords: List<String> = emptyList(),
    val sentimentScore: Double? = null,

    @Indexed
    val sentiment: String? = null,  // POSITIVE, NEUTRAL, NEGATIVE

    @Indexed
    val relatedSymbols: List<String> = emptyList(),

    val analyzedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): NewsArticle {
        return NewsArticle(
            id = id?.toLongOrNull(),
            source = source,
            title = title,
            content = content,
            url = url,
            publishedAt = publishedAt,
            language = language,
            keywords = keywords,
            sentimentScore = sentimentScore,
            sentiment = sentiment?.let { SentimentType.valueOf(it) },
            relatedSymbols = relatedSymbols,
            analyzedAt = analyzedAt,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(article: NewsArticle): NewsArticleDocument {
            return NewsArticleDocument(
                id = article.id?.toString(),
                source = article.source,
                title = article.title,
                content = article.content,
                url = article.url,
                publishedAt = article.publishedAt,
                language = article.language,
                keywords = article.keywords,
                sentimentScore = article.sentimentScore,
                sentiment = article.sentiment?.name,
                relatedSymbols = article.relatedSymbols,
                analyzedAt = article.analyzedAt,
                createdAt = article.createdAt
            )
        }
    }
}
