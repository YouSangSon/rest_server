package yousang.rest_server.application.service.sns

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.application.ports.out.*
import yousang.rest_server.domain.sns.*
import java.time.LocalDateTime

/**
 * 게시물 서비스
 * 게시물 생성, 조회, 수정, 삭제 및 좋아요 기능 제공
 */
@Service
@Transactional
class PostService(
    private val postRepository: PostRepositoryPort,
    private val likeRepository: LikeRepositoryPort,
    private val commentRepository: CommentRepositoryPort,
    private val userRepository: SnsUserRepositoryPort,
    private val followRepository: FollowRepositoryPort,
    private val notificationService: NotificationService
) {
    /**
     * 게시물 생성
     */
    fun createPost(
        userId: Long,
        caption: String,
        imageUrls: List<String>,
        location: String? = null
    ): Post {
        // 사용자 존재 확인
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        // 게시물 생성
        val post = Post(
            postId = 0, // Auto-generated
            userId = userId,
            caption = caption,
            imageUrls = imageUrls,
            location = location,
            createdAt = LocalDateTime.now()
        ).extractHashtags()

        val savedPost = postRepository.save(post)

        // 사용자의 게시물 수 증가
        userRepository.save(user.incrementPosts())

        return savedPost
    }

    /**
     * 게시물 조회
     */
    @Transactional(readOnly = true)
    fun getPost(postId: Long): Post {
        return postRepository.findById(postId)
            ?: throw IllegalArgumentException("Post not found")
    }

    /**
     * 게시물 업데이트
     */
    fun updatePost(
        postId: Long,
        userId: Long,
        caption: String? = null,
        location: String? = null
    ): Post {
        val post = postRepository.findById(postId)
            ?: throw IllegalArgumentException("Post not found")

        // 작성자 확인
        if (post.userId != userId) {
            throw IllegalArgumentException("Not authorized to update this post")
        }

        val updatedPost = post.update(caption, location)
        return postRepository.save(updatedPost)
    }

    /**
     * 게시물 삭제
     */
    fun deletePost(postId: Long, userId: Long) {
        val post = postRepository.findById(postId)
            ?: throw IllegalArgumentException("Post not found")

        // 작성자 확인
        if (post.userId != userId) {
            throw IllegalArgumentException("Not authorized to delete this post")
        }

        postRepository.delete(postId)

        // 사용자의 게시물 수 감소
        val user = userRepository.findById(userId)
        if (user != null) {
            userRepository.save(user.decrementPosts())
        }
    }

    /**
     * 사용자 게시물 조회
     */
    @Transactional(readOnly = true)
    fun getUserPosts(userId: Long, limit: Int = 20, offset: Int = 0): List<Post> {
        return postRepository.findByUserId(userId, limit, offset)
    }

    /**
     * 피드 조회 (팔로잉 사용자의 게시물)
     */
    @Transactional(readOnly = true)
    fun getFeed(userId: Long, limit: Int = 20, offset: Int = 0): List<Post> {
        // 팔로잉 중인 사용자 ID 목록 가져오기
        val following = followRepository.findFollowing(userId, limit = 1000, offset = 0)
        val followingIds = following.map { it.followingId }

        return postRepository.findFeed(userId, followingIds, limit, offset)
    }

    /**
     * 해시태그로 게시물 검색
     */
    @Transactional(readOnly = true)
    fun getPostsByHashtag(hashtag: String, limit: Int = 20, offset: Int = 0): List<Post> {
        return postRepository.findByHashtag(hashtag.lowercase(), limit, offset)
    }

    /**
     * 게시물 좋아요
     */
    fun likePost(postId: Long, userId: Long) {
        val post = postRepository.findById(postId)
            ?: throw IllegalArgumentException("Post not found")

        // 이미 좋아요 했는지 확인
        if (likeRepository.exists(postId, userId)) {
            throw IllegalArgumentException("Already liked")
        }

        // 좋아요 저장
        val like = Like(
            likeId = 0,
            postId = postId,
            userId = userId,
            createdAt = LocalDateTime.now()
        )
        likeRepository.save(like)

        // 게시물 좋아요 수 증가
        postRepository.save(post.incrementLikes())

        // 알림 전송 (자신의 게시물이 아닌 경우)
        if (post.userId != userId) {
            notificationService.createLikeNotification(post.userId, userId, postId)
        }
    }

    /**
     * 게시물 좋아요 취소
     */
    fun unlikePost(postId: Long, userId: Long) {
        val post = postRepository.findById(postId)
            ?: throw IllegalArgumentException("Post not found")

        // 좋아요 삭제
        if (!likeRepository.delete(postId, userId)) {
            throw IllegalArgumentException("Like not found")
        }

        // 게시물 좋아요 수 감소
        postRepository.save(post.decrementLikes())
    }

    /**
     * 게시물 좋아요 목록 조회
     */
    @Transactional(readOnly = true)
    fun getPostLikes(postId: Long, limit: Int = 100, offset: Int = 0): List<Like> {
        return likeRepository.findByPostId(postId, limit, offset)
    }

    /**
     * 게시물 조회 수 증가
     */
    fun incrementViews(postId: Long) {
        val post = postRepository.findById(postId) ?: return
        postRepository.save(post.incrementViews())
    }

    /**
     * 게시물 검색
     */
    @Transactional(readOnly = true)
    fun searchPosts(query: String, limit: Int = 20, offset: Int = 0): List<Post> {
        return postRepository.search(query, limit, offset)
    }
}
