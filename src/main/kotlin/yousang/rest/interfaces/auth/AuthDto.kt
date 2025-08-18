package yousang.rest.interfaces.auth

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class UserDto(
    val id: Long,
    val email: String,
    val username: String,
    val provider: String,
    val providerId: String?,
    val profileImage: String?,
    val isEnabled: Boolean,
    val lastLoginAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val user: UserDto,
    val accessToken: String,
    val tokenType: String = "Bearer"
)

data class OAuth2LoginRequest(
    val provider: String // google, github, kakao
)

data class OAuth2LoginResponse(
    val user: UserDto,
    val accessToken: String,
    val tokenType: String = "Bearer",
    val provider: String
)

data class LogoutRequest(
    val accessToken: String
)

data class LogoutResponse(
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

data class UserProfileUpdateRequest(
    val username: String?,
    val profileImage: String?
)

data class UserProfileResponse(
    val user: UserDto,
    val message: String
)
