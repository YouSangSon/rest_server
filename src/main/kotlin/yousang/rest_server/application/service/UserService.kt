package yousang.rest_server.application.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.adapter.out.kafka.KafkaProducerService
import yousang.rest_server.application.ports.`in`.*
import yousang.rest_server.application.ports.out.UserRepositoryPort
import yousang.rest_server.config.security.JwtTokenProvider
import yousang.rest_server.domain.exception.ConflictException
import yousang.rest_server.domain.exception.NotFoundException
import yousang.rest_server.domain.exception.UnauthorizedException
import yousang.rest_server.domain.model.User
import yousang.rest_server.domain.model.UserEvent
import yousang.rest_server.domain.model.UserEventType
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepositoryPort,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) : RegisterUserUseCase, GetUserUseCase, UpdateUserUseCase, DeleteUserUseCase, AuthenticateUserUseCase {

    @Autowired(required = false)
    private val kafkaProducerService: KafkaProducerService? = null

    @Autowired(required = false)
    private val notificationService: NotificationService? = null

    override fun register(command: RegisterUserCommand): User {
        // Check if username or email already exists
        if (userRepository.existsByUsername(command.username)) {
            throw ConflictException("Username '${command.username}' already exists")
        }
        if (userRepository.existsByEmail(command.email)) {
            throw ConflictException("Email '${command.email}' already exists")
        }

        // Create user with encoded password
        val user = User(
            username = command.username,
            email = command.email,
            password = command.password
        ).withEncodedPassword(passwordEncoder.encode(command.password))

        val savedUser = userRepository.save(user)

        // Publish user registered event
        publishUserEvent(savedUser, UserEventType.USER_REGISTERED)

        // Send welcome email
        notificationService?.sendWelcomeEmail(savedUser)

        return savedUser
    }

    @Cacheable(value = ["users"], key = "#id")
    @Transactional(readOnly = true)
    override fun getUserById(id: Long): User? {
        return userRepository.findById(id)
    }

    @Cacheable(value = ["users"], key = "#username")
    @Transactional(readOnly = true)
    override fun getUserByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    @Cacheable(value = ["users"], key = "#email")
    @Transactional(readOnly = true)
    override fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    @Transactional(readOnly = true)
    override fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    @CacheEvict(value = ["users"], key = "#id")
    override fun updateUser(id: Long, command: UpdateUserCommand): User {
        val existingUser = userRepository.findById(id)
            ?: throw NotFoundException("User with id $id not found")

        // Check if new username/email conflicts with other users
        command.username?.let { newUsername ->
            if (newUsername != existingUser.username && userRepository.existsByUsername(newUsername)) {
                throw ConflictException("Username '$newUsername' already exists")
            }
        }
        command.email?.let { newEmail ->
            if (newEmail != existingUser.email && userRepository.existsByEmail(newEmail)) {
                throw ConflictException("Email '$newEmail' already exists")
            }
        }

        val updatedUser = existingUser.updateProfile(
            username = command.username,
            email = command.email
        )

        val savedUser = userRepository.save(updatedUser)

        // Publish user updated event
        publishUserEvent(savedUser, UserEventType.USER_UPDATED)

        return savedUser
    }

    @CacheEvict(value = ["users"], key = "#id")
    override fun deleteUser(id: Long) {
        val user = userRepository.findById(id)
            ?: throw NotFoundException("User with id $id not found")

        userRepository.delete(id)

        // Publish user deleted event
        publishUserEvent(user, UserEventType.USER_DELETED)

        // Send account deleted email
        notificationService?.sendAccountDeletedEmail(user)
    }

    override fun authenticate(command: LoginCommand): AuthenticationResult {
        val user = userRepository.findByUsername(command.username)
            ?: throw UnauthorizedException("Invalid username or password")

        if (!passwordEncoder.matches(command.password, user.password)) {
            throw UnauthorizedException("Invalid username or password")
        }

        if (!user.enabled) {
            throw UnauthorizedException("User account is disabled")
        }

        val roles = user.roles.map { it.name }
        val accessToken = jwtTokenProvider.generateToken(user.username, roles)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.username)

        // Publish user logged in event
        publishUserEvent(user, UserEventType.USER_LOGGED_IN)

        return AuthenticationResult(
            user = user,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    private fun publishUserEvent(user: User, eventType: UserEventType) {
        kafkaProducerService?.let {
            val event = UserEvent(
                eventId = UUID.randomUUID().toString(),
                eventType = eventType,
                userId = user.id,
                username = user.username,
                email = user.email,
                timestamp = LocalDateTime.now()
            )
            it.publishUserEvent(event)
        }
    }
}
