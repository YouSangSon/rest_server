package yousang.rest_server.application.ports.`in`

import yousang.rest_server.domain.model.NewsArticle
import yousang.rest_server.domain.model.SentimentType
import java.time.LocalDateTime

/**
 * 뉴스 기사 조회 Use Case
 *
 * 저장된 뉴스 기사를 조회합니다.
 */
interface GetNewsArticlesUseCase {
    /**
     * ID로 뉴스 기사를 조회합니다.
     *
     * @param id 뉴스 기사 ID
     * @return 뉴스 기사
     */
    fun getNewsById(id: Long): NewsArticle?

    /**
     * 특정 심볼과 관련된 뉴스 기사를 조회합니다.
     *
     * @param symbol 거래 쌍 (예: "BTC/USDT")
     * @param limit 최대 개수 (기본값: 20)
     * @return 뉴스 기사 목록
     */
    fun getNewsBySymbol(symbol: String, limit: Int = 20): List<NewsArticle>

    /**
     * 특정 감성 타입의 뉴스 기사를 조회합니다.
     *
     * @param sentiment 감성 타입
     * @param limit 최대 개수 (기본값: 20)
     * @return 뉴스 기사 목록
     */
    fun getNewsBySentiment(sentiment: SentimentType, limit: Int = 20): List<NewsArticle>

    /**
     * 날짜 범위 내의 뉴스 기사를 조회합니다.
     *
     * @param from 시작 날짜
     * @param to 종료 날짜
     * @param limit 최대 개수 (기본값: 100)
     * @return 뉴스 기사 목록
     */
    fun getNewsByDateRange(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int = 100
    ): List<NewsArticle>

    /**
     * 최신 뉴스 기사를 조회합니다.
     *
     * @param limit 최대 개수 (기본값: 20)
     * @return 뉴스 기사 목록
     */
    fun getLatestNews(limit: Int = 20): List<NewsArticle>

    /**
     * 분석되지 않은 뉴스 기사를 조회합니다 (감성 분석 대상).
     *
     * @param limit 최대 개수 (기본값: 100)
     * @return 뉴스 기사 목록
     */
    fun getUnanalyzedNews(limit: Int = 100): List<NewsArticle>
}
