package yousang.rest_server.domain.sns

import java.time.LocalDateTime

/**
 * SNS 게시물 도메인 모델
 * 사용자가 작성한 소셜 미디어 게시물 (이미지, 캡션, 위치 등 포함)
 */
data class Post(
    val postId: Long,
    val userId: Long,
    val caption: String,
    val imageUrls: List<String> = emptyList(),
    val location: String? = null,
    val hashtags: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val bookmarkCount: Int = 0,
    val viewCount: Int = 0,
    val isHidden: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val MAX_IMAGES = 10
        const val MAX_CAPTION_LENGTH = 2200
    }

    init {
        require(caption.length <= MAX_CAPTION_LENGTH) {
            "Caption cannot exceed $MAX_CAPTION_LENGTH characters"
        }
        require(imageUrls.size <= MAX_IMAGES) {
            "Cannot upload more than $MAX_IMAGES images"
        }
    }

    /**
     * 해시태그 자동 추출
     */
    fun extractHashtags(): Post {
        val extractedHashtags = Regex("#(\\w+)")
            .findAll(caption)
            .map { it.groupValues[1].lowercase() }
            .distinct()
            .toList()

        return copy(hashtags = extractedHashtags)
    }

    /**
     * 게시물 업데이트 (캡션, 위치)
     */
    fun update(caption: String? = null, location: String? = null): Post {
        val newCaption = caption ?: this.caption
        val updatedPost = copy(
            caption = newCaption,
            location = location ?: this.location,
            updatedAt = LocalDateTime.now()
        )
        return if (caption != null) updatedPost.extractHashtags() else updatedPost
    }

    /**
     * 좋아요 수 증가
     */
    fun incrementLikes(): Post = copy(likeCount = likeCount + 1)

    /**
     * 좋아요 수 감소
     */
    fun decrementLikes(): Post = copy(likeCount = maxOf(0, likeCount - 1))

    /**
     * 댓글 수 증가
     */
    fun incrementComments(): Post = copy(commentCount = commentCount + 1)

    /**
     * 댓글 수 감소
     */
    fun decrementComments(): Post = copy(commentCount = maxOf(0, commentCount - 1))

    /**
     * 북마크 수 증가
     */
    fun incrementBookmarks(): Post = copy(bookmarkCount = bookmarkCount + 1)

    /**
     * 북마크 수 감소
     */
    fun decrementBookmarks(): Post = copy(bookmarkCount = maxOf(0, bookmarkCount - 1))

    /**
     * 조회 수 증가
     */
    fun incrementViews(): Post = copy(viewCount = viewCount + 1)

    /**
     * 게시물 숨기기/표시
     */
    fun hide(hidden: Boolean = true): Post = copy(isHidden = hidden, updatedAt = LocalDateTime.now())
}
