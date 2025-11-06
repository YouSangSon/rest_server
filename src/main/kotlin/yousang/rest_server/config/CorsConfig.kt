package yousang.rest_server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig(
    private val corsProperties: CorsProperties
) {

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()

        config.allowCredentials = corsProperties.allowCredentials
        corsProperties.allowedOrigins.forEach { config.addAllowedOrigin(it) }
        corsProperties.allowedMethods.forEach { config.addAllowedMethod(it) }
        corsProperties.allowedHeaders.forEach { config.addAllowedHeader(it) }
        config.maxAge = corsProperties.maxAge

        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}
