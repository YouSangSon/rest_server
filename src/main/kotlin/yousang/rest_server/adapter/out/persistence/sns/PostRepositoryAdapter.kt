package yousang.rest_server.adapter.out.persistence.sns

import org.springframework.context.annotation.Primary
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import yousang.rest_server.adapter.out.persistence.sns.document.PostDocument
import yousang.rest_server.adapter.out.persistence.sns.repository.PostMongoRepository
import yousang.rest_server.application.ports.out.PostRepositoryPort
import yousang.rest_server.domain.sns.Post

/**
 * 게시물 Repository Adapter (Direct MongoDB via Spring Data MongoDB)
 */
@Component
@Primary
class PostRepositoryAdapter(
    private val repository: PostMongoRepository
) : PostRepositoryPort {

    override fun save(post: Post): Post {
        val document = PostDocument.from(post)
        val saved = repository.save(document)
        return saved.toDomain()
    }

    override fun findById(postId: Long): Post? {
        return repository.findById(postId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUserId(userId: Long, limit: Int, offset: Int): List<Post> {
        val pageable = PageRequest.of(
            offset / limit,
            limit,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
        return repository.findByUserId(userId, pageable)
            .map { it.toDomain() }
    }

    override fun findFeed(userId: Long, followingIds: List<Long>, limit: Int, offset: Int): List<Post> {
        val userIds = (followingIds + userId).distinct()
        val pageable = PageRequest.of(
            offset / limit,
            limit,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
        return repository.findByUserIdIn(userIds, pageable)
            .map { it.toDomain() }
    }

    override fun findByHashtag(hashtag: String, limit: Int, offset: Int): List<Post> {
        val pageable = PageRequest.of(
            offset / limit,
            limit,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
        return repository.findByHashtag(hashtag, pageable)
            .map { it.toDomain() }
    }

    override fun findExplore(limit: Int, offset: Int): List<Post> {
        val pageable = PageRequest.of(
            offset / limit,
            limit,
            Sort.by(Sort.Direction.DESC, "likeCount", "createdAt")
        )
        return repository.findAllVisible(pageable)
            .map { it.toDomain() }
    }

    override fun delete(postId: Long) {
        repository.deleteById(postId)
    }

    override fun countByUserId(userId: Long): Long {
        return repository.countByUserId(userId)
    }
}
