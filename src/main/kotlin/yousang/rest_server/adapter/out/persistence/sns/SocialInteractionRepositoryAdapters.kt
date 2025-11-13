package yousang.rest_server.adapter.out.persistence.sns

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import yousang.rest_server.adapter.out.database.DatabaseServiceClient
import yousang.rest_server.application.ports.out.*
import yousang.rest_server.domain.sns.*
import java.time.LocalDateTime

/**
 * Comment Repository Adapter (Database Service - MongoDB)
 */
@Component
@Primary
class CommentRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient
) : CommentRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_comments"
        private const val DB_TYPE = DatabaseServiceClient.DB_MONGODB
    }

    override fun save(comment: Comment): Comment {
        val document = mapOf(
            "commentId" to comment.commentId,
            "postId" to comment.postId,
            "userId" to comment.userId,
            "parentCommentId" to comment.parentCommentId,
            "content" to comment.content,
            "likeCount" to comment.likeCount,
            "createdAt" to comment.createdAt.toString(),
            "updatedAt" to comment.updatedAt.toString()
        )

        val response = if (comment.commentId == 0L) {
            databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        } else {
            databaseServiceClient.upsert(
                collection = COLLECTION,
                filter = mapOf("commentId" to comment.commentId),
                document = document,
                databaseType = DB_TYPE
            )
        }

        return documentToComment(response.data as Map<*, *>)
    }

    override fun findById(commentId: Long): Comment? {
        val response = databaseServiceClient.findById(
            collection = COLLECTION,
            id = commentId.toString(),
            databaseType = DB_TYPE,
            responseType = Map::class.java
        ) ?: return null

        return documentToComment(response.data as Map<*, *>)
    }

    override fun findByPostId(postId: Long, limit: Int, offset: Int): List<Comment> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("postId" to postId, "parentCommentId" to null),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val comments = response.data as? List<*> ?: return emptyList()
        return comments.map { documentToComment(it as Map<*, *>) }
    }

    override fun findReplies(parentCommentId: Long): List<Comment> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("parentCommentId" to parentCommentId),
            sort = mapOf("createdAt" to 1),
            limit = 100,
            offset = 0,
            databaseType = DB_TYPE
        )

        val comments = response.data as? List<*> ?: return emptyList()
        return comments.map { documentToComment(it as Map<*, *>) }
    }

    override fun delete(commentId: Long): Boolean {
        val response = databaseServiceClient.delete(
            collection = COLLECTION,
            id = commentId.toString(),
            databaseType = DB_TYPE
        )
        return response.data as? Boolean ?: false
    }

    override fun countByPostId(postId: Long): Long {
        val response = databaseServiceClient.count(
            collection = COLLECTION,
            filter = mapOf("postId" to postId),
            databaseType = DB_TYPE
        )
        return (response.data as? Number)?.toLong() ?: 0L
    }

    private fun documentToComment(doc: Map<*, *>): Comment {
        return Comment(
            commentId = (doc["commentId"] as Number).toLong(),
            postId = (doc["postId"] as Number).toLong(),
            userId = (doc["userId"] as Number).toLong(),
            parentCommentId = (doc["parentCommentId"] as? Number)?.toLong(),
            content = doc["content"] as String,
            likeCount = (doc["likeCount"] as? Number)?.toInt() ?: 0,
            createdAt = LocalDateTime.parse(doc["createdAt"] as String),
            updatedAt = LocalDateTime.parse(doc["updatedAt"] as String)
        )
    }
}

/**
 * Like Repository Adapter (Database Service - MongoDB)
 */
