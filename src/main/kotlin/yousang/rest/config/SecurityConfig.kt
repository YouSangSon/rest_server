package yousang.rest.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import org.springframework.core.annotation.Order

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { csrf -> csrf.disable() }
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .authorizeExchange { authorize -> 
                authorize
                    .pathMatchers(
                        // Swagger UI 및 API 문서화 경로
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/api-docs/**",
                        "/webjars/**",
                        // OAuth2 로그인 관련 경로
                        "/login/**",
                        "/oauth2/**",
                        // API 경로 (인증 필요)
                        "/api/v1/auth/**",
                        "/api/v1/public/**"
                    ).permitAll()
                    .pathMatchers(
                        // 보호된 API 경로
                        "/api/v1/**"
                    ).authenticated()
                    .anyExchange().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .authorizedClientRepository(authorizedClientRepository())
                    .tokenResponseClient(tokenResponseClient())
            }
            .oauth2Logout { logout ->
                logout
                    .logoutSuccessHandler(oidcLogoutSuccessHandler())
            }
            .formLogin { form -> form.disable() }
            .httpBasic { basic -> basic.disable() }
            .build()
    }
    
    @Bean
    fun authorizedClientRepository(): ServerOAuth2AuthorizedClientRepository {
        return WebSessionServerOAuth2AuthorizedClientRepository()
    }
    
    @Bean
    fun tokenResponseClient(): WebClientReactiveAuthorizationCodeTokenResponseClient {
        return WebClientReactiveAuthorizationCodeTokenResponseClient()
    }
    
    @Bean
    fun oidcLogoutSuccessHandler(): OidcClientInitiatedServerLogoutSuccessHandler {
        return OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository())
    }
    
    @Bean
    fun clientRegistrationRepository(): ReactiveClientRegistrationRepository {
        // Spring Boot가 자동으로 OAuth2 클라이언트 등록 정보를 구성
        return org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository()
    }
    
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
    
    /**
     * 인증 정보를 요청 컨텍스트에 추가하는 필터
     */
    @Bean
    @Order(1)
    fun authenticationContextFilter(): WebFilter {
        return WebFilter { exchange: ServerWebExchange, chain: WebFilterChain ->
            ReactiveSecurityContextHolder.getContext()
                .map { context: SecurityContext ->
                    val authentication = context.authentication
                    if (authentication is OAuth2AuthenticationToken) {
                        // OAuth2 인증 정보를 요청 속성에 추가
                        exchange.attributes["oauth2User"] = authentication.principal
                        exchange.attributes["oauth2Token"] = authentication.credentials
                    }
                    context
                }
                .defaultIfEmpty(ReactiveSecurityContextHolder.createEmptyContext())
                .flatMap { chain.filter(exchange) }
        }
    }
} 