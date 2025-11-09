package yousang.rest_server.application.ports.`in`

import yousang.rest_server.domain.model.NewsArticle
import java.time.LocalDateTime

/**
 * 뉴스 수집 Use Case
 *
 * 외부 뉴스 API로부터 금융 관련 뉴스를 수집합니다.
 */
interface CollectNewsUseCase {
    /**
     * 특정 키워드로 뉴스를 수집합니다.
     *
     * @param keywords 검색 키워드 (예: "Bitcoin", "cryptocurrency")
     * @param language 언어 코드 (기본값: "en")
     * @param from 시작 날짜
     * @param to 종료 날짜
     * @return 수집된 뉴스 목록
     */
    fun collectNews(
        keywords: List<String>,
        language: String = "en",
        from: LocalDateTime? = null,
        to: LocalDateTime? = null
    ): List<NewsArticle>

    /**
     * 특정 심볼(거래 쌍)과 관련된 뉴스를 수집합니다.
     *
     * @param symbol 거래 쌍 (예: "BTC/USDT")
     * @return 수집된 뉴스 목록
     */
    fun collectNewsBySymbol(symbol: String): List<NewsArticle>

    /**
     * 모든 활성 소스로부터 최신 뉴스를 수집합니다.
     *
     * @return 수집된 뉴스 목록
     */
    fun collectLatestNews(): List<NewsArticle>
}
