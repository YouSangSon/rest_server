package yousang.rest_server.adapter.out.persistence.sns

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import yousang.rest_server.adapter.out.database.DatabaseServiceClient
import yousang.rest_server.application.ports.out.SnsUserRepositoryPort
import yousang.rest_server.domain.sns.SnsUser
import java.time.LocalDateTime

/**
 * SNS 사용자 Repository Adapter (Database Service - PostgreSQL)
 */
@Component
@Primary
class SnsUserRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient,
    private val objectMapper: ObjectMapper
) : SnsUserRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_users"
        private const val DB_TYPE = DatabaseServiceClient.DB_POSTGRES
    }

    override fun save(user: SnsUser): SnsUser {
        val document = mapOf(
            "userId" to user.userId,
            "email" to user.email,
            "username" to user.username,
            "fullName" to user.fullName,
            "bio" to user.bio,
            "profileImageUrl" to user.profileImageUrl,
            "followerCount" to user.followerCount,
            "followingCount" to user.followingCount,
            "postCount" to user.postCount,
            "isVerified" to user.isVerified,
            "isActive" to user.isActive,
            "emailVerified" to user.emailVerified,
            "createdAt" to user.createdAt.toString(),
            "updatedAt" to user.updatedAt.toString()
        )

        val response = if (user.userId == 0L) {
            // Create new user
            databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        } else {
            // Update existing user
            databaseServiceClient.upsert(
                collection = COLLECTION,
                filter = mapOf("userId" to user.userId),
                document = document,
                databaseType = DB_TYPE
            )
        }

        return documentToUser(response.data as Map<*, *>)
    }

    override fun findById(userId: Long): SnsUser? {
        val response = databaseServiceClient.findById(
            collection = COLLECTION,
            id = userId.toString(),
            databaseType = DB_TYPE,
            responseType = Map::class.java
        ) ?: return null

        return documentToUser(response.data as Map<*, *>)
    }

    override fun findByEmail(email: String): SnsUser? {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("email" to email),
            limit = 1,
            databaseType = DB_TYPE
        )

        val users = response.data as? List<*> ?: return null
        return if (users.isNotEmpty()) {
            documentToUser(users[0] as Map<*, *>)
        } else {
            null
        }
    }

    override fun findByUsername(username: String): SnsUser? {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("username" to username),
            limit = 1,
            databaseType = DB_TYPE
        )

        val users = response.data as? List<*> ?: return null
        return if (users.isNotEmpty()) {
            documentToUser(users[0] as Map<*, *>)
        } else {
            null
        }
    }

    override fun search(query: String, limit: Int, offset: Int): List<SnsUser> {
        // PostgreSQL ILIKE search on username and fullName
        val response = databaseServiceClient.search<Map<String, Any>>(
            collection = COLLECTION,
            searchQuery = query,
            fields = listOf("username", "fullName"),
            limit = limit,
            databaseType = DB_TYPE
        )

        val users = response.data as? List<*> ?: return emptyList()
        return users.map { documentToUser(it as Map<*, *>) }
    }

    override fun findByIds(userIds: List<Long>): List<SnsUser> {
        if (userIds.isEmpty()) return emptyList()

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("userId" to mapOf("\$in" to userIds)),
            limit = userIds.size,
            databaseType = DB_TYPE
        )

        val users = response.data as? List<*> ?: return emptyList()
        return users.map { documentToUser(it as Map<*, *>) }
    }

    override fun delete(userId: Long): Boolean {
        val response = databaseServiceClient.delete(
            collection = COLLECTION,
            id = userId.toString(),
            databaseType = DB_TYPE
        )
        return response.data as? Boolean ?: false
    }

    override fun existsByEmail(email: String): Boolean {
        val count = databaseServiceClient.count(
            collection = COLLECTION,
            filter = mapOf("email" to email),
            databaseType = DB_TYPE
        )
        return (count.data as? Long ?: 0L) > 0
    }

    override fun existsByUsername(username: String): Boolean {
        val count = databaseServiceClient.count(
            collection = COLLECTION,
            filter = mapOf("username" to username),
            databaseType = DB_TYPE
        )
        return (count.data as? Long ?: 0L) > 0
    }

    private fun documentToUser(doc: Map<*, *>): SnsUser {
        return SnsUser(
            userId = (doc["userId"] as Number).toLong(),
            email = doc["email"] as String,
            username = doc["username"] as String,
            fullName = doc["fullName"] as String,
            bio = doc["bio"] as? String,
            profileImageUrl = doc["profileImageUrl"] as? String,
            followerCount = (doc["followerCount"] as? Number)?.toInt() ?: 0,
            followingCount = (doc["followingCount"] as? Number)?.toInt() ?: 0,
            postCount = (doc["postCount"] as? Number)?.toInt() ?: 0,
            isVerified = doc["isVerified"] as? Boolean ?: false,
            isActive = doc["isActive"] as? Boolean ?: true,
            emailVerified = doc["emailVerified"] as? Boolean ?: false,
            createdAt = LocalDateTime.parse(doc["createdAt"] as String),
            updatedAt = LocalDateTime.parse(doc["updatedAt"] as String)
        )
    }
}
