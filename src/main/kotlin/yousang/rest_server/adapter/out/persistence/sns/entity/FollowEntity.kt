package yousang.rest_server.adapter.out.persistence.sns.entity

import jakarta.persistence.*
import yousang.rest_server.domain.sns.Follow
import java.time.LocalDateTime

@Entity
@Table(
    name = "sns_follows",
    indexes = [
        Index(name = "idx_follower_id", columnList = "follower_id"),
        Index(name = "idx_following_id", columnList = "following_id"),
        Index(name = "idx_created_at", columnList = "created_at")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_follower_following", columnNames = ["follower_id", "following_id"])
    ]
)
class FollowEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follow_id")
    val followId: Long = 0,

    @Column(name = "follower_id", nullable = false)
    val followerId: Long,

    @Column(name = "following_id", nullable = false)
    val followingId: Long,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Follow = Follow(
        followId = followId,
        followerId = followerId,
        followingId = followingId,
        createdAt = createdAt
    )

    companion object {
        fun from(domain: Follow): FollowEntity = FollowEntity(
            followId = domain.followId,
            followerId = domain.followerId,
            followingId = domain.followingId,
            createdAt = domain.createdAt
        )
    }
}
