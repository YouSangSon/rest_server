package yousang.rest_server.application.service

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.application.ports.`in`.*
import yousang.rest_server.application.ports.out.UserRepositoryPort
import yousang.rest_server.config.security.JwtTokenProvider
import yousang.rest_server.domain.exception.BadRequestException
import yousang.rest_server.domain.exception.ConflictException
import yousang.rest_server.domain.exception.NotFoundException
import yousang.rest_server.domain.exception.UnauthorizedException
import yousang.rest_server.domain.model.User

@Service
@Transactional
class UserService(
    private val userRepository: UserRepositoryPort,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) : RegisterUserUseCase, GetUserUseCase, UpdateUserUseCase, DeleteUserUseCase, AuthenticateUserUseCase {

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

        return userRepository.save(user)
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

        return userRepository.save(updatedUser)
    }

    @CacheEvict(value = ["users"], key = "#id")
    override fun deleteUser(id: Long) {
        if (userRepository.findById(id) == null) {
            throw NotFoundException("User with id $id not found")
        }
        userRepository.delete(id)
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

        return AuthenticationResult(
            user = user,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}
