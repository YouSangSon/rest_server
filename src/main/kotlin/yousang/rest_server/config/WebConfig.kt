package yousang.rest_server.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import yousang.rest_server.config.interceptor.RateLimitInterceptor

@Configuration
class WebConfig(
    private val rateLimitInterceptor: RateLimitInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/v1/auth/**",
                "/actuator/**",
                "/swagger-ui/**",
                "/api-docs/**"
            )
    }
}
