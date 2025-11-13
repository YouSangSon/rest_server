package yousang.rest_server.adapter.out.persistence.sns

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import yousang.rest_server.adapter.out.database.DatabaseServiceClient
import yousang.rest_server.application.ports.out.PostRepositoryPort
import yousang.rest_server.domain.sns.Post
import java.time.LocalDateTime

/**
 * Post Repository Adapter (Database Service - MongoDB)
 */
@Component
@Primary
class PostRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient,
    private val objectMapper: ObjectMapper
) : PostRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_posts"
        private const val DB_TYPE = DatabaseServiceClient.DB_MONGODB
    }

    override fun save(post: Post): Post {
        val document = mapOf(
            "postId" to post.postId,
            "userId" to post.userId,
            "caption" to post.caption,
            "imageUrls" to post.imageUrls,
            "location" to post.location,
            "hashtags" to post.hashtags,
            "likeCount" to post.likeCount,
            "commentCount" to post.commentCount,
            "bookmarkCount" to post.bookmarkCount,
            "viewCount" to post.viewCount,
            "isHidden" to post.isHidden,
            "createdAt" to post.createdAt.toString(),
            "updatedAt" to post.updatedAt.toString()
        )

        val response = if (post.postId == 0L) {
            databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        } else {
            databaseServiceClient.upsert(
                collection = COLLECTION,
                filter = mapOf("postId" to post.postId),
                document = document,
                databaseType = DB_TYPE
            )
        }

        return documentToPost(response.data as Map<*, *>)
    }

    override fun findById(postId: Long): Post? {
        val response = databaseServiceClient.findById(
            collection = COLLECTION,
            id = postId.toString(),
            databaseType = DB_TYPE,
            responseType = Map::class.java
        ) ?: return null

        return documentToPost(response.data as Map<*, *>)
    }

    override fun findByUserId(userId: Long, limit: Int, offset: Int): List<Post> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("userId" to userId, "isHidden" to false),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val posts = response.data as? List<*> ?: return emptyList()
        return posts.map { documentToPost(it as Map<*, *>) }
    }

    override fun findByHashtag(hashtag: String, limit: Int, offset: Int): List<Post> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf(
                "hashtags" to hashtag,
                "isHidden" to false
            ),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val posts = response.data as? List<*> ?: return emptyList()
        return posts.map { documentToPost(it as Map<*, *>) }
    }

    override fun findFeed(
        userId: Long,
        followingIds: List<Long>,
        limit: Int,
        offset: Int
    ): List<Post> {
        // Include user's own posts and following users' posts
        val userIds = followingIds + userId

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf(
                "userId" to mapOf("\$in" to userIds),
                "isHidden" to false
            ),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val posts = response.data as? List<*> ?: return emptyList()
        return posts.map { documentToPost(it as Map<*, *>) }
    }

    override fun delete(postId: Long): Boolean {
        // Soft delete - set isHidden to true
        val response = databaseServiceClient.update<Map<String, Any>>(
            collection = COLLECTION,
            id = postId.toString(),
            updates = mapOf("isHidden" to true, "updatedAt" to LocalDateTime.now().toString()),
            databaseType = DB_TYPE
        )
        return response.success
    }

    override fun search(query: String, limit: Int, offset: Int): List<Post> {
        val response = databaseServiceClient.search<Map<String, Any>>(
            collection = COLLECTION,
            searchQuery = query,
            fields = listOf("caption", "hashtags"),
            limit = limit,
            databaseType = DB_TYPE
        )

        val posts = response.data as? List<*> ?: return emptyList()
        return posts.map { documentToPost(it as Map<*, *>) }
    }

    private fun documentToPost(doc: Map<*, *>): Post {
        return Post(
            postId = (doc["postId"] as Number).toLong(),
            userId = (doc["userId"] as Number).toLong(),
            caption = doc["caption"] as String,
            imageUrls = (doc["imageUrls"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            location = doc["location"] as? String,
            hashtags = (doc["hashtags"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            likeCount = (doc["likeCount"] as? Number)?.toInt() ?: 0,
            commentCount = (doc["commentCount"] as? Number)?.toInt() ?: 0,
            bookmarkCount = (doc["bookmarkCount"] as? Number)?.toInt() ?: 0,
            viewCount = (doc["viewCount"] as? Number)?.toInt() ?: 0,
            isHidden = doc["isHidden"] as? Boolean ?: false,
            createdAt = LocalDateTime.parse(doc["createdAt"] as String),
            updatedAt = LocalDateTime.parse(doc["updatedAt"] as String)
        )
    }
}
