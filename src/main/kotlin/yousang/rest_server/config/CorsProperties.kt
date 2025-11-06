package yousang.rest_server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "cors")
data class CorsProperties(
    var allowedOrigins: List<String> = listOf("http://localhost:3000", "http://localhost:8080"),
    var allowedMethods: List<String> = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"),
    var allowedHeaders: List<String> = listOf("*"),
    var allowCredentials: Boolean = true,
    var maxAge: Long = 3600
)
