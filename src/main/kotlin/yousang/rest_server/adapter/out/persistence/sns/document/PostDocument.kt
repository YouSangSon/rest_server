package yousang.rest_server.adapter.out.persistence.sns.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import yousang.rest_server.domain.sns.Post
import java.time.LocalDateTime

@Document(collection = "sns_posts")
@CompoundIndex(name = "user_created_idx", def = "{'userId': 1, 'createdAt': -1}")
@CompoundIndex(name = "hidden_created_idx", def = "{'isHidden': 1, 'createdAt': -1}")
data class PostDocument(
    @Id
    val postId: Long = 0,

    @Indexed
    val userId: Long,

    val caption: String,

    val imageUrls: List<String> = emptyList(),

    val location: String? = null,

    @Indexed
    val hashtags: List<String> = emptyList(),

    val likeCount: Int = 0,

    val commentCount: Int = 0,

    val bookmarkCount: Int = 0,

    val viewCount: Int = 0,

    @Indexed
    val isHidden: Boolean = false,

    @Indexed
    val createdAt: LocalDateTime = LocalDateTime.now(),

    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Post = Post(
        postId = postId,
        userId = userId,
        caption = caption,
        imageUrls = imageUrls,
        location = location,
        hashtags = hashtags,
        likeCount = likeCount,
        commentCount = commentCount,
        bookmarkCount = bookmarkCount,
        viewCount = viewCount,
        isHidden = isHidden,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun from(domain: Post): PostDocument = PostDocument(
            postId = domain.postId,
            userId = domain.userId,
            caption = domain.caption,
            imageUrls = domain.imageUrls,
            location = domain.location,
            hashtags = domain.hashtags,
            likeCount = domain.likeCount,
            commentCount = domain.commentCount,
            bookmarkCount = domain.bookmarkCount,
            viewCount = domain.viewCount,
            isHidden = domain.isHidden,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
