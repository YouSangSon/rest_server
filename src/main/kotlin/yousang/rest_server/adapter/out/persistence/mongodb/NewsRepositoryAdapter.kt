package yousang.rest_server.adapter.out.persistence.mongodb

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import yousang.rest_server.application.ports.out.NewsRepositoryPort
import yousang.rest_server.domain.model.NewsArticle
import yousang.rest_server.domain.model.SentimentType
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

@Component
class NewsRepositoryAdapter(
    private val newsArticleMongoRepository: NewsArticleMongoRepository
) : NewsRepositoryPort {

    private val idGenerator = AtomicLong(1)

    override fun save(article: NewsArticle): NewsArticle {
        val articleWithId = if (article.id == null) {
            article.copy(id = idGenerator.getAndIncrement())
        } else {
            article
        }
        val document = NewsArticleDocument.fromDomain(articleWithId)
        val saved = newsArticleMongoRepository.save(document)
        return saved.toDomain()
    }

    override fun saveAll(articles: List<NewsArticle>): List<NewsArticle> {
        val articlesWithIds = articles.map {
            if (it.id == null) it.copy(id = idGenerator.getAndIncrement()) else it
        }
        val documents = articlesWithIds.map { NewsArticleDocument.fromDomain(it) }
        return newsArticleMongoRepository.saveAll(documents).map { it.toDomain() }
    }

    override fun findById(id: Long): NewsArticle? {
        return newsArticleMongoRepository.findById(id.toString())
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUrl(url: String): NewsArticle? {
        return newsArticleMongoRepository.findByUrl(url)?.toDomain()
    }

    override fun findBySymbol(symbol: String, limit: Int): List<NewsArticle> {
        val pageable = PageRequest.of(0, limit)
        return newsArticleMongoRepository.findByRelatedSymbolsContaining(symbol, pageable)
            .map { it.toDomain() }
    }

    override fun findBySentiment(sentiment: SentimentType, limit: Int): List<NewsArticle> {
        val pageable = PageRequest.of(0, limit)
        return newsArticleMongoRepository.findBySentiment(sentiment.name, pageable)
            .map { it.toDomain() }
    }

    override fun findByDateRange(from: LocalDateTime, to: LocalDateTime, limit: Int): List<NewsArticle> {
        val pageable = PageRequest.of(0, limit)
        return newsArticleMongoRepository.findByPublishedAtBetween(from, to, pageable)
            .map { it.toDomain() }
    }

    override fun findLatest(limit: Int): List<NewsArticle> {
        val pageable = PageRequest.of(0, limit)
        val oneDayAgo = LocalDateTime.now().minusDays(1)
        return newsArticleMongoRepository.findByPublishedAtAfterOrderByPublishedAtDesc(oneDayAgo, pageable)
            .map { it.toDomain() }
    }

    override fun findUnanalyzed(limit: Int): List<NewsArticle> {
        val pageable = PageRequest.of(0, limit)
        return newsArticleMongoRepository.findUnanalyzed(pageable)
            .map { it.toDomain() }
    }

    override fun aggregateSentimentBySymbol(symbol: String, hoursBack: Int): Map<SentimentType, Int> {
        val from = LocalDateTime.now().minusHours(hoursBack.toLong())
        val to = LocalDateTime.now()
        val articles = findByDateRange(from, to, 1000)
            .filter { it.isRelatedTo(symbol) && it.isAnalyzed() }

        return articles.groupBy { it.sentiment!! }
            .mapValues { it.value.size }
    }

    override fun deleteById(id: Long) {
        newsArticleMongoRepository.deleteById(id.toString())
    }
}
