package yousang.rest_server.domain.model

import java.time.LocalDateTime

/**
 * 뉴스 기사 도메인 모델
 *
 * 금융 뉴스를 나타내는 핵심 도메인 엔티티.
 * 뉴스 수집, 감성 분석, 트레이딩 신호 생성에 사용됩니다.
 */
data class NewsArticle(
    val id: Long? = null,
    val source: String,
    val title: String,
    val content: String,
    val url: String,
    val publishedAt: LocalDateTime,
    val language: String = "en",
    val keywords: List<String> = emptyList(),
    val sentimentScore: Double? = null,  // -1.0 ~ +1.0
    val sentiment: SentimentType? = null,
    val relatedSymbols: List<String> = emptyList(),  // ["BTC/USDT", "ETH/USDT"]
    val analyzedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(title.isNotBlank()) { "뉴스 제목은 필수입니다" }
        require(content.isNotBlank()) { "뉴스 내용은 필수입니다" }
        require(url.isNotBlank()) { "뉴스 URL은 필수입니다" }
        sentimentScore?.let {
            require(it in -1.0..1.0) { "감성 점수는 -1.0 ~ +1.0 사이여야 합니다: $it" }
        }
    }

    /**
     * 감성 분석 결과를 업데이트합니다.
     */
    fun withSentimentAnalysis(score: Double, type: SentimentType): NewsArticle {
        require(score in -1.0..1.0) { "감성 점수는 -1.0 ~ +1.0 사이여야 합니다: $score" }
        return copy(
            sentimentScore = score,
            sentiment = type,
            analyzedAt = LocalDateTime.now()
        )
    }

    /**
     * 연관 심볼을 추가합니다.
     */
    fun withRelatedSymbols(symbols: List<String>): NewsArticle {
        val uniqueSymbols = (relatedSymbols + symbols).distinct()
        return copy(relatedSymbols = uniqueSymbols)
    }

    /**
     * 뉴스가 특정 심볼과 관련이 있는지 확인합니다.
     */
    fun isRelatedTo(symbol: String): Boolean {
        return relatedSymbols.contains(symbol) ||
                title.contains(symbol, ignoreCase = true) ||
                content.contains(symbol, ignoreCase = true)
    }

    /**
     * 감성이 긍정적인지 확인합니다.
     */
    fun isPositive(): Boolean = sentiment == SentimentType.POSITIVE

    /**
     * 감성이 부정적인지 확인합니다.
     */
    fun isNegative(): Boolean = sentiment == SentimentType.NEGATIVE

    /**
     * 뉴스가 최신인지 확인합니다 (24시간 이내).
     */
    fun isRecent(): Boolean {
        return publishedAt.isAfter(LocalDateTime.now().minusHours(24))
    }

    /**
     * 감성 분석이 완료되었는지 확인합니다.
     */
    fun isAnalyzed(): Boolean = sentimentScore != null && sentiment != null

    companion object {
        /**
         * 뉴스 기사를 생성합니다 (팩토리 메서드).
         */
        fun create(
            source: String,
            title: String,
            content: String,
            url: String,
            publishedAt: LocalDateTime,
            language: String = "en",
            keywords: List<String> = emptyList()
        ): NewsArticle {
            return NewsArticle(
                source = source,
                title = title,
                content = content,
                url = url,
                publishedAt = publishedAt,
                language = language,
                keywords = keywords
            )
        }
    }
}

/**
 * 감성 분석 결과 타입
 */
enum class SentimentType {
    POSITIVE,   // 긍정 (score > 0.3)
    NEUTRAL,    // 중립 (-0.3 ~ 0.3)
    NEGATIVE;   // 부정 (score < -0.3)

    companion object {
        /**
         * 감성 점수를 기반으로 타입을 결정합니다.
         */
        fun fromScore(score: Double): SentimentType {
            return when {
                score > 0.3 -> POSITIVE
                score < -0.3 -> NEGATIVE
                else -> NEUTRAL
            }
        }
    }
}
