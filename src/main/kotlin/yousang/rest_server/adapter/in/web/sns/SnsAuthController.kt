package yousang.rest_server.adapter.`in`.web.sns

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import yousang.rest_server.application.service.sns.SnsAuthService
import yousang.rest_server.domain.sns.SnsUser

/**
 * SNS 인증 API 컨트롤러
 * /api/v1/sns/auth
 */
@RestController
@RequestMapping("/api/v1/sns/auth")
class SnsAuthController(
    private val authService: SnsAuthService
) {
    /**
     * POST /api/v1/sns/auth/register
     * 사용자 회원가입
     */
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponseDto> {
        val response = authService.register(
            email = request.email,
            password = request.password,
            username = request.username,
            fullName = request.fullName
        )

        return ResponseEntity.ok(AuthResponseDto.from(response))
    }

    /**
     * POST /api/v1/sns/auth/login
     * 사용자 로그인
     */
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponseDto> {
        val response = authService.login(
            email = request.email,
            password = request.password
        )

        return ResponseEntity.ok(AuthResponseDto.from(response))
    }

    /**
     * POST /api/v1/sns/auth/refresh
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponseDto> {
        val response = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(AuthResponseDto.from(response))
    }

    /**
     * GET /api/v1/sns/auth/profile
     * 현재 사용자 프로필 조회
     */
    @GetMapping("/profile")
    fun getProfile(@RequestAttribute("userId") userId: Long): ResponseEntity<SnsUserDto> {
        val user = authService.getCurrentUserProfile(userId)
        return ResponseEntity.ok(SnsUserDto.from(user))
    }

    /**
     * PUT /api/v1/sns/auth/profile
     * 사용자 프로필 업데이트
     */
    @PutMapping("/profile")
    fun updateProfile(
        @RequestAttribute("userId") userId: Long,
        @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<SnsUserDto> {
        val user = authService.updateProfile(
            userId = userId,
            fullName = request.fullName,
            bio = request.bio,
            profileImageUrl = request.profileImageUrl
        )
        return ResponseEntity.ok(SnsUserDto.from(user))
    }

    /**
     * POST /api/v1/sns/auth/logout
     * 로그아웃
     */
    @PostMapping("/logout")
    fun logout(@RequestAttribute("userId") userId: Long): ResponseEntity<Map<String, String>> {
        // JWT는 stateless이므로 클라이언트에서 토큰 삭제
        return ResponseEntity.ok(mapOf("message" to "Logged out successfully"))
    }
}

// Request DTOs
data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String,
    val fullName: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class UpdateProfileRequest(
    val fullName: String?,
    val bio: String?,
    val profileImageUrl: String?
)

// Response DTOs
data class AuthResponseDto(
    val token: String,
    val refreshToken: String,
    val user: SnsUserDto
) {
    companion object {
        fun from(response: yousang.rest_server.application.service.sns.AuthResponse): AuthResponseDto {
            return AuthResponseDto(
                token = response.token,
                refreshToken = response.refreshToken,
                user = SnsUserDto.from(response.user)
            )
        }
    }
}

data class SnsUserDto(
    val userId: Long,
    val email: String,
    val username: String,
    val fullName: String,
    val bio: String?,
    val profileImageUrl: String?,
    val followerCount: Int,
    val followingCount: Int,
    val postCount: Int,
    val isVerified: Boolean
) {
    companion object {
        fun from(user: SnsUser): SnsUserDto {
            return SnsUserDto(
                userId = user.userId,
                email = user.email,
                username = user.username,
                fullName = user.fullName,
                bio = user.bio,
                profileImageUrl = user.profileImageUrl,
                followerCount = user.followerCount,
                followingCount = user.followingCount,
                postCount = user.postCount,
                isVerified = user.isVerified
            )
        }
    }
}
