package yousang.rest.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.http.MediaType
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.reactive.config.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.netty.http.client.HttpClient
import java.time.Duration

/**
 * WebFlux 관련 설정
 * - JSON 인코더/디코더 설정
 * - CORS 설정
 * - 리소스 핸들러 설정
 * - URL 경로 매칭 설정
 */
@Configuration
@EnableWebFlux
class WebFluxConfig(private val objectMapper: ObjectMapper) : WebFluxConfigurer {
    
    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        // JSON 인코더/디코더 설정
        val encoder = Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON)
        val decoder = Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON)
        
        configurer.defaultCodecs().jackson2JsonEncoder(encoder)
        configurer.defaultCodecs().jackson2JsonDecoder(decoder)
        
        // 메모리 버퍼 크기 제한 설정
        configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) // 2MB로 설정
    }
    
    override fun configurePathMatching(configurer: PathMatchConfigurer) {
        // URL 경로 매칭 설정
        configurer.setUseCaseSensitiveMatch(false)
        configurer.setUseTrailingSlashMatch(true)
    }
    
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Swagger UI 리소스 핸들러 설정
        registry.addResourceHandler("/swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/")
        
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/")
            
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
    }
    
    // CORS 설정
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600)
    }
}

/**
 * WebClient 관련 설정
 * - WebClient.Builder 구성
 * - 타임아웃 설정
 * - 메모리 버퍼 크기 설정
 */
@Configuration
class WebClientConfig(private val objectMapper: ObjectMapper) {
    
    @Bean
    @Primary
    fun webClientBuilder(): WebClient.Builder {
        // HTTP 클라이언트 설정
        val httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(10))
            
        // 교환 전략 설정
        val exchangeStrategies = ExchangeStrategies.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON))
                configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON))
                configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) // 2MB
            }
            .build()
            
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(exchangeStrategies)
    }
} 