package yousang.rest_server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var secret: String = "",
    var expiration: Long = 86400000,  // 24 hours
    var refreshExpiration: Long = 604800000  // 7 days
)
