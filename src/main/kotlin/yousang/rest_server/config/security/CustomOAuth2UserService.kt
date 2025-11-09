package yousang.rest_server.config.security

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import yousang.rest_server.adapter.out.kafka.KafkaProducerService
import yousang.rest_server.application.ports.out.UserRepositoryPort
import yousang.rest_server.application.service.NotificationService
import yousang.rest_server.domain.model.OAuth2Provider
import yousang.rest_server.domain.model.Role
import yousang.rest_server.domain.model.User
import yousang.rest_server.domain.model.UserEvent
import yousang.rest_server.domain.model.UserEventType
import java.time.LocalDateTime
import java.util.*

/**
 * Custom OAuth2 user service to handle social login
 */
@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepositoryPort
) : DefaultOAuth2UserService() {

    private val logger = LoggerFactory.getLogger(CustomOAuth2UserService::class.java)

    @Autowired(required = false)
    private val kafkaProducerService: KafkaProducerService? = null

    @Autowired(required = false)
    private val notificationService: NotificationService? = null

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        return processOAuth2User(userRequest, oAuth2User)
    }

    private fun processOAuth2User(userRequest: OAuth2UserRequest, oAuth2User: OAuth2User): OAuth2User {
        val registrationId = userRequest.clientRegistration.registrationId
        val attributes = oAuth2User.attributes

        logger.info("Processing OAuth2 user from provider: $registrationId")

        val oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes)

        // Check if email is present
        if (oauth2UserInfo.getEmail().isBlank()) {
            throw IllegalArgumentException("Email not found from OAuth2 provider")
        }

        // Find or create user
        val user = findOrCreateUser(oauth2UserInfo)

        return OAuth2UserPrincipal(user, attributes)
    }

    private fun findOrCreateUser(oauth2UserInfo: OAuth2UserInfo): User {
        val email = oauth2UserInfo.getEmail()
        val provider = oauth2UserInfo.getProvider()

        // Try to find existing user by email
        val existingUser = userRepository.findByEmail(email)

        return if (existingUser != null) {
            logger.info("Found existing user with email: $email")
            existingUser
        } else {
            logger.info("Creating new user with email: $email via OAuth2 provider: $provider")

            // Create new user with OAuth2 information
            val newUser = User(
                username = generateUsername(oauth2UserInfo),
                email = email,
                password = UUID.randomUUID().toString(), // Random password for OAuth2 users
                roles = setOf(Role.USER),
                enabled = true
            )

            val savedUser = userRepository.save(newUser)

            // Publish OAuth2 linked event
            publishOAuth2Event(savedUser, provider)

            // Send welcome email
            notificationService?.sendOAuth2WelcomeEmail(savedUser, provider)

            savedUser
        }
    }

    private fun publishOAuth2Event(user: User, provider: OAuth2Provider) {
        kafkaProducerService?.let {
            val event = UserEvent(
                eventId = UUID.randomUUID().toString(),
                eventType = UserEventType.OAUTH2_LINKED,
                userId = user.id,
                username = user.username,
                email = user.email,
                timestamp = LocalDateTime.now(),
                metadata = mapOf("provider" to provider.name)
            )
            it.publishUserEvent(event)
        }
    }

    private fun generateUsername(oauth2UserInfo: OAuth2UserInfo): String {
        val baseName = oauth2UserInfo.getName()
            .replace(" ", "_")
            .lowercase()

        // Check if username exists, if so, append provider and random number
        var username = baseName
        var counter = 1

        while (userRepository.existsByUsername(username)) {
            username = "${baseName}_${oauth2UserInfo.getProvider().name.lowercase()}_${counter}"
            counter++
        }

        return username
    }
}

/**
 * Custom OAuth2 user principal
 */
class OAuth2UserPrincipal(
    private val user: User,
    private val attributes: Map<String, Any>
) : OAuth2User {

    override fun getName(): String = user.username

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getAuthorities() = user.roles.map {
        org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_${it.name}")
    }

    fun getUser(): User = user
}
