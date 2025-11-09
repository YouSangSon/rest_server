package yousang.rest_server.domain.event

import yousang.rest_server.domain.model.SentimentType

/**
 * 뉴스 수집 이벤트
 */
data class NewsCollectedEvent(
    val newsId: Long,
    val source: String,
    val title: String,
    val url: String,
    val relatedSymbols: List<String>
) : BaseDomainEvent() {
    override val eventType: String = "news.article.collected"
}

/**
 * 감성 분석 완료 이벤트
 */
data class SentimentAnalyzedEvent(
    val newsId: Long,
    val sentimentScore: Double,
    val sentiment: SentimentType,
    val relatedSymbols: List<String>
) : BaseDomainEvent() {
    override val eventType: String = "news.sentiment.analyzed"
}

/**
 * SNS 포스트 수집 이벤트
 */
data class SocialPostCollectedEvent(
    val platform: String,  // twitter, reddit
    val author: String,
    val content: String,
    val url: String,
    val relatedSymbols: List<String>
) : BaseDomainEvent() {
    override val eventType: String = "social.post.collected"
}
