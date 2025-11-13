package yousang.rest_server.domain.sns

import java.time.LocalDateTime

/**
 * 댓글 도메인 모델
 * 게시물에 대한 댓글 및 대댓글 지원
 */
data class Comment(
    val commentId: Long,
    val postId: Long,
    val userId: Long,
    val parentCommentId: Long? = null,
    val content: String,
    val likeCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val MAX_CONTENT_LENGTH = 1000
    }

    init {
        require(content.isNotBlank()) { "Comment content cannot be blank" }
        require(content.length <= MAX_CONTENT_LENGTH) {
            "Comment cannot exceed $MAX_CONTENT_LENGTH characters"
        }
    }

    /**
     * 대댓글인지 확인
     */
    fun isReply(): Boolean = parentCommentId != null

    /**
     * 댓글 내용 업데이트
     */
    fun update(content: String): Comment {
        require(content.isNotBlank()) { "Comment content cannot be blank" }
        require(content.length <= MAX_CONTENT_LENGTH) {
            "Comment cannot exceed $MAX_CONTENT_LENGTH characters"
        }
        return copy(content = content, updatedAt = LocalDateTime.now())
    }

    /**
     * 좋아요 수 증가
     */
    fun incrementLikes(): Comment = copy(likeCount = likeCount + 1)

    /**
     * 좋아요 수 감소
     */
    fun decrementLikes(): Comment = copy(likeCount = maxOf(0, likeCount - 1))
}

/**
 * 팔로우 관계 도메인 모델
 */
data class Follow(
    val followId: Long,
    val followerId: Long,
    val followingId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(followerId != followingId) { "Cannot follow yourself" }
    }
}

/**
 * 좋아요 도메인 모델
 */
data class Like(
    val likeId: Long,
    val postId: Long,
    val userId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
