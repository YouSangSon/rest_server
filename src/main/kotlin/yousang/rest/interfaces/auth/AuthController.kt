package yousang.rest.interfaces.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import yousang.rest.application.auth.OAuth2Service
import yousang.rest.domain.user.UserEntity
import yousang.rest.domain.user.UserRepository
import yousang.rest.interfaces.ApiResponse
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "인증", description = "사용자 인증 및 OAuth2 소셜 로그인 API")
class AuthController(
    private val oAuth2Service: OAuth2Service,
    private val userRepository: UserRepository
) {
    
    @GetMapping("/me")
    @Operation(summary = "현재 로그인한 사용자 정보 조회", description = "OAuth2 인증된 사용자의 정보를 반환합니다.")
    suspend fun getCurrentUser(
        @AuthenticationPrincipal oauth2User: OAuth2User?,
        exchange: ServerWebExchange
    ): ResponseEntity<ApiResponse<UserDto>> {
        return try {
            if (oauth2User == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse(
                        statusCode = HttpStatus.UNAUTHORIZED.value(),
                        message = "인증되지 않은 사용자입니다.",
                        data = null
                    )
                )
            }
            
            // OAuth2 사용자 정보를 UserEntity로 변환
            val provider = exchange.attributes["oauth2Provider"] as? String ?: "unknown"
            val user = oAuth2Service.convertOAuth2UserToEntity(oauth2User, provider)
            
            val userDto = user.toDto()
            
            ResponseEntity.ok(ApiResponse(
                statusCode = HttpStatus.OK.value(),
                message = "사용자 정보가 성공적으로 조회되었습니다.",
                data = userDto
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "사용자 정보 조회 중 오류가 발생했습니다: ${e.message}",
                    data = null
                )
            )
        }
    }
    
    @PostMapping("/logout")
    @Operation(summary = "사용자 로그아웃", description = "현재 로그인한 사용자를 로그아웃시킵니다.")
    suspend fun logout(
        @AuthenticationPrincipal oauth2User: OAuth2User?
    ): ResponseEntity<ApiResponse<LogoutResponse>> {
        return try {
            if (oauth2User == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse(
                        statusCode = HttpStatus.UNAUTHORIZED.value(),
                        message = "인증되지 않은 사용자입니다.",
                        data = null
                    )
                )
            }
            
            // 로그아웃 처리 (세션 무효화는 Spring Security가 자동으로 처리)
            val logoutResponse = LogoutResponse(
                message = "로그아웃이 성공적으로 완료되었습니다."
            )
            
            ResponseEntity.ok(ApiResponse(
                statusCode = HttpStatus.OK.value(),
                message = "로그아웃이 성공적으로 완료되었습니다.",
                data = logoutResponse
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "로그아웃 중 오류가 발생했습니다: ${e.message}",
                    data = null
                )
            )
        }
    }
    
    @PutMapping("/profile")
    @Operation(summary = "사용자 프로필 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
    suspend fun updateProfile(
        @RequestBody request: UserProfileUpdateRequest,
        @AuthenticationPrincipal oauth2User: OAuth2User?,
        exchange: ServerWebExchange
    ): ResponseEntity<ApiResponse<UserProfileResponse>> {
        return try {
            if (oauth2User == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse(
                        statusCode = HttpStatus.UNAUTHORIZED.value(),
                        message = "인증되지 않은 사용자입니다.",
                        data = null
                    )
                )
            }
            
            // OAuth2 사용자 정보를 UserEntity로 변환
            val provider = exchange.attributes["oauth2Provider"] as? String ?: "unknown"
            val user = oAuth2Service.convertOAuth2UserToEntity(oauth2User, provider)
            
            // 프로필 정보 업데이트
            request.username?.let { user.username = it }
            request.profileImage?.let { user.profileImage = it }
            user.updatedAt = LocalDateTime.now()
            
            val updatedUser = userRepository.update(user)
            val userDto = updatedUser.toDto()
            
            val profileResponse = UserProfileResponse(
                user = userDto,
                message = "프로필이 성공적으로 업데이트되었습니다."
            )
            
            ResponseEntity.ok(ApiResponse(
                statusCode = HttpStatus.OK.value(),
                message = "프로필이 성공적으로 업데이트되었습니다.",
                data = profileResponse
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "프로필 업데이트 중 오류가 발생했습니다: ${e.message}",
                    data = null
                )
            )
        }
    }
    
    @GetMapping("/providers")
    @Operation(summary = "지원하는 OAuth2 제공자 목록", description = "현재 지원하는 OAuth2 소셜 로그인 제공자 목록을 반환합니다.")
    suspend fun getSupportedProviders(): ResponseEntity<ApiResponse<List<String>>> {
        return try {
            val providers = listOf("google", "github", "kakao")
            
            ResponseEntity.ok(ApiResponse(
                statusCode = HttpStatus.OK.value(),
                message = "지원하는 OAuth2 제공자 목록을 성공적으로 조회했습니다.",
                data = providers
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "OAuth2 제공자 목록 조회 중 오류가 발생했습니다: ${e.message}",
                    data = null
                )
            )
        }
    }
}

// UserEntity를 UserDto로 변환하는 확장 함수
fun UserEntity.toDto(): UserDto {
    return UserDto(
        id = id.value,
        email = email,
        username = username,
        provider = provider,
        providerId = providerId,
        profileImage = profileImage,
        isEnabled = isEnabled,
        lastLoginAt = lastLoginAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
