package yousang.rest_server.application.ports.`in`

import yousang.rest_server.domain.model.NewsArticle
import yousang.rest_server.domain.model.SentimentType

/**
 * 감성 분석 Use Case
 *
 * 뉴스 기사의 감성(긍정/중립/부정)을 분석합니다.
 */
interface AnalyzeSentimentUseCase {
    /**
     * 뉴스 기사의 감성을 분석합니다.
     *
     * @param newsArticle 분석할 뉴스 기사
     * @return 감성 분석이 완료된 뉴스 기사
     */
    fun analyzeSentiment(newsArticle: NewsArticle): NewsArticle

    /**
     * 여러 뉴스 기사의 감성을 일괄 분석합니다.
     *
     * @param articles 분석할 뉴스 기사 목록
     * @return 감성 분석이 완료된 뉴스 기사 목록
     */
    fun analyzeSentimentBatch(articles: List<NewsArticle>): List<NewsArticle>

    /**
     * 텍스트의 감성을 분석합니다.
     *
     * @param text 분석할 텍스트
     * @return 감성 점수 (-1.0 ~ +1.0)
     */
    fun analyzeSentimentScore(text: String): Double

    /**
     * 특정 심볼에 대한 전체 뉴스 감성을 집계합니다.
     *
     * @param symbol 거래 쌍 (예: "BTC/USDT")
     * @param hoursBack 과거 몇 시간의 뉴스를 분석할지 (기본값: 24시간)
     * @return 집계된 감성 점수 (-1.0 ~ +1.0)
     */
    fun getAggregateSentiment(symbol: String, hoursBack: Int = 24): AggregatedSentiment
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
) {
    init {
        require(averageScore in -1.0..1.0) { "평균 감성 점수는 -1.0 ~ +1.0 사이여야 합니다" }
        require(totalArticles >= 0) { "전체 기사 수는 0 이상이어야 합니다" }
    }
}
