package yousang.rest.application.auth

import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import yousang.rest.domain.user.UserEntity
import yousang.rest.domain.user.UserRepository
import java.time.LocalDateTime

@Service
class OAuth2Service(
    private val userRepository: UserRepository
) {
    
    /**
     * OAuth2 사용자 정보를 처리하고 사용자를 생성하거나 업데이트합니다.
     */
    suspend fun processOAuth2User(
        oauth2User: OAuth2User,
        provider: String
    ): UserEntity {
        val email = oauth2User.getAttribute<String>("email")
        val name = oauth2User.getAttribute<String>("name")
        val providerId = oauth2User.getName()
        val profileImage = getProfileImage(oauth2User, provider)
        
        // 이메일이 없는 경우 예외 발생
        if (email.isNullOrBlank()) {
            throw IllegalArgumentException("OAuth2 사용자의 이메일 정보를 가져올 수 없습니다.")
        }
        
        // 기존 사용자 확인 (이메일 또는 provider + providerId로)
        var user = userRepository.findByEmail(email)
        
        if (user == null) {
            // provider + providerId로도 확인
            user = userRepository.findByProviderAndProviderId(provider, providerId)
        }
        
        if (user != null) {
            // 기존 사용자 정보 업데이트
            user.username = name ?: user.username
            user.profileImage = profileImage ?: user.profileImage
            user.lastLoginAt = LocalDateTime.now()
            user.updatedAt = LocalDateTime.now()
            return userRepository.update(user)
        } else {
            // 새로운 OAuth2 사용자 생성
            val newUser = UserEntity.createOAuth2User(
                email = email,
                username = name ?: email.split("@")[0],
                provider = provider,
                providerId = providerId,
                profileImage = profileImage
            )
            newUser.lastLoginAt = LocalDateTime.now()
            return userRepository.save(newUser)
        }
    }
    
    /**
     * OAuth2 사용자 정보에서 프로필 이미지 URL을 추출합니다.
     */
    private fun getProfileImage(oauth2User: OAuth2User, provider: String): String? {
        return when (provider.lowercase()) {
            "google" -> oauth2User.getAttribute<String>("picture")
            "github" -> oauth2User.getAttribute<String>("avatar_url")
            "kakao" -> {
                val kakaoAccount = oauth2User.getAttribute<Map<String, Any>>("kakao_account")
                val profile = kakaoAccount?.get("profile") as? Map<String, Any>
                profile?.get("profile_image_url") as? String
            }
            else -> null
        }
    }
    
    /**
     * OAuth2 사용자 정보를 UserEntity로 변환합니다.
     */
    suspend fun convertOAuth2UserToEntity(
        oauth2User: OAuth2User,
        provider: String
    ): UserEntity {
        return processOAuth2User(oauth2User, provider)
    }
}
