package yousang.rest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication(
	exclude = [
		WebMvcAutoConfiguration::class, // WebMvc 자동 구성 제외하여 WebFlux만 사용
		OAuth2ClientAutoConfiguration::class // OAuth2 서블릿 클라이언트 구성 제외
	]
)

@ConfigurationPropertiesScan
@EnableWebFlux // WebFlux 활성화
class RestApplication

fun main(args: Array<String>) {
	runApplication<RestApplication>(*args) {
		// WebFlux가 활성화된 상태로 애플리케이션 시작
		setWebApplicationType(org.springframework.boot.WebApplicationType.REACTIVE)
	}
}