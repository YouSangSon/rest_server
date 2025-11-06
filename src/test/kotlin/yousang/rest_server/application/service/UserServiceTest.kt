package yousang.rest_server.application.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.crypto.password.PasswordEncoder
import yousang.rest_server.application.ports.`in`.LoginCommand
import yousang.rest_server.application.ports.`in`.RegisterUserCommand
import yousang.rest_server.application.ports.`in`.UpdateUserCommand
import yousang.rest_server.application.ports.out.UserRepositoryPort
import yousang.rest_server.config.security.JwtTokenProvider
import yousang.rest_server.domain.exception.ConflictException
import yousang.rest_server.domain.exception.NotFoundException
import yousang.rest_server.domain.exception.UnauthorizedException
import yousang.rest_server.domain.model.User
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserServiceTest {

    private lateinit var userService: UserService
    private lateinit var userRepository: UserRepositoryPort
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setup() {
        userRepository = mock()
        passwordEncoder = mock()
        jwtTokenProvider = mock()
        userService = UserService(userRepository, passwordEncoder, jwtTokenProvider)
    }

    @Test
    fun `should register new user successfully`() {
        // Given
        val command = RegisterUserCommand(
            username = "testuser",
            email = "test@example.com",
            password = "password123"
        )
        val encodedPassword = "encoded-password"

        whenever(userRepository.existsByUsername(command.username)).thenReturn(false)
        whenever(userRepository.existsByEmail(command.email)).thenReturn(false)
        whenever(passwordEncoder.encode(command.password)).thenReturn(encodedPassword)
        whenever(userRepository.save(any())).thenAnswer { it.getArgument(0) }

        // When
        val result = userService.register(command)

        // Then
        assertNotNull(result)
        assertEquals(command.username, result.username)
        assertEquals(command.email, result.email)
        verify(userRepository).save(any())
    }

    @Test
    fun `should throw ConflictException when username already exists`() {
        // Given
        val command = RegisterUserCommand(
            username = "existinguser",
            email = "test@example.com",
            password = "password123"
        )
        whenever(userRepository.existsByUsername(command.username)).thenReturn(true)

        // When & Then
        assertThrows<ConflictException> {
            userService.register(command)
        }
    }

    @Test
    fun `should throw ConflictException when email already exists`() {
        // Given
        val command = RegisterUserCommand(
            username = "testuser",
            email = "existing@example.com",
            password = "password123"
        )
        whenever(userRepository.existsByUsername(command.username)).thenReturn(false)
        whenever(userRepository.existsByEmail(command.email)).thenReturn(true)

        // When & Then
        assertThrows<ConflictException> {
            userService.register(command)
        }
    }

    @Test
    fun `should authenticate user successfully`() {
        // Given
        val command = LoginCommand(username = "testuser", password = "password123")
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            password = "encoded-password"
        )
        val accessToken = "access-token"
        val refreshToken = "refresh-token"

        whenever(userRepository.findByUsername(command.username)).thenReturn(user)
        whenever(passwordEncoder.matches(command.password, user.password)).thenReturn(true)
        whenever(jwtTokenProvider.generateToken(eq(user.username), any())).thenReturn(accessToken)
        whenever(jwtTokenProvider.generateRefreshToken(user.username)).thenReturn(refreshToken)

        // When
        val result = userService.authenticate(command)

        // Then
        assertNotNull(result)
        assertEquals(accessToken, result.accessToken)
        assertEquals(refreshToken, result.refreshToken)
        assertEquals(user, result.user)
    }

    @Test
    fun `should throw UnauthorizedException when user not found`() {
        // Given
        val command = LoginCommand(username = "nonexistent", password = "password123")
        whenever(userRepository.findByUsername(command.username)).thenReturn(null)

        // When & Then
        assertThrows<UnauthorizedException> {
            userService.authenticate(command)
        }
    }

    @Test
    fun `should throw UnauthorizedException when password is incorrect`() {
        // Given
        val command = LoginCommand(username = "testuser", password = "wrongpassword")
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            password = "encoded-password"
        )

        whenever(userRepository.findByUsername(command.username)).thenReturn(user)
        whenever(passwordEncoder.matches(command.password, user.password)).thenReturn(false)

        // When & Then
        assertThrows<UnauthorizedException> {
            userService.authenticate(command)
        }
    }

    @Test
    fun `should update user successfully`() {
        // Given
        val userId = 1L
        val existingUser = User(
            id = userId,
            username = "olduser",
            email = "old@example.com",
            password = "password"
        )
        val command = UpdateUserCommand(username = "newuser", email = "new@example.com")

        whenever(userRepository.findById(userId)).thenReturn(existingUser)
        whenever(userRepository.existsByUsername("newuser")).thenReturn(false)
        whenever(userRepository.existsByEmail("new@example.com")).thenReturn(false)
        whenever(userRepository.save(any())).thenAnswer { it.getArgument(0) }

        // When
        val result = userService.updateUser(userId, command)

        // Then
        assertEquals("newuser", result.username)
        assertEquals("new@example.com", result.email)
    }

    @Test
    fun `should throw NotFoundException when updating non-existent user`() {
        // Given
        val userId = 999L
        val command = UpdateUserCommand(username = "newuser", email = "new@example.com")
        whenever(userRepository.findById(userId)).thenReturn(null)

        // When & Then
        assertThrows<NotFoundException> {
            userService.updateUser(userId, command)
        }
    }

    @Test
    fun `should delete user successfully`() {
        // Given
        val userId = 1L
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            password = "password"
        )
        whenever(userRepository.findById(userId)).thenReturn(user)

        // When
        userService.deleteUser(userId)

        // Then
        verify(userRepository).delete(userId)
    }

    @Test
    fun `should throw NotFoundException when deleting non-existent user`() {
        // Given
        val userId = 999L
        whenever(userRepository.findById(userId)).thenReturn(null)

        // When & Then
        assertThrows<NotFoundException> {
            userService.deleteUser(userId)
        }
    }
}
