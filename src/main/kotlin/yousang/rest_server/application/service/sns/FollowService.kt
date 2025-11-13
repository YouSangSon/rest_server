package yousang.rest_server.application.service.sns

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.application.ports.out.FollowRepositoryPort
import yousang.rest_server.application.ports.out.SnsUserRepositoryPort
import yousang.rest_server.domain.sns.Follow
import java.time.LocalDateTime

/**
 * 팔로우 서비스
 */
@Service
@Transactional
class FollowService(
    private val followRepository: FollowRepositoryPort,
    private val userRepository: SnsUserRepositoryPort,
    private val notificationService: NotificationService
) {
    /**
     * 사용자 팔로우
     */
    fun followUser(followerId: Long, followingId: Long) {
        if (followerId == followingId) {
            throw IllegalArgumentException("Cannot follow yourself")
        }

        // 이미 팔로우 중인지 확인
        if (followRepository.exists(followerId, followingId)) {
            throw IllegalArgumentException("Already following")
        }

        // 팔로우 대상 사용자 존재 확인
        val followingUser = userRepository.findById(followingId)
            ?: throw IllegalArgumentException("User not found")

        val followerUser = userRepository.findById(followerId)
            ?: throw IllegalArgumentException("User not found")

        // 팔로우 저장
        val follow = Follow(
            followId = 0,
            followerId = followerId,
            followingId = followingId,
            createdAt = LocalDateTime.now()
        )
        followRepository.save(follow)

        // 사용자 카운트 업데이트
        userRepository.save(followerUser.incrementFollowing())
        userRepository.save(followingUser.incrementFollowers())

        // 알림 전송
        notificationService.createFollowNotification(followingId, followerId)
    }

    /**
     * 사용자 언팔로우
     */
    fun unfollowUser(followerId: Long, followingId: Long) {
        if (!followRepository.delete(followerId, followingId)) {
            throw IllegalArgumentException("Follow relationship not found")
        }

        // 사용자 카운트 업데이트
        val followerUser = userRepository.findById(followerId)
        if (followerUser != null) {
            userRepository.save(followerUser.decrementFollowing())
        }

        val followingUser = userRepository.findById(followingId)
        if (followingUser != null) {
            userRepository.save(followingUser.decrementFollowers())
        }
    }

    /**
     * 팔로워 목록 조회
     */
    @Transactional(readOnly = true)
    fun getFollowers(userId: Long, limit: Int = 50, offset: Int = 0): List<Follow> {
        return followRepository.findFollowers(userId, limit, offset)
    }

    /**
     * 팔로잉 목록 조회
     */
    @Transactional(readOnly = true)
    fun getFollowing(userId: Long, limit: Int = 50, offset: Int = 0): List<Follow> {
        return followRepository.findFollowing(userId, limit, offset)
    }

    /**
     * 팔로우 여부 확인
     */
    @Transactional(readOnly = true)
    fun isFollowing(followerId: Long, followingId: Long): Boolean {
        return followRepository.exists(followerId, followingId)
    }
}
