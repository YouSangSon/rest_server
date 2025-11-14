package yousang.rest_server.adapter.out.persistence.sns

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import yousang.rest_server.adapter.out.database.DatabaseServiceClient
import yousang.rest_server.application.ports.out.*
import yousang.rest_server.domain.sns.*
import java.time.LocalDateTime

/**
 * Notification Repository Adapter (Database Service - MongoDB)
 */
@Component
@Primary
class NotificationRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient,
    private val objectMapper: ObjectMapper
) : NotificationRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_notifications"
        private const val DB_TYPE = DatabaseServiceClient.DB_MONGODB
    }

    override fun save(notification: Notification): Notification {
        val document = mapOf(
            "notificationId" to notification.notificationId,
            "userId" to notification.userId,
            "notificationType" to notification.notificationType.name,
            "sourceUserId" to notification.sourceUserId,
            "relatedContentType" to notification.relatedContentType,
            "relatedContentId" to notification.relatedContentId,
            "title" to notification.title,
            "message" to notification.message,
            "dataPayload" to notification.dataPayload,
            "isRead" to notification.isRead,
            "createdAt" to notification.createdAt.toString()
        )

        val response = databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        return documentToNotification(response.data as Map<*, *>)
    }

    override fun findById(notificationId: Long): Notification? {
        val response = databaseServiceClient.findById(
            collection = COLLECTION,
            id = notificationId.toString(),
            databaseType = DB_TYPE,
            responseType = Map::class.java
        ) ?: return null

        return documentToNotification(response.data as Map<*, *>)
    }

    override fun findByUserId(userId: Long, limit: Int, offset: Int): List<Notification> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("userId" to userId),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val notifications = response.data as? List<*> ?: return emptyList()
        return notifications.map { documentToNotification(it as Map<*, *>) }
    }

    override fun markAsRead(notificationId: Long): Boolean {
        val response = databaseServiceClient.update<Map<String, Any>>(
            collection = COLLECTION,
            id = notificationId.toString(),
            updates = mapOf("isRead" to true),
            databaseType = DB_TYPE
        )
        return response.success
    }

    override fun delete(notificationId: Long): Boolean {
        val response = databaseServiceClient.delete(
            collection = COLLECTION,
            id = notificationId.toString(),
            databaseType = DB_TYPE
        )
        return response.data as? Boolean ?: false
    }

    override fun countUnread(userId: Long): Long {
        val response = databaseServiceClient.count(
            collection = COLLECTION,
            filter = mapOf("userId" to userId, "isRead" to false),
            databaseType = DB_TYPE
        )
        return (response.data as? Number)?.toLong() ?: 0L
    }

    private fun documentToNotification(doc: Map<*, *>): Notification {
        return Notification(
            notificationId = (doc["notificationId"] as Number).toLong(),
            userId = (doc["userId"] as Number).toLong(),
            notificationType = NotificationType.valueOf(doc["notificationType"] as String),
            sourceUserId = (doc["sourceUserId"] as? Number)?.toLong(),
            relatedContentType = doc["relatedContentType"] as? String,
            relatedContentId = (doc["relatedContentId"] as? Number)?.toLong(),
            title = doc["title"] as String,
            message = doc["message"] as String,
            dataPayload = (doc["dataPayload"] as? Map<*, *>)?.mapKeys { it.key.toString() }?.mapValues { it.value as Any } ?: emptyMap(),
            isRead = doc["isRead"] as? Boolean ?: false,
            createdAt = LocalDateTime.parse(doc["createdAt"] as String)
        )
    }
}

/**
 * Bookmark Repository Adapter (Database Service - MongoDB)
 */
@Component
@Primary
class BookmarkRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient
) : BookmarkRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_bookmarks"
        private const val DB_TYPE = DatabaseServiceClient.DB_MONGODB
    }

    override fun save(bookmark: Bookmark): Bookmark {
        val document = mapOf(
            "bookmarkId" to bookmark.bookmarkId,
            "userId" to bookmark.userId,
            "contentType" to bookmark.contentType.name,
            "contentId" to bookmark.contentId,
            "createdAt" to bookmark.createdAt.toString()
        )

        val response = databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        return documentToBookmark(response.data as Map<*, *>)
    }

    override fun delete(bookmarkId: Long): Boolean {
        val response = databaseServiceClient.delete(
            collection = COLLECTION,
            id = bookmarkId.toString(),
            databaseType = DB_TYPE
        )
        return response.data as? Boolean ?: false
    }

    override fun findByUserId(userId: Long, limit: Int, offset: Int): List<Bookmark> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("userId" to userId),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val bookmarks = response.data as? List<*> ?: return emptyList()
        return bookmarks.map { documentToBookmark(it as Map<*, *>) }
    }

    override fun exists(userId: Long, contentType: BookmarkContentType, contentId: Long): Boolean {
        val count = databaseServiceClient.count(
            collection = COLLECTION,
            filter = mapOf(
                "userId" to userId,
                "contentType" to contentType.name,
                "contentId" to contentId
            ),
            databaseType = DB_TYPE
        )
        return (count.data as? Long ?: 0L) > 0
    }

    private fun documentToBookmark(doc: Map<*, *>): Bookmark {
        return Bookmark(
            bookmarkId = (doc["bookmarkId"] as Number).toLong(),
            userId = (doc["userId"] as Number).toLong(),
            contentType = BookmarkContentType.valueOf(doc["contentType"] as String),
            contentId = (doc["contentId"] as Number).toLong(),
            createdAt = LocalDateTime.parse(doc["createdAt"] as String)
        )
    }
}

