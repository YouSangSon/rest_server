package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.sns.*
import java.time.LocalDateTime

/**
 * 게시물 저장소 포트
 */
interface PostRepositoryPort {
    fun save(post: Post): Post
    fun findById(postId: Long): Post?
    fun findByUserId(userId: Long, limit: Int = 20, offset: Int = 0): List<Post>
    fun findByHashtag(hashtag: String, limit: Int = 20, offset: Int = 0): List<Post>
    fun findFeed(userId: Long, followingIds: List<Long>, limit: Int = 20, offset: Int = 0): List<Post>
    fun delete(postId: Long): Boolean
    fun search(query: String, limit: Int = 20, offset: Int = 0): List<Post>
}

/**
 * 댓글 저장소 포트
 */
interface CommentRepositoryPort {
    fun save(comment: Comment): Comment
    fun findById(commentId: Long): Comment?
    fun findByPostId(postId: Long, limit: Int = 50, offset: Int = 0): List<Comment>
    fun findReplies(parentCommentId: Long): List<Comment>
    fun delete(commentId: Long): Boolean
    fun countByPostId(postId: Long): Long
}

/**
 * 좋아요 저장소 포트
 */
interface LikeRepositoryPort {
    fun save(like: Like): Like
    fun delete(postId: Long, userId: Long): Boolean
    fun findByPostId(postId: Long, limit: Int = 100, offset: Int = 0): List<Like>
    fun exists(postId: Long, userId: Long): Boolean
    fun countByPostId(postId: Long): Long
}

/**
 * 팔로우 저장소 포트
 */
interface FollowRepositoryPort {
    fun save(follow: Follow): Follow
    fun delete(followerId: Long, followingId: Long): Boolean
    fun findFollowers(userId: Long, limit: Int = 50, offset: Int = 0): List<Follow>
    fun findFollowing(userId: Long, limit: Int = 50, offset: Int = 0): List<Follow>
    fun exists(followerId: Long, followingId: Long): Boolean
    fun countFollowers(userId: Long): Long
    fun countFollowing(userId: Long): Long
}

/**
 * 북마크 저장소 포트
 */
interface BookmarkRepositoryPort {
    fun save(bookmark: Bookmark): Bookmark
    fun delete(bookmarkId: Long): Boolean
    fun findByUserId(userId: Long, limit: Int = 50, offset: Int = 0): List<Bookmark>
    fun exists(userId: Long, contentType: BookmarkContentType, contentId: Long): Boolean
}

/**
 * 알림 저장소 포트
 */
interface NotificationRepositoryPort {
    fun save(notification: Notification): Notification
    fun findById(notificationId: Long): Notification?
    fun findByUserId(userId: Long, limit: Int = 50, offset: Int = 0): List<Notification>
    fun markAsRead(notificationId: Long): Boolean
    fun delete(notificationId: Long): Boolean
    fun countUnread(userId: Long): Long
}

/**
 * 대화 저장소 포트
 */
interface ConversationRepositoryPort {
    fun save(conversation: Conversation): Conversation
    fun findById(conversationId: Long): Conversation?
    fun findByParticipants(user1Id: Long, user2Id: Long): Conversation?
    fun findByUserId(userId: Long, limit: Int = 50, offset: Int = 0): List<Conversation>
}

/**
 * 메시지 저장소 포트
 */
interface MessageRepositoryPort {
    fun save(message: Message): Message
    fun findById(messageId: Long): Message?
    fun findByConversationId(conversationId: Long, limit: Int = 50, offset: Int = 0): List<Message>
    fun markAsRead(messageId: Long): Boolean
    fun delete(messageId: Long): Boolean
}

/**
 * 스토리 저장소 포트
 */
interface StoryRepositoryPort {
    fun save(story: Story): Story
    fun findById(storyId: Long): Story?
    fun findByUserId(userId: Long): List<Story>
    fun findActive(userId: Long, followingIds: List<Long>): List<Story>
    fun delete(storyId: Long): Boolean
    fun deleteExpired(): Int
}

/**
 * 스토리 조회 기록 저장소 포트
 */
interface StoryViewRepositoryPort {
    fun save(storyView: StoryView): StoryView
    fun findByStoryId(storyId: Long): List<StoryView>
    fun exists(storyId: Long, viewerId: Long): Boolean
}
