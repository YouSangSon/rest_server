package yousang.rest.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * 디버깅 및 API 문서화 관련 설정
 * - Swagger UI 설정
 * - 디버그 응답 필터
 */
@Configuration
class DebugSwaggerConfig {
    
    private val logger = LoggerFactory.getLogger(DebugSwaggerConfig::class.java)
    
    /**
     * OpenAPI 설정
     * Swagger UI를 통한 API 문서화
     */
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearer-token",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
            .info(
                Info()
                    .title("REST API")
                    .description("Spring Boot 기반 RESTful API")
                    .version("1.0.0")
            )
    }
    
    /**
     * 디버그 응답 필터
     * 개발 환경에서만 활성화
     */
    @Bean
    @Profile("dev") // 개발 환경에서만 활성화
    fun debugResponseFilter(): WebFilter {
        return object : WebFilter {
            override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
                val startTime = System.currentTimeMillis()
                val path = exchange.request.uri.path
                val method = exchange.request.method.name()
                
                // 요청 헤더 로깅
                logger.debug("Request: $method $path")
                exchange.request.headers.forEach { (name, values) ->
                    logger.debug("Header: $name = $values")
                }
                
                return chain.filter(exchange).doFinally {
                    val duration = System.currentTimeMillis() - startTime
                    val status = exchange.response.statusCode?.value() ?: 0
                    
                    // 응답 정보 로깅
                    logger.debug("Response: $status in ${duration}ms for $method $path")
                }
            }
        }
    }
} 