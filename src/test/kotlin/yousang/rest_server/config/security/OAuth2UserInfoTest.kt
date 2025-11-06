package yousang.rest_server.config.security

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import yousang.rest_server.domain.model.OAuth2Provider
import kotlin.test.assertEquals

class OAuth2UserInfoTest {

    @Test
    fun `should create Google OAuth2UserInfo correctly`() {
        // Given
        val attributes = mapOf(
            "sub" to "google-user-id-123",
            "email" to "user@gmail.com",
            "name" to "John Doe",
            "picture" to "https://example.com/photo.jpg"
        )

        // When
        val userInfo = GoogleOAuth2UserInfo(attributes)

        // Then
        assertEquals(OAuth2Provider.GOOGLE, userInfo.getProvider())
        assertEquals("google-user-id-123", userInfo.getProviderId())
        assertEquals("user@gmail.com", userInfo.getEmail())
        assertEquals("John Doe", userInfo.getName())
        assertEquals("https://example.com/photo.jpg", userInfo.getProfileImage())
    }

    @Test
    fun `should create Naver OAuth2UserInfo correctly`() {
        // Given
        val attributes = mapOf(
            "response" to mapOf(
                "id" to "naver-user-id-456",
                "email" to "user@naver.com",
                "name" to "김철수",
                "profile_image" to "https://example.com/naver.jpg"
            )
        )

        // When
        val userInfo = NaverOAuth2UserInfo(attributes)

        // Then
        assertEquals(OAuth2Provider.NAVER, userInfo.getProvider())
        assertEquals("naver-user-id-456", userInfo.getProviderId())
        assertEquals("user@naver.com", userInfo.getEmail())
        assertEquals("김철수", userInfo.getName())
        assertEquals("https://example.com/naver.jpg", userInfo.getProfileImage())
    }

    @Test
    fun `should create Kakao OAuth2UserInfo correctly`() {
        // Given
        val attributes = mapOf(
            "id" to 789L,
            "kakao_account" to mapOf(
                "email" to "user@kakao.com",
                "profile" to mapOf(
                    "nickname" to "카카오유저",
                    "profile_image_url" to "https://example.com/kakao.jpg"
                )
            )
        )

        // When
        val userInfo = KakaoOAuth2UserInfo(attributes)

        // Then
        assertEquals(OAuth2Provider.KAKAO, userInfo.getProvider())
        assertEquals("789", userInfo.getProviderId())
        assertEquals("user@kakao.com", userInfo.getEmail())
        assertEquals("카카오유저", userInfo.getName())
        assertEquals("https://example.com/kakao.jpg", userInfo.getProfileImage())
    }

    @Test
    fun `should create correct OAuth2UserInfo using factory`() {
        // Given
        val googleAttributes = mapOf(
            "sub" to "google-id",
            "email" to "google@example.com",
            "name" to "Google User"
        )

        // When
        val googleUserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("google", googleAttributes)

        // Then
        assertEquals(OAuth2Provider.GOOGLE, googleUserInfo.getProvider())
        assertEquals("google@example.com", googleUserInfo.getEmail())
    }

    @Test
    fun `should throw exception for unsupported provider`() {
        // Given
        val attributes = mapOf("id" to "123")

        // When & Then
        assertThrows<IllegalArgumentException> {
            OAuth2UserInfoFactory.getOAuth2UserInfo("facebook", attributes)
        }
    }
}
