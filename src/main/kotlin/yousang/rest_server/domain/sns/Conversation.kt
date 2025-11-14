package yousang.rest_server.domain.sns

import java.time.LocalDateTime

/**
 * 대화 도메인 모델
 * 두 사용자 간의 1:1 대화
 */
data class Conversation(
    val conversationId: Long,
    val participant1Id: Long,
    val participant2Id: Long,
    val lastMessageAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(participant1Id != participant2Id) {
            "Conversation participants must be different"
        }
    }

    /**
     * 특정 사용자가 참여자인지 확인
     */
    fun isParticipant(userId: Long): Boolean {
        return userId == participant1Id || userId == participant2Id
    }

    /**
     * 상대방 ID 가져오기
     */
    fun getOtherParticipant(userId: Long): Long? {
        return when (userId) {
            participant1Id -> participant2Id
            participant2Id -> participant1Id
            else -> null
        }
    }

    /**
     * 마지막 메시지 시간 업데이트
     */
    fun updateLastMessageAt(timestamp: LocalDateTime): Conversation {
        return copy(lastMessageAt = timestamp)
    }
}

/**
 * 메시지 도메인 모델
 */
data class Message(
    val messageId: Long,
    val conversationId: Long,
    val senderId: Long,
    val content: String,
    val imageUrl: String? = null,
    val isRead: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val MAX_CONTENT_LENGTH = 2000
    }

    init {
        require(content.isNotBlank() || imageUrl != null) {
            "Message must have either content or image"
        }
        if (content.isNotBlank()) {
            require(content.length <= MAX_CONTENT_LENGTH) {
                "Message cannot exceed $MAX_CONTENT_LENGTH characters"
            }
        }
    }

    /**
     * 메시지 내용 업데이트
     */
    fun update(content: String): Message {
        require(content.isNotBlank()) { "Message content cannot be blank" }
        require(content.length <= MAX_CONTENT_LENGTH) {
            "Message cannot exceed $MAX_CONTENT_LENGTH characters"
        }
        return copy(content = content, updatedAt = LocalDateTime.now())
    }

    /**
     * 읽음 표시
     */
    fun markAsRead(): Message = copy(isRead = true)
}

/**
 * 스토리 도메인 모델
 * 24시간 동안 공개되는 임시 콘텐츠
 */
data class Story(
    val storyId: Long,
    val userId: Long,
    val mediaUrl: String,
    val mediaType: StoryMediaType,
    val caption: String? = null,
    val viewCount: Int = 0,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val STORY_LIFESPAN_HOURS = 24L
    }

    init {
        require(mediaUrl.isNotBlank()) { "Media URL cannot be blank" }
        require(expiresAt.isAfter(createdAt)) {
            "Expiration time must be after creation time"
        }
    }

    /**
     * 스토리가 만료되었는지 확인
     */
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }

    /**
     * 조회 수 증가
     */
    fun incrementViews(): Story = copy(viewCount = viewCount + 1)

    /**
     * 남은 시간 계산 (초)
     */
    fun getRemainingSeconds(): Long {
        val now = LocalDateTime.now()
        return if (now.isBefore(expiresAt)) {
            java.time.Duration.between(now, expiresAt).seconds
        } else {
            0L
        }
    }
}

/**
 * 스토리 미디어 유형
 */
enum class StoryMediaType {
    IMAGE,  // 이미지
    VIDEO   // 비디오
}

/**
 * 스토리 조회 기록
 */
data class StoryView(
    val storyViewId: Long,
    val storyId: Long,
    val viewerId: Long,
    val viewedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * 포트폴리오 팔로우 도메인 모델
 */
data class PortfolioFollower(
    val portfolioFollowerId: Long,
    val portfolioId: Long,
    val userId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
