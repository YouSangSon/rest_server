package yousang.rest_server.adapter.out.persistence.sns.entity

import jakarta.persistence.*
import yousang.rest_server.domain.sns.SnsUser
import java.time.LocalDateTime

@Entity
@Table(
    name = "sns_users",
    indexes = [
        Index(name = "idx_email", columnList = "email", unique = true),
        Index(name = "idx_username", columnList = "username", unique = true),
        Index(name = "idx_created_at", columnList = "created_at")
    ]
)
class SnsUserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val userId: Long = 0,

    @Column(nullable = false, unique = true, length = 255)
    val email: String,

    @Column(nullable = false, unique = true, length = 50)
    val username: String,

    @Column(name = "full_name", nullable = false, length = 100)
    val fullName: String,

    @Column(columnDefinition = "TEXT")
    val bio: String? = null,

    @Column(name = "profile_image_url", length = 500)
    val profileImageUrl: String? = null,

    @Column(name = "follower_count", nullable = false)
    val followerCount: Int = 0,

    @Column(name = "following_count", nullable = false)
    val followingCount: Int = 0,

    @Column(name = "post_count", nullable = false)
    val postCount: Int = 0,

    @Column(name = "is_verified", nullable = false)
    val isVerified: Boolean = false,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "email_verified", nullable = false)
    val emailVerified: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): SnsUser = SnsUser(
        userId = userId,
        email = email,
        username = username,
        fullName = fullName,
        bio = bio,
        profileImageUrl = profileImageUrl,
        followerCount = followerCount,
        followingCount = followingCount,
        postCount = postCount,
        isVerified = isVerified,
        isActive = isActive,
        emailVerified = emailVerified,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun from(domain: SnsUser): SnsUserEntity = SnsUserEntity(
            userId = domain.userId,
            email = domain.email,
            username = domain.username,
            fullName = domain.fullName,
            bio = domain.bio,
            profileImageUrl = domain.profileImageUrl,
            followerCount = domain.followerCount,
            followingCount = domain.followingCount,
            postCount = domain.postCount,
            isVerified = domain.isVerified,
            isActive = domain.isActive,
            emailVerified = domain.emailVerified,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
