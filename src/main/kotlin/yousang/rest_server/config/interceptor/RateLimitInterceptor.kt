package yousang.rest_server.config.interceptor

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import yousang.rest_server.config.RateLimitProperties
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple in-memory rate limiting using Bucket4j
 * For production, consider using Redis-backed rate limiting
 */
@Component
class RateLimitInterceptor(
    private val rateLimitProperties: RateLimitProperties
) : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(RateLimitInterceptor::class.java)
    private val cache = ConcurrentHashMap<String, Bucket>()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (!rateLimitProperties.enabled) {
            return true
        }

        val clientId = getClientId(request)
        val bucket = cache.computeIfAbsent(clientId) { createNewBucket() }

        return if (bucket.tryConsume(1)) {
            true
        } else {
            logger.warn("Rate limit exceeded for client: $clientId")
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.writer.write("{\"error\":\"Too many requests\",\"message\":\"Rate limit exceeded\"}")
            response.contentType = "application/json"
            false
        }
    }

    private fun getClientId(request: HttpServletRequest): String {
        // Use X-Forwarded-For header if behind a proxy, otherwise use remote address
        return request.getHeader("X-Forwarded-For")?.split(",")?.first()?.trim()
            ?: request.remoteAddr
    }

    private fun createNewBucket(): Bucket {
        val limit = Bandwidth.classic(
            rateLimitProperties.requestsPerMinute.toLong(),
            Refill.intervally(rateLimitProperties.requestsPerMinute.toLong(), Duration.ofMinutes(1))
        )
        return Bucket.builder()
            .addLimit(limit)
            .build()
    }
}
