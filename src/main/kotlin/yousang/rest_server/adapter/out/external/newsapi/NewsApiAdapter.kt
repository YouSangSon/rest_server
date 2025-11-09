package yousang.rest_server.adapter.out.external.newsapi

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import yousang.rest_server.application.ports.out.NewsApiPort
import yousang.rest_server.domain.model.NewsArticle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * NewsAPI.org 어댑터
 *
 * https://newsapi.org/ API를 통해 실시간 뉴스 수집
 */
@Component
class NewsApiAdapter(
    private val restTemplate: RestTemplate,
    @Value("\${newsapi.api-key:}") private val apiKey: String,
    @Value("\${newsapi.base-url:https://newsapi.org/v2}") private val baseUrl: String
) : NewsApiPort {

    private val dateFormatter = DateTimeFormatter.ISO_DATE_TIME

    override fun fetchNews(
        keywords: List<String>,
        language: String,
        from: LocalDateTime?,
        to: LocalDateTime?
    ): List<NewsArticle> {
        if (apiKey.isBlank()) {
            println("NewsAPI key not configured, skipping...")
            return emptyList()
        }

        val query = keywords.joinToString(" OR ")

        val url = UriComponentsBuilder.fromHttpUrl("$baseUrl/everything")
            .queryParam("q", query)
            .queryParam("language", language)
            .queryParam("sortBy", "publishedAt")
            .queryParam("pageSize", 100)
            .apply {
                from?.let { queryParam("from", it.format(dateFormatter)) }
                to?.let { queryParam("to", it.format(dateFormatter)) }
            }
            .build()
            .toUriString()

        val headers = HttpHeaders().apply {
            set("X-Api-Key", apiKey)
        }

        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                NewsApiResponse::class.java
            )

            response.body?.articles?.map { article ->
                NewsArticle.create(
                    source = article.source.name,
                    author = article.author,
                    title = article.title ?: "No title",
                    description = article.description ?: "",
                    content = article.content ?: article.description ?: "",
                    url = article.url,
                    publishedAt = parsePublishedAt(article.publishedAt),
                    relatedSymbols = extractSymbolsFromText(article.title + " " + article.description)
                )
            } ?: emptyList()
        } catch (e: Exception) {
            println("Failed to fetch from NewsAPI: ${e.message}")
            emptyList()
        }
    }

    override fun fetchNewsBySymbol(symbol: String, language: String, limit: Int): List<NewsArticle> {
        val keywords = when {
            symbol.contains("BTC") -> listOf("Bitcoin", "BTC")
            symbol.contains("ETH") -> listOf("Ethereum", "ETH")
            symbol.contains("BNB") -> listOf("Binance", "BNB")
            else -> listOf(symbol)
        }
        return fetchNews(keywords, language).take(limit)
    }

    override fun fetchTopHeadlines(category: String, country: String, limit: Int): List<NewsArticle> {
        if (apiKey.isBlank()) {
            println("NewsAPI key not configured, skipping...")
            return emptyList()
        }

        val url = UriComponentsBuilder.fromHttpUrl("$baseUrl/top-headlines")
            .queryParam("category", category)
            .queryParam("country", country)
            .queryParam("pageSize", limit)
            .build()
            .toUriString()

        val headers = HttpHeaders().apply {
            set("X-Api-Key", apiKey)
        }

        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                NewsApiResponse::class.java
            )

            response.body?.articles?.map { article ->
                NewsArticle.create(
                    source = article.source.name,
                    author = article.author,
                    title = article.title ?: "No title",
                    description = article.description ?: "",
                    content = article.content ?: article.description ?: "",
                    url = article.url,
                    publishedAt = parsePublishedAt(article.publishedAt),
                    relatedSymbols = extractSymbolsFromText(article.title + " " + article.description)
                )
            } ?: emptyList()
        } catch (e: Exception) {
            println("Failed to fetch top headlines: ${e.message}")
            emptyList()
        }
    }

    override fun getSourceName(): String = "NewsAPI"

    private fun parsePublishedAt(dateStr: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    private fun extractSymbolsFromText(text: String): List<String> {
        val symbols = mutableListOf<String>()
        val lowerText = text.lowercase()

        // 암호화폐 키워드 매핑
        val cryptoKeywords = mapOf(
            "bitcoin" to "BTC/USDT",
            "btc" to "BTC/USDT",
            "ethereum" to "ETH/USDT",
            "eth" to "ETH/USDT",
            "binance coin" to "BNB/USDT",
            "bnb" to "BNB/USDT",
            "ripple" to "XRP/USDT",
            "xrp" to "XRP/USDT",
            "cardano" to "ADA/USDT",
            "ada" to "ADA/USDT",
            "solana" to "SOL/USDT",
            "sol" to "SOL/USDT",
            "polkadot" to "DOT/USDT",
            "dot" to "DOT/USDT",
            "dogecoin" to "DOGE/USDT",
            "doge" to "DOGE/USDT"
        )

        cryptoKeywords.forEach { (keyword, symbol) ->
            if (lowerText.contains(keyword)) {
                symbols.add(symbol)
            }
        }

        return symbols.distinct()
    }
}

/**
 * NewsAPI 응답 모델
 */
data class NewsApiResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<NewsApiArticle>
)

data class NewsApiArticle(
    val source: NewsApiSource,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?
)

data class NewsApiSource(
    val id: String?,
    val name: String
)
