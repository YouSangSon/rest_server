package yousang.rest_server.domain.sns

import java.time.LocalDateTime

/**
 * 투자 게시물 도메인 모델
 * 투자 아이디어, 성과, 거래, 분석 등을 공유하는 게시물
 */
data class InvestmentPost(
    val investmentPostId: Long,
    val userId: Long,
    val portfolioId: Long? = null,
    val postType: InvestmentPostType,
    val title: String,
    val content: String,
    val assetReferences: List<AssetReference> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val bookmarkCount: Int = 0,
    val viewCount: Int = 0,
    val voteCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val MAX_TITLE_LENGTH = 200
        const val MAX_CONTENT_LENGTH = 5000
    }

    init {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(title.length <= MAX_TITLE_LENGTH) {
            "Title cannot exceed $MAX_TITLE_LENGTH characters"
        }
        require(content.isNotBlank()) { "Content cannot be blank" }
        require(content.length <= MAX_CONTENT_LENGTH) {
            "Content cannot exceed $MAX_CONTENT_LENGTH characters"
        }
    }

    /**
     * 게시물 업데이트
     */
    fun update(
        title: String? = null,
        content: String? = null,
        assetReferences: List<AssetReference>? = null
    ): InvestmentPost {
        return copy(
            title = title ?: this.title,
            content = content ?: this.content,
            assetReferences = assetReferences ?: this.assetReferences,
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * 좋아요 수 증가
     */
    fun incrementLikes(): InvestmentPost = copy(likeCount = likeCount + 1)

    /**
     * 좋아요 수 감소
     */
    fun decrementLikes(): InvestmentPost = copy(likeCount = maxOf(0, likeCount - 1))

    /**
     * 댓글 수 증가
     */
    fun incrementComments(): InvestmentPost = copy(commentCount = commentCount + 1)

    /**
     * 댓글 수 감소
     */
    fun decrementComments(): InvestmentPost = copy(commentCount = maxOf(0, commentCount - 1))

    /**
     * 북마크 수 증가
     */
    fun incrementBookmarks(): InvestmentPost = copy(bookmarkCount = bookmarkCount + 1)

    /**
     * 북마크 수 감소
     */
    fun decrementBookmarks(): InvestmentPost = copy(bookmarkCount = maxOf(0, bookmarkCount - 1))

    /**
     * 조회 수 증가
     */
    fun incrementViews(): InvestmentPost = copy(viewCount = viewCount + 1)

    /**
     * 투표 수 증가
     */
    fun incrementVotes(): InvestmentPost = copy(voteCount = voteCount + 1)

    /**
     * 투표 수 감소
     */
    fun decrementVotes(): InvestmentPost = copy(voteCount = maxOf(0, voteCount - 1))
}

/**
 * 투자 게시물 유형
 */
enum class InvestmentPostType {
    IDEA,           // 투자 아이디어
    PERFORMANCE,    // 투자 성과
    TRADE,          // 거래 내역
    ANALYSIS        // 시장 분석
}

/**
 * 자산 참조 (투자 게시물에서 언급된 자산)
 */
data class AssetReference(
    val symbol: String,
    val assetType: AssetType
) {
    init {
        require(symbol.isNotBlank()) { "Symbol cannot be blank" }
    }
}
