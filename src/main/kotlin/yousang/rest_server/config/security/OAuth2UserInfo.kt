package yousang.rest_server.config.security

import yousang.rest_server.domain.model.OAuth2Provider

/**
 * Interface for OAuth2 user information
 */
interface OAuth2UserInfo {
    fun getProvider(): OAuth2Provider
    fun getProviderId(): String
    fun getEmail(): String
    fun getName(): String
    fun getProfileImage(): String?
}

/**
 * Google OAuth2 user information
 */
class GoogleOAuth2UserInfo(
    private val attributes: Map<String, Any>
) : OAuth2UserInfo {
    override fun getProvider() = OAuth2Provider.GOOGLE
    override fun getProviderId(): String = attributes["sub"] as String
    override fun getEmail(): String = attributes["email"] as String
    override fun getName(): String = attributes["name"] as String
    override fun getProfileImage(): String? = attributes["picture"] as? String
}

/**
 * Naver OAuth2 user information
 */
class NaverOAuth2UserInfo(
    private val attributes: Map<String, Any>
) : OAuth2UserInfo {
    private val response: Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        get() = attributes["response"] as Map<String, Any>

    override fun getProvider() = OAuth2Provider.NAVER
    override fun getProviderId(): String = response["id"] as String
    override fun getEmail(): String = response["email"] as String
    override fun getName(): String = response["name"] as String
    override fun getProfileImage(): String? = response["profile_image"] as? String
}

/**
 * Kakao OAuth2 user information
 */
class KakaoOAuth2UserInfo(
    private val attributes: Map<String, Any>
) : OAuth2UserInfo {
    private val kakaoAccount: Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        get() = attributes["kakao_account"] as Map<String, Any>

    private val profile: Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        get() = kakaoAccount["profile"] as Map<String, Any>

    override fun getProvider() = OAuth2Provider.KAKAO
    override fun getProviderId(): String = attributes["id"].toString()
    override fun getEmail(): String = kakaoAccount["email"] as String
    override fun getName(): String = profile["nickname"] as String
    override fun getProfileImage(): String? = profile["profile_image_url"] as? String
}

/**
 * Factory for creating OAuth2UserInfo instances
 */
object OAuth2UserInfoFactory {
    fun getOAuth2UserInfo(
        registrationId: String,
        attributes: Map<String, Any>
    ): OAuth2UserInfo {
        return when (registrationId.lowercase()) {
            "google" -> GoogleOAuth2UserInfo(attributes)
            "naver" -> NaverOAuth2UserInfo(attributes)
            "kakao" -> KakaoOAuth2UserInfo(attributes)
            else -> throw IllegalArgumentException("Unsupported OAuth2 provider: $registrationId")
        }
    }
}
