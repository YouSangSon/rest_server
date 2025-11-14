package yousang.rest_server.domain.sns

import java.time.LocalDateTime

/**
 * SNS 사용자 도메인 모델
 * 소셜 미디어 및 투자 커뮤니티 사용자를 나타냄
 */
data class SnsUser(
    val userId: Long,
    val email: String,
    val username: String,
    val fullName: String,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val emailVerified: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(fullName.isNotBlank()) { "Full name cannot be blank" }
        require(username.length >= 3) { "Username must be at least 3 characters" }
        require(username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            "Username can only contain letters, numbers, and underscores"
        }
    }

    /**
     * 사용자 프로필 업데이트
     */
    fun updateProfile(
        fullName: String? = null,
        bio: String? = null,
        profileImageUrl: String? = null
    ): SnsUser {
        return copy(
            fullName = fullName ?: this.fullName,
            bio = bio ?: this.bio,
            profileImageUrl = profileImageUrl ?: this.profileImageUrl,
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * 팔로워 수 증가
     */
    fun incrementFollowers(): SnsUser = copy(followerCount = followerCount + 1)

    /**
     * 팔로워 수 감소
     */
    fun decrementFollowers(): SnsUser = copy(followerCount = maxOf(0, followerCount - 1))

    /**
     * 팔로잉 수 증가
     */
    fun incrementFollowing(): SnsUser = copy(followingCount = followingCount + 1)

    /**
     * 팔로잉 수 감소
     */
    fun decrementFollowing(): SnsUser = copy(followingCount = maxOf(0, followingCount - 1))

    /**
     * 게시물 수 증가
     */
    fun incrementPosts(): SnsUser = copy(postCount = postCount + 1)

    /**
     * 게시물 수 감소
     */
    fun decrementPosts(): SnsUser = copy(postCount = maxOf(0, postCount - 1))

    /**
     * 이메일 인증 완료
     */
    fun verifyEmail(): SnsUser = copy(emailVerified = true, updatedAt = LocalDateTime.now())

    /**
     * 계정 활성화/비활성화
     */
    fun setActive(active: Boolean): SnsUser = copy(isActive = active, updatedAt = LocalDateTime.now())
}
