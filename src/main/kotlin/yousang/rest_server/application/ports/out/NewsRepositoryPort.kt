package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.model.NewsArticle
import yousang.rest_server.domain.model.SentimentType
import java.time.LocalDateTime

/**
 * 뉴스 Repository Port (Outbound Port)
 *
 * 뉴스 기사 저장 및 조회를 위한 포트.
 * MongoDB에 구현됩니다.
 */
interface NewsRepositoryPort {
    /**
     * 뉴스 기사를 저장합니다.
     *
     * @param article 저장할 뉴스 기사
     * @return 저장된 뉴스 기사 (ID 포함)
     */
    fun save(article: NewsArticle): NewsArticle

    /**
     * 여러 뉴스 기사를 일괄 저장합니다.
     *
     * @param articles 저장할 뉴스 기사 목록
     * @return 저장된 뉴스 기사 목록
     */
    fun saveAll(articles: List<NewsArticle>): List<NewsArticle>

    /**
     * ID로 뉴스 기사를 조회합니다.
     *
     * @param id 뉴스 기사 ID
     * @return 뉴스 기사 (없으면 null)
     */
    fun findById(id: Long): NewsArticle?

    /**
     * URL로 뉴스 기사를 조회합니다 (중복 체크용).
     *
     * @param url 뉴스 URL
     * @return 뉴스 기사 (없으면 null)
     */
    fun findByUrl(url: String): NewsArticle?

    /**
     * 특정 심볼과 관련된 뉴스를 조회합니다.
     *
     * @param symbol 거래 쌍
     * @param limit 최대 개수
     * @return 뉴스 기사 목록
     */
    fun findBySymbol(symbol: String, limit: Int = 20): List<NewsArticle>

    /**
     * 특정 감성 타입의 뉴스를 조회합니다.
     *
     * @param sentiment 감성 타입
     * @param limit 최대 개수
     * @return 뉴스 기사 목록
     */
    fun findBySentiment(sentiment: SentimentType, limit: Int = 20): List<NewsArticle>

    /**
     * 날짜 범위 내의 뉴스를 조회합니다.
     *
     * @param from 시작 날짜
     * @param to 종료 날짜
     * @param limit 최대 개수
     * @return 뉴스 기사 목록
     */
    fun findByDateRange(from: LocalDateTime, to: LocalDateTime, limit: Int = 100): List<NewsArticle>

    /**
     * 최신 뉴스를 조회합니다.
     *
     * @param limit 최대 개수
     * @return 뉴스 기사 목록 (발행일 내림차순)
     */
    fun findLatest(limit: Int = 20): List<NewsArticle>

    /**
     * 분석되지 않은 뉴스를 조회합니다.
     *
     * @param limit 최대 개수
     * @return 뉴스 기사 목록
     */
    fun findUnanalyzed(limit: Int = 100): List<NewsArticle>

    /**
     * 특정 심볼과 기간에 해당하는 뉴스의 감성을 집계합니다.
     *
     * @param symbol 거래 쌍
     * @param hoursBack 과거 몇 시간
     * @return 감성별 개수 (Map<SentimentType, Int>)
     */
    fun aggregateSentimentBySymbol(symbol: String, hoursBack: Int = 24): Map<SentimentType, Int>

    /**
     * 뉴스 기사를 삭제합니다.
     *
     * @param id 뉴스 기사 ID
     */
    fun deleteById(id: Long)
}
