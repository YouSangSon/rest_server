package yousang.rest_server.domain.model

/**
 * OAuth2 Provider enumeration
 */
enum class OAuth2Provider {
    GOOGLE,
    NAVER,
    KAKAO,
    LOCAL;  // For regular username/password login

    companion object {
        fun fromRegistrationId(registrationId: String): OAuth2Provider {
            return when (registrationId.lowercase()) {
                "google" -> GOOGLE
                "naver" -> NAVER
                "kakao" -> KAKAO
                else -> LOCAL
            }
        }
    }
}