/**
 * Conversation Repository Adapter (Database Service - PostgreSQL)
 */
@Component
@Primary
class ConversationRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient
) : ConversationRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_conversations"
        private const val DB_TYPE = DatabaseServiceClient.DB_POSTGRES
    }

    override fun save(conversation: Conversation): Conversation {
        val document = mapOf(
            "conversationId" to conversation.conversationId,
            "participant1Id" to conversation.participant1Id,
            "participant2Id" to conversation.participant2Id,
            "lastMessageAt" to conversation.lastMessageAt?.toString(),
            "createdAt" to conversation.createdAt.toString()
        )

        val response = if (conversation.conversationId == 0L) {
            databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        } else {
            databaseServiceClient.upsert(
                collection = COLLECTION,
                filter = mapOf("conversationId" to conversation.conversationId),
                document = document,
                databaseType = DB_TYPE
            )
        }

        return documentToConversation(response.data as Map<*, *>)
    }

    override fun findById(conversationId: Long): Conversation? {
        val response = databaseServiceClient.findById(
            collection = COLLECTION,
            id = conversationId.toString(),
            databaseType = DB_TYPE,
            responseType = Map::class.java
        ) ?: return null

        return documentToConversation(response.data as Map<*, *>)
    }

    override fun findByParticipants(user1Id: Long, user2Id: Long): Conversation? {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf(
                "\$or" to listOf(
                    mapOf("participant1Id" to user1Id, "participant2Id" to user2Id),
                    mapOf("participant1Id" to user2Id, "participant2Id" to user1Id)
                )
            ),
            limit = 1,
            databaseType = DB_TYPE
        )

        val conversations = response.data as? List<*> ?: return null
        return if (conversations.isNotEmpty()) {
            documentToConversation(conversations[0] as Map<*, *>)
        } else {
            null
        }
    }

    override fun findByUserId(userId: Long, limit: Int, offset: Int): List<Conversation> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf(
                "\$or" to listOf(
                    mapOf("participant1Id" to userId),
                    mapOf("participant2Id" to userId)
                )
            ),
            sort = mapOf("lastMessageAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val conversations = response.data as? List<*> ?: return emptyList()
        return conversations.map { documentToConversation(it as Map<*, *>) }
    }

    private fun documentToConversation(doc: Map<*, *>): Conversation {
        return Conversation(
            conversationId = (doc["conversationId"] as Number).toLong(),
            participant1Id = (doc["participant1Id"] as Number).toLong(),
            participant2Id = (doc["participant2Id"] as Number).toLong(),
            lastMessageAt = (doc["lastMessageAt"] as? String)?.let { LocalDateTime.parse(it) },
            createdAt = LocalDateTime.parse(doc["createdAt"] as String)
        )
    }
}

/**
 * Message Repository Adapter (Database Service - MongoDB)
 */
@Component
@Primary
class MessageRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient
) : MessageRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_messages"
        private const val DB_TYPE = DatabaseServiceClient.DB_MONGODB
    }

    override fun save(message: Message): Message {
        val document = mapOf(
            "messageId" to message.messageId,
            "conversationId" to message.conversationId,
            "senderId" to message.senderId,
            "content" to message.content,
            "imageUrl" to message.imageUrl,
            "isRead" to message.isRead,
            "createdAt" to message.createdAt.toString(),
            "updatedAt" to message.updatedAt.toString()
        )

        val response = if (message.messageId == 0L) {
            databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        } else {
            databaseServiceClient.upsert(
                collection = COLLECTION,
                filter = mapOf("messageId" to message.messageId),
                document = document,
                databaseType = DB_TYPE
            )
        }

        return documentToMessage(response.data as Map<*, *>)
    }

    override fun findById(messageId: Long): Message? {
        val response = databaseServiceClient.findById(
            collection = COLLECTION,
            id = messageId.toString(),
            databaseType = DB_TYPE,
            responseType = Map::class.java
        ) ?: return null

        return documentToMessage(response.data as Map<*, *>)
    }

    override fun findByConversationId(conversationId: Long, limit: Int, offset: Int): List<Message> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("conversationId" to conversationId),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val messages = response.data as? List<*> ?: return emptyList()
        return messages.map { documentToMessage(it as Map<*, *>) }
    }

    override fun markAsRead(messageId: Long): Boolean {
        val response = databaseServiceClient.update<Map<String, Any>>(
            collection = COLLECTION,
            id = messageId.toString(),
            updates = mapOf("isRead" to true),
            databaseType = DB_TYPE
        )
        return response.success
    }

    override fun delete(messageId: Long): Boolean {
        val response = databaseServiceClient.delete(
            collection = COLLECTION,
            id = messageId.toString(),
            databaseType = DB_TYPE
        )
        return response.data as? Boolean ?: false
    }

    private fun documentToMessage(doc: Map<*, *>): Message {
        return Message(
            messageId = (doc["messageId"] as Number).toLong(),
            conversationId = (doc["conversationId"] as Number).toLong(),
            senderId = (doc["senderId"] as Number).toLong(),
            content = doc["content"] as String,
            imageUrl = doc["imageUrl"] as? String,
            isRead = doc["isRead"] as? Boolean ?: false,
            createdAt = LocalDateTime.parse(doc["createdAt"] as String),
            updatedAt = LocalDateTime.parse(doc["updatedAt"] as String)
        )
    }
}
