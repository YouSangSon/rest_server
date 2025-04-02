package yousang.rest

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux
import java.io.File

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
	// 현재 활성화된 프로파일에 따라 .env 파일 로드
	val profile = System.getenv("SPRING_PROFILES_ACTIVE") ?: "dev"
	
	// env 디렉토리에서 프로파일에 맞는 .env 파일 찾기
	val envFilePath = "env/$profile.env"
	val envFile = File(envFilePath)
	
	if (envFile.exists()) {
		// 파일이 있으면 로드
		val dotenv = Dotenv.configure()
			.directory("env")
			.filename("$profile.env")
			.load()
			
		// 주요 환경 변수 설정
		setSystemPropertiesFromDotenv(dotenv)
		
		println("환경 파일 로드 완료: $envFilePath")
	} else {
		println("경고: 환경 파일을 찾을 수 없습니다: $envFilePath")
	}
	
	runApplication<RestApplication>(*args) {
		// WebFlux가 활성화된 상태로 애플리케이션 시작
		webApplicationType = org.springframework.boot.WebApplicationType.REACTIVE
	}
}

/**
 * Dotenv에서 시스템 프로퍼티로 환경 변수 설정
 */
fun setSystemPropertiesFromDotenv(dotenv: Dotenv) {
	// 데이터베이스 연결 정보
	System.setProperty("DB_URL", dotenv["DB_URL"] ?: "")
	System.setProperty("DB_USERNAME", dotenv["DB_USERNAME"] ?: "")
	System.setProperty("DB_PASSWORD", dotenv["DB_PASSWORD"] ?: "")
	
	// 애플리케이션 설정
	System.setProperty("SPRING_PROFILES_ACTIVE", dotenv["SPRING_PROFILES_ACTIVE"] ?: "dev")
	System.setProperty("APP_PORT", dotenv["APP_PORT"] ?: "8080")
	
	// 로깅 설정
	System.setProperty("LOG_LEVEL", dotenv["LOG_LEVEL"] ?: "INFO")
	System.setProperty("APP_LOG_LEVEL", dotenv["APP_LOG_LEVEL"] ?: "DEBUG")
	System.setProperty("JPA_SHOW_SQL", dotenv["JPA_SHOW_SQL"] ?: "false")
	System.setProperty("HIBERNATE_FORMAT_SQL", dotenv["HIBERNATE_FORMAT_SQL"] ?: "false")
	
	// CORS 설정
	System.setProperty("CORS_ALLOWED_ORIGINS", dotenv["CORS_ALLOWED_ORIGINS"] ?: "*")
	System.setProperty("CORS_ALLOWED_METHODS", dotenv["CORS_ALLOWED_METHODS"] ?: "GET,POST,PUT,DELETE,OPTIONS")
	System.setProperty("CORS_ALLOWED_HEADERS", dotenv["CORS_ALLOWED_HEADERS"] ?: "*")
}