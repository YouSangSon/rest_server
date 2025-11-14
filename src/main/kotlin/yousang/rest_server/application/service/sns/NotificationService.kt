package yousang.rest_server.application.service.sns

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.application.ports.out.NotificationRepositoryPort
import yousang.rest_server.domain.sns.Notification
import yousang.rest_server.domain.sns.NotificationType
import java.time.LocalDateTime

/**
 * 알림 서비스
 */
@Service
@Transactional
class NotificationService(
    private val notificationRepository: NotificationRepositoryPort
) {
    /**
     * 좋아요 알림 생성
     */
    fun createLikeNotification(recipientId: Long, sourceUserId: Long, postId: Long) {
        val notification = Notification(
            notificationId = 0,
            userId = recipientId,
            notificationType = NotificationType.LIKE,
            sourceUserId = sourceUserId,
            relatedContentType = "post",
            relatedContentId = postId,
            title = "New Like",
            message = "Someone liked your post",
            createdAt = LocalDateTime.now()
        )
        notificationRepository.save(notification)
    }

    /**
     * 댓글 알림 생성
     */
    fun createCommentNotification(recipientId: Long, sourceUserId: Long, postId: Long) {
        val notification = Notification(
            notificationId = 0,
            userId = recipientId,
            notificationType = NotificationType.COMMENT,
            sourceUserId = sourceUserId,
            relatedContentType = "post",
            relatedContentId = postId,
            title = "New Comment",
            message = "Someone commented on your post",
            createdAt = LocalDateTime.now()
        )
        notificationRepository.save(notification)
    }

    /**
     * 팔로우 알림 생성
     */
    fun createFollowNotification(recipientId: Long, sourceUserId: Long) {
        val notification = Notification(
            notificationId = 0,
            userId = recipientId,
            notificationType = NotificationType.FOLLOW,
            sourceUserId = sourceUserId,
            title = "New Follower",
            message = "Someone started following you",
            createdAt = LocalDateTime.now()
        )
        notificationRepository.save(notification)
    }

    /**
     * 가격 알림 생성
     */
    fun createPriceAlertNotification(
        userId: Long,
        symbol: String,
        currentPrice: Double,
        condition: String
    ) {
        val notification = Notification(
            notificationId = 0,
            userId = userId,
            notificationType = NotificationType.PRICE_ALERT,
            title = "Price Alert: $symbol",
            message = "$symbol has reached $currentPrice ($condition)",
            dataPayload = mapOf(
                "symbol" to symbol,
                "price" to currentPrice,
                "condition" to condition
            ),
            createdAt = LocalDateTime.now()
        )
        notificationRepository.save(notification)
    }

    /**
     * 알림 목록 조회
     */
    @Transactional(readOnly = true)
    fun getNotifications(userId: Long, limit: Int = 50, offset: Int = 0): List<Notification> {
        return notificationRepository.findByUserId(userId, limit, offset)
    }

    /**
     * 알림 읽음 표시
     */
    fun markAsRead(notificationId: Long, userId: Long) {
        val notification = notificationRepository.findById(notificationId)
            ?: throw IllegalArgumentException("Notification not found")

        if (notification.userId != userId) {
            throw IllegalArgumentException("Not authorized")
        }

        notificationRepository.markAsRead(notificationId)
    }

    /**
     * 알림 삭제
     */
    fun deleteNotification(notificationId: Long, userId: Long) {
        val notification = notificationRepository.findById(notificationId)
            ?: throw IllegalArgumentException("Notification not found")

        if (notification.userId != userId) {
            throw IllegalArgumentException("Not authorized")
        }

        notificationRepository.delete(notificationId)
    }

    /**
     * 읽지 않은 알림 개수
     */
    @Transactional(readOnly = true)
    fun getUnreadCount(userId: Long): Long {
        return notificationRepository.countUnread(userId)
    }
}
