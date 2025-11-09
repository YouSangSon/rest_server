package yousang.rest_server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

/**
 * RestTemplate 설정
 */
@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
