package yousang.rest_server.adapter.out.persistence.mongodb

import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface NewsArticleMongoRepository : MongoRepository<NewsArticleDocument, String> {
    fun findByUrl(url: String): NewsArticleDocument?
    fun findByRelatedSymbolsContaining(symbol: String, pageable: Pageable): List<NewsArticleDocument>
    fun findBySentiment(sentiment: String, pageable: Pageable): List<NewsArticleDocument>

    @Query("{'publishedAt': {'\$gte': ?0, '\$lte': ?1}}")
    fun findByPublishedAtBetween(from: LocalDateTime, to: LocalDateTime, pageable: Pageable): List<NewsArticleDocument>

    fun findByPublishedAtAfterOrderByPublishedAtDesc(after: LocalDateTime, pageable: Pageable): List<NewsArticleDocument>

    @Query("{'sentiment': null}")
    fun findUnanalyzed(pageable: Pageable): List<NewsArticleDocument>
}
