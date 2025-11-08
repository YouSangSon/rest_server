package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.model.NewsArticle
import java.time.LocalDateTime

/**
 * 뉴스 API Port (Outbound Port)
 *
 * 외부 뉴스 API와의 통신을 위한 포트.
 * NewsAPI.org, Alpha Vantage 등의 어댑터가 이 포트를 구현합니다.
 */
interface NewsApiPort {
    /**
     * 키워드로 뉴스를 검색합니다.
     *
     * @param keywords 검색 키워드
     * @param language 언어 코드 (예: "en", "ko")
     * @param from 시작 날짜
     * @param to 종료 날짜
     * @param pageSize 페이지 크기 (기본값: 100)
     * @return 검색된 뉴스 목록
     */
    fun fetchNews(
        keywords: List<String>,
        language: String = "en",
        from: LocalDateTime? = null,
        to: LocalDateTime? = null,
        pageSize: Int = 100
    ): List<NewsArticle>

    /**
     * 특정 주제의 최신 뉴스를 가져옵니다.
     *
     * @param category 카테고리 (예: "business", "technology")
     * @param language 언어 코드
     * @param pageSize 페이지 크기
     * @return 최신 뉴스 목록
     */
    fun fetchTopHeadlines(
        category: String = "business",
        language: String = "en",
        pageSize: Int = 100
    ): List<NewsArticle>

    /**
     * 뉴스 소스의 이름을 반환합니다.
     *
     * @return 뉴스 소스 이름 (예: "NewsAPI", "AlphaVantage")
     */
    fun getSourceName(): String

    /**
     * Rate Limit을 확인합니다.
     *
     * @return 남은 요청 수
     */
    fun getRemainingRequests(): Int?
}
