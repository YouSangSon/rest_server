package yousang.rest_server.config.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException

/**
 * OAuth2 authentication success handler
 * Redirects to frontend with JWT token after successful OAuth2 login
 */
@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider
) : SimpleUrlAuthenticationSuccessHandler() {

    private val logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler::class.java)

    @Throws(IOException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val targetUrl = determineTargetUrl(request, response, authentication)

        if (response.isCommitted) {
            logger.debug("Response has already been committed. Unable to redirect to $targetUrl")
            return
        }

        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    protected fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ): String {
        val redirectUri = request.getParameter("redirect_uri") ?: getDefaultTargetUrl()

        val principal = authentication.principal as? OAuth2UserPrincipal
            ?: throw IllegalStateException("Unexpected principal type")

        val user = principal.getUser()
        val roles = user.roles.map { it.name }

        // Generate JWT tokens
        val accessToken = jwtTokenProvider.generateToken(user.username, roles)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.username)

        logger.info("OAuth2 login successful for user: ${user.username}")

        // Build redirect URL with tokens
        return UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("token", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build()
            .toUriString()
    }
}
