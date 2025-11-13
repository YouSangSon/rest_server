package yousang.rest_server.adapter.`in`.web.sns

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import yousang.rest_server.application.service.sns.PostService
import yousang.rest_server.application.service.sns.CommentService
import yousang.rest_server.domain.sns.Post
import yousang.rest_server.domain.sns.Comment

/**
 * 게시물 API 컨트롤러
 * /api/v1/sns/posts
 */
@RestController
@RequestMapping("/api/v1/sns/posts")
class PostController(
    private val postService: PostService,
    private val commentService: CommentService
) {
    /**
     * GET /api/v1/sns/posts
     * 피드 조회
     */
    @GetMapping
    fun getFeed(
        @RequestAttribute("userId") userId: Long,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<FeedResponse> {
        val posts = postService.getFeed(userId, limit, offset)
        return ResponseEntity.ok(FeedResponse(
            data = posts.map { PostDto.from(it) },
            meta = PaginationMeta(limit, offset, posts.size >= limit)
        ))
    }

    /**
     * GET /api/v1/sns/posts/{postId}
     * 게시물 상세 조회
     */
    @GetMapping("/{postId}")
    fun getPost(
        @PathVariable postId: Long,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<PostDto> {
        val post = postService.getPost(postId)
        postService.incrementViews(postId)
        return ResponseEntity.ok(PostDto.from(post))
    }

    /**
     * POST /api/v1/sns/posts
     * 게시물 생성
     */
    @PostMapping
    fun createPost(
        @RequestAttribute("userId") userId: Long,
        @RequestBody request: CreatePostRequest
    ): ResponseEntity<PostDto> {
        val post = postService.createPost(
            userId = userId,
            caption = request.caption,
            imageUrls = request.imageUrls,
            location = request.location
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(PostDto.from(post))
    }

    /**
     * PUT /api/v1/sns/posts/{postId}
     * 게시물 업데이트
     */
    @PutMapping("/{postId}")
    fun updatePost(
        @PathVariable postId: Long,
        @RequestAttribute("userId") userId: Long,
        @RequestBody request: UpdatePostRequest
    ): ResponseEntity<PostDto> {
        val post = postService.updatePost(
            postId = postId,
            userId = userId,
            caption = request.caption,
            location = request.location
        )
        return ResponseEntity.ok(PostDto.from(post))
    }

    /**
     * DELETE /api/v1/sns/posts/{postId}
     * 게시물 삭제
     */
    @DeleteMapping("/{postId}")
    fun deletePost(
        @PathVariable postId: Long,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<Map<String, String>> {
        postService.deletePost(postId, userId)
        return ResponseEntity.ok(mapOf("message" to "Post deleted successfully"))
    }

    /**
     * POST /api/v1/sns/posts/{postId}/like
     * 게시물 좋아요
     */
    @PostMapping("/{postId}/like")
    fun likePost(
        @PathVariable postId: Long,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<Map<String, String>> {
        postService.likePost(postId, userId)
        return ResponseEntity.ok(mapOf("message" to "Post liked"))
    }

    /**
     * DELETE /api/v1/sns/posts/{postId}/like
     * 게시물 좋아요 취소
     */
    @DeleteMapping("/{postId}/like")
    fun unlikePost(
        @PathVariable postId: Long,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<Map<String, String>> {
        postService.unlikePost(postId, userId)
        return ResponseEntity.ok(mapOf("message" to "Post unliked"))
    }

    /**
     * GET /api/v1/sns/posts/{postId}/comments
     * 게시물 댓글 조회
     */
    @GetMapping("/{postId}/comments")
    fun getComments(
        @PathVariable postId: Long,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<CommentsResponse> {
        val comments = commentService.getPostComments(postId, limit, offset)
        return ResponseEntity.ok(CommentsResponse(
            data = comments.map { CommentDto.from(it) },
            meta = PaginationMeta(limit, offset, comments.size >= limit)
        ))
    }

    /**
     * POST /api/v1/sns/posts/{postId}/comments
     * 댓글 작성
     */
    @PostMapping("/{postId}/comments")
    fun createComment(
        @PathVariable postId: Long,
        @RequestAttribute("userId") userId: Long,
        @RequestBody request: CreateCommentRequest
    ): ResponseEntity<CommentDto> {
        val comment = commentService.createComment(
            postId = postId,
            userId = userId,
            content = request.content,
            parentCommentId = request.parentCommentId
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentDto.from(comment))
    }

    /**
     * PUT /api/v1/sns/comments/{commentId}
     * 댓글 수정
     */
    @PutMapping("/comments/{commentId}")
    fun updateComment(
        @PathVariable commentId: Long,
        @RequestAttribute("userId") userId: Long,
        @RequestBody request: UpdateCommentRequest
    ): ResponseEntity<CommentDto> {
        val comment = commentService.updateComment(commentId, userId, request.content)
        return ResponseEntity.ok(CommentDto.from(comment))
    }

    /**
     * DELETE /api/v1/sns/comments/{commentId}
     * 댓글 삭제
     */
    @DeleteMapping("/comments/{commentId}")
    fun deleteComment(
        @PathVariable commentId: Long,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<Map<String, String>> {
        commentService.deleteComment(commentId, userId)
        return ResponseEntity.ok(mapOf("message" to "Comment deleted successfully"))
    }
}

// DTOs
data class PostDto(
    val postId: Long,
    val userId: Long,
    val caption: String,
    val imageUrls: List<String>,
    val location: String?,
    val hashtags: List<String>,
    val likeCount: Int,
    val commentCount: Int,
    val viewCount: Int,
    val createdAt: String
) {
    companion object {
        fun from(post: Post): PostDto {
            return PostDto(
                postId = post.postId,
                userId = post.userId,
                caption = post.caption,
                imageUrls = post.imageUrls,
                location = post.location,
                hashtags = post.hashtags,
                likeCount = post.likeCount,
                commentCount = post.commentCount,
                viewCount = post.viewCount,
                createdAt = post.createdAt.toString()
            )
        }
    }
}

data class CommentDto(
    val commentId: Long,
    val postId: Long,
    val userId: Long,
    val parentCommentId: Long?,
    val content: String,
    val likeCount: Int,
    val createdAt: String
) {
    companion object {
        fun from(comment: Comment): CommentDto {
            return CommentDto(
                commentId = comment.commentId,
                postId = comment.postId,
                userId = comment.userId,
                parentCommentId = comment.parentCommentId,
                content = comment.content,
                likeCount = comment.likeCount,
                createdAt = comment.createdAt.toString()
            )
        }
    }
}

data class CreatePostRequest(
    val caption: String,
    val imageUrls: List<String>,
    val location: String?
)

data class UpdatePostRequest(
    val caption: String?,
    val location: String?
)

data class CreateCommentRequest(
    val content: String,
    val parentCommentId: Long?
)

data class UpdateCommentRequest(
    val content: String
)

data class FeedResponse(
    val data: List<PostDto>,
    val meta: PaginationMeta
)

data class CommentsResponse(
    val data: List<CommentDto>,
    val meta: PaginationMeta
)

data class PaginationMeta(
    val limit: Int,
    val offset: Int,
    val hasMore: Boolean
)
