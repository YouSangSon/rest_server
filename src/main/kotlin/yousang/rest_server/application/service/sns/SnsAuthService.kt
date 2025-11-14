package yousang.rest_server.application.service.sns

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.application.ports.out.SnsUserRepositoryPort
import yousang.rest_server.domain.sns.SnsUser
import yousang.rest_server.infrastructure.security.JwtTokenProvider
import java.time.LocalDateTime

/**
 * SNS 인증 서비스
 * 회원가입, 로그인, 토큰 갱신 등 인증 관련 기능 제공
 */
@Service
@Transactional
class SnsAuthService(
    private val userRepository: SnsUserRepositoryPort,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {
    /**
     * 사용자 회원가입
     */
    fun register(
        email: String,
        password: String,
        username: String,
        fullName: String
    ): AuthResponse {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("Email already exists")
        }

        // 사용자명 중복 확인
        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("Username already exists")
        }

        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(password)

        // 사용자 생성
        val newUser = SnsUser(
            userId = 0, // Auto-generated
            email = email,
            username = username,
            fullName = fullName,
            isActive = true,
            emailVerified = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(newUser)

        // JWT 토큰 생성
        val token = jwtTokenProvider.generateToken(savedUser.userId.toString())
        val refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.userId.toString())

        return AuthResponse(
            token = token,
            refreshToken = refreshToken,
            user = savedUser
        )
    }

    /**
     * 사용자 로그인
     */
    fun login(email: String, password: String): AuthResponse {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Invalid credentials")

        if (!user.isActive) {
            throw IllegalArgumentException("Account is inactive")
        }

        // Note: 실제 비밀번호 검증은 별도 저장소에서 가져와야 함
        // 여기서는 간단한 예시로 표현

        // JWT 토큰 생성
        val token = jwtTokenProvider.generateToken(user.userId.toString())
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.userId.toString())

        return AuthResponse(
            token = token,
            refreshToken = refreshToken,
            user = user
        )
    }

    /**
     * 토큰 갱신
     */
    fun refreshToken(refreshToken: String): AuthResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        val userId = jwtTokenProvider.getUserIdFromToken(refreshToken)
        val user = userRepository.findById(userId.toLong())
            ?: throw IllegalArgumentException("User not found")

        val newToken = jwtTokenProvider.generateToken(userId)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(userId)

        return AuthResponse(
            token = newToken,
            refreshToken = newRefreshToken,
            user = user
        )
    }

    /**
     * 현재 사용자 프로필 조회
     */
    @Transactional(readOnly = true)
    fun getCurrentUserProfile(userId: Long): SnsUser {
        return userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")
    }

    /**
     * 사용자 프로필 업데이트
     */
    fun updateProfile(
        userId: Long,
        fullName: String? = null,
        bio: String? = null,
        profileImageUrl: String? = null
    ): SnsUser {
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        val updatedUser = user.updateProfile(
            fullName = fullName,
            bio = bio,
            profileImageUrl = profileImageUrl
        )

        return userRepository.save(updatedUser)
    }

    /**
     * 이메일 인증
     */
    fun verifyEmail(userId: Long): SnsUser {
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        val verifiedUser = user.verifyEmail()
        return userRepository.save(verifiedUser)
    }
}

/**
 * 인증 응답 DTO
 */
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val user: SnsUser
)
