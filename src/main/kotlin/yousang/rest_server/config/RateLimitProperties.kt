package yousang.rest_server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "rate-limit")
data class RateLimitProperties(
    var enabled: Boolean = true,
    var requestsPerMinute: Int = 100
)
