package yousang.rest_server.application.service.sns

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.application.ports.out.CommentRepositoryPort
import yousang.rest_server.application.ports.out.PostRepositoryPort
import yousang.rest_server.domain.sns.Comment
import java.time.LocalDateTime

/**
 * 댓글 서비스
 */
@Service
@Transactional
class CommentService(
    private val commentRepository: CommentRepositoryPort,
    private val postRepository: PostRepositoryPort,
    private val notificationService: NotificationService
) {
    /**
     * 댓글 생성
     */
    fun createComment(
        postId: Long,
        userId: Long,
        content: String,
        parentCommentId: Long? = null
    ): Comment {
        // 게시물 존재 확인
        val post = postRepository.findById(postId)
            ?: throw IllegalArgumentException("Post not found")

        // 부모 댓글 확인 (대댓글인 경우)
        if (parentCommentId != null) {
            commentRepository.findById(parentCommentId)
                ?: throw IllegalArgumentException("Parent comment not found")
        }

        val comment = Comment(
            commentId = 0,
            postId = postId,
            userId = userId,
            parentCommentId = parentCommentId,
            content = content,
            createdAt = LocalDateTime.now()
        )

        val savedComment = commentRepository.save(comment)

        // 게시물 댓글 수 증가
        postRepository.save(post.incrementComments())

        // 알림 전송
        if (post.userId != userId) {
            notificationService.createCommentNotification(post.userId, userId, postId)
        }

        return savedComment
    }

    /**
     * 댓글 조회
     */
    @Transactional(readOnly = true)
    fun getComment(commentId: Long): Comment {
        return commentRepository.findById(commentId)
            ?: throw IllegalArgumentException("Comment not found")
    }

    /**
     * 게시물의 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    fun getPostComments(postId: Long, limit: Int = 50, offset: Int = 0): List<Comment> {
        return commentRepository.findByPostId(postId, limit, offset)
    }

    /**
     * 대댓글 조회
     */
    @Transactional(readOnly = true)
    fun getReplies(parentCommentId: Long): List<Comment> {
        return commentRepository.findReplies(parentCommentId)
    }

    /**
     * 댓글 업데이트
     */
    fun updateComment(commentId: Long, userId: Long, content: String): Comment {
        val comment = commentRepository.findById(commentId)
            ?: throw IllegalArgumentException("Comment not found")

        if (comment.userId != userId) {
            throw IllegalArgumentException("Not authorized to update this comment")
        }

        val updatedComment = comment.update(content)
        return commentRepository.save(updatedComment)
    }

    /**
     * 댓글 삭제
     */
    fun deleteComment(commentId: Long, userId: Long) {
        val comment = commentRepository.findById(commentId)
            ?: throw IllegalArgumentException("Comment not found")

        if (comment.userId != userId) {
            throw IllegalArgumentException("Not authorized to delete this comment")
        }

        commentRepository.delete(commentId)

        // 게시물 댓글 수 감소
        val post = postRepository.findById(comment.postId)
        if (post != null) {
            postRepository.save(post.decrementComments())
        }
    }
}