@Component
@Primary
class LikeRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient
) : LikeRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_likes"
        private const val DB_TYPE = DatabaseServiceClient.DB_MONGODB
    }

    override fun save(like: Like): Like {
        val document = mapOf(
            "likeId" to like.likeId,
            "postId" to like.postId,
            "userId" to like.userId,
            "createdAt" to like.createdAt.toString()
        )

        val response = databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        return documentToLike(response.data as Map<*, *>)
    }

    override fun delete(postId: Long, userId: Long): Boolean {
        val response = databaseServiceClient.deleteMany(
            collection = COLLECTION,
            filter = mapOf("postId" to postId, "userId" to userId),
            databaseType = DB_TYPE
        )
        return (response.data as? Map<*, *>)?.get("deletedCount") as? Int ?: 0 > 0
    }

    override fun findByPostId(postId: Long, limit: Int, offset: Int): List<Like> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("postId" to postId),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val likes = response.data as? List<*> ?: return emptyList()
        return likes.map { documentToLike(it as Map<*, *>) }
    }

    override fun exists(postId: Long, userId: Long): Boolean {
        val count = databaseServiceClient.count(
            collection = COLLECTION,
            filter = mapOf("postId" to postId, "userId" to userId),
            databaseType = DB_TYPE
        )
        return (count.data as? Long ?: 0L) > 0
    }

    override fun countByPostId(postId: Long): Long {
        val response = databaseServiceClient.count(
            collection = COLLECTION,
            filter = mapOf("postId" to postId),
            databaseType = DB_TYPE
        )
        return (response.data as? Number)?.toLong() ?: 0L
    }

    private fun documentToLike(doc: Map<*, *>): Like {
        return Like(
            likeId = (doc["likeId"] as Number).toLong(),
            postId = (doc["postId"] as Number).toLong(),
            userId = (doc["userId"] as Number).toLong(),
            createdAt = LocalDateTime.parse(doc["createdAt"] as String)
        )
    }
}

/**
 * Follow Repository Adapter (Database Service - PostgreSQL)
 */
@Component
@Primary
class FollowRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient
) : FollowRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_follows"
        private const val DB_TYPE = DatabaseServiceClient.DB_POSTGRES
    }

    override fun save(follow: Follow): Follow {
        val document = mapOf(
            "followId" to follow.followId,
            "followerId" to follow.followerId,
            "followingId" to follow.followingId,
            "createdAt" to follow.createdAt.toString()
        )

        val response = databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        return documentToFollow(response.data as Map<*, *>)
    }

    override fun delete(followerId: Long, followingId: Long): Boolean {
        val response = databaseServiceClient.deleteMany(
            collection = COLLECTION,
            filter = mapOf("followerId" to followerId, "followingId" to followingId),
            databaseType = DB_TYPE
        )
        return (response.data as? Map<*, *>)?.get("deletedCount") as? Int ?: 0 > 0
    }

    override fun findFollowers(userId: Long, limit: Int, offset: Int): List<Follow> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("followingId" to userId),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val follows = response.data as? List<*> ?: return emptyList()
        return follows.map { documentToFollow(it as Map<*, *>) }
    }

    override fun findFollowing(userId: Long, limit: Int, offset: Int): List<Follow> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("followerId" to userId),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val follows = response.data as? List<*> ?: return emptyList()
        return follows.map { documentToFollow(it as Map<*, *>) }
    }

    override fun exists(followerId: Long, followingId: Long): Boolean {
        val count = databaseServiceClient.count(
            collection = COLLECTION,
            filter = mapOf("followerId" to followerId, "followingId" to followingId),
            databaseType = DB_TYPE
        )
        return (count.data as? Long ?: 0L) > 0
    }

    override fun countFollowers(userId: Long): Long {
        val response = databaseServiceClient.count(
            collection = COLLECTION,
            filter = mapOf("followingId" to userId),
            databaseType = DB_TYPE
        )
        return (response.data as? Number)?.toLong() ?: 0L
    }

    override fun countFollowing(userId: Long): Long {
        val response = databaseServiceClient.count(
            collection = COLLECTION,
            filter = mapOf("followerId" to userId),
            databaseType = DB_TYPE
        )
        return (response.data as? Number)?.toLong() ?: 0L
    }

    private fun documentToFollow(doc: Map<*, *>): Follow {
        return Follow(
            followId = (doc["followId"] as Number).toLong(),
            followerId = (doc["followerId"] as Number).toLong(),
            followingId = (doc["followingId"] as Number).toLong(),
            createdAt = LocalDateTime.parse(doc["createdAt"] as String)
        )
    }
}
