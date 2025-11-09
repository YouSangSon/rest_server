package yousang.rest_server.domain.model

import java.time.LocalDateTime

/**
 * 뉴스 소스 도메인 모델
 *
 * 뉴스 API 제공자 정보를 나타냅니다.
 */
data class NewsSource(
    val id: Long? = null,
    val name: String,
    val apiKey: String,
    val baseUrl: String,
    val requestLimitPerMinute: Int = 100,
    val isActive: Boolean = true,
    val lastFetchedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(name.isNotBlank()) { "뉴스 소스 이름은 필수입니다" }
        require(apiKey.isNotBlank()) { "API Key는 필수입니다" }
        require(baseUrl.isNotBlank()) { "Base URL은 필수입니다" }
        require(requestLimitPerMinute > 0) { "요청 제한은 0보다 커야 합니다" }
    }

    /**
     * 소스를 활성화/비활성화합니다.
     */
    fun toggleActive(): NewsSource {
        return copy(isActive = !isActive, updatedAt = LocalDateTime.now())
    }

    /**
     * 마지막 수집 시간을 업데이트합니다.
     */
    fun updateLastFetched(): NewsSource {
        return copy(lastFetchedAt = LocalDateTime.now(), updatedAt = LocalDateTime.now())
    }

    /**
     * Rate Limit을 준수하는지 확인합니다.
     * 마지막 수집 후 1분이 지나지 않았으면 false.
     */
    fun canFetchNow(): Boolean {
        if (lastFetchedAt == null) return true
        val oneMinuteAgo = LocalDateTime.now().minusMinutes(1)
        return lastFetchedAt.isBefore(oneMinuteAgo)
    }

    companion object {
        /**
         * 뉴스 소스를 생성합니다.
         */
        fun create(
            name: String,
            apiKey: String,
            baseUrl: String,
            requestLimitPerMinute: Int = 100
        ): NewsSource {
            return NewsSource(
                name = name,
                apiKey = apiKey,
                baseUrl = baseUrl,
                requestLimitPerMinute = requestLimitPerMinute
            )
        }

        /**
         * NewsAPI.org 소스
         */
        fun newsApi(apiKey: String): NewsSource {
            return create(
                name = "NewsAPI",
                apiKey = apiKey,
                baseUrl = "https://newsapi.org/v2",
                requestLimitPerMinute = 100
            )
        }

        /**
         * Alpha Vantage 뉴스 소스
         */
        fun alphaVantage(apiKey: String): NewsSource {
            return create(
                name = "AlphaVantage",
                apiKey = apiKey,
                baseUrl = "https://www.alphavantage.co",
                requestLimitPerMinute = 5
            )
        }

        /**
         * Crypto News API
         */
        fun cryptoNews(apiKey: String): NewsSource {
            return create(
                name = "CryptoNewsAPI",
                apiKey = apiKey,
                baseUrl = "https://cryptonews-api.com/api/v1",
                requestLimitPerMinute = 60
            )
        }
    }
}
