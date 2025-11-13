package yousang.rest_server.adapter.out.persistence.sns

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import yousang.rest_server.adapter.out.database.DatabaseServiceClient
import yousang.rest_server.application.ports.out.*
import yousang.rest_server.domain.sns.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Investment Post Repository Adapter (Database Service - MongoDB)
 */
@Component
@Primary
class InvestmentPostRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient,
    private val objectMapper: ObjectMapper
) : InvestmentPostRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_investment_posts"
        private const val DB_TYPE = DatabaseServiceClient.DB_MONGODB
    }

    override fun save(investmentPost: InvestmentPost): InvestmentPost {
        val document = mapOf(
            "investmentPostId" to investmentPost.investmentPostId,
            "userId" to investmentPost.userId,
            "portfolioId" to investmentPost.portfolioId,
            "postType" to investmentPost.postType.name,
            "title" to investmentPost.title,
            "content" to investmentPost.content,
            "assetReferences" to investmentPost.assetReferences.map {
                mapOf("symbol" to it.symbol, "assetType" to it.assetType.name)
            },
            "likeCount" to investmentPost.likeCount,
            "commentCount" to investmentPost.commentCount,
            "bookmarkCount" to investmentPost.bookmarkCount,
            "viewCount" to investmentPost.viewCount,
            "voteCount" to investmentPost.voteCount,
            "createdAt" to investmentPost.createdAt.toString(),
            "updatedAt" to investmentPost.updatedAt.toString()
        )

        val response = if (investmentPost.investmentPostId == 0L) {
            databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        } else {
            databaseServiceClient.upsert(
                collection = COLLECTION,
                filter = mapOf("investmentPostId" to investmentPost.investmentPostId),
                document = document,
                databaseType = DB_TYPE
            )
        }

        return documentToInvestmentPost(response.data as Map<*, *>)
    }

    override fun findById(investmentPostId: Long): InvestmentPost? {
        val response = databaseServiceClient.findById(
            collection = COLLECTION,
            id = investmentPostId.toString(),
            databaseType = DB_TYPE,
            responseType = Map::class.java
        ) ?: return null

        return documentToInvestmentPost(response.data as Map<*, *>)
    }

    override fun findByUserId(userId: Long, limit: Int, offset: Int): List<InvestmentPost> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("userId" to userId),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val posts = response.data as? List<*> ?: return emptyList()
        return posts.map { documentToInvestmentPost(it as Map<*, *>) }
    }

    override fun findByPortfolioId(portfolioId: Long, limit: Int, offset: Int): List<InvestmentPost> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("portfolioId" to portfolioId),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val posts = response.data as? List<*> ?: return emptyList()
        return posts.map { documentToInvestmentPost(it as Map<*, *>) }
    }

    override fun findByType(postType: InvestmentPostType, limit: Int, offset: Int): List<InvestmentPost> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("postType" to postType.name),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val posts = response.data as? List<*> ?: return emptyList()
        return posts.map { documentToInvestmentPost(it as Map<*, *>) }
    }

    override fun findBySymbol(symbol: String, limit: Int, offset: Int): List<InvestmentPost> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("assetReferences.symbol" to symbol),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val posts = response.data as? List<*> ?: return emptyList()
        return posts.map { documentToInvestmentPost(it as Map<*, *>) }
    }

    override fun findFeed(userId: Long, limit: Int, offset: Int): List<InvestmentPost> {
        // For simplicity, return recent posts
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = emptyMap(),
            sort = mapOf("createdAt" to -1, "voteCount" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val posts = response.data as? List<*> ?: return emptyList()
        return posts.map { documentToInvestmentPost(it as Map<*, *>) }
    }

    override fun delete(investmentPostId: Long): Boolean {
        val response = databaseServiceClient.delete(
            collection = COLLECTION,
            id = investmentPostId.toString(),
            databaseType = DB_TYPE
        )
        return response.data as? Boolean ?: false
    }

    override fun search(query: String, limit: Int, offset: Int): List<InvestmentPost> {
        val response = databaseServiceClient.search<Map<String, Any>>(
            collection = COLLECTION,
            searchQuery = query,
            fields = listOf("title", "content"),
            limit = limit,
            databaseType = DB_TYPE
        )

        val posts = response.data as? List<*> ?: return emptyList()
        return posts.map { documentToInvestmentPost(it as Map<*, *>) }
    }

    private fun documentToInvestmentPost(doc: Map<*, *>): InvestmentPost {
        val assetRefs = (doc["assetReferences"] as? List<*>)?.map {
            val ref = it as Map<*, *>
            AssetReference(
                symbol = ref["symbol"] as String,
                assetType = AssetType.valueOf(ref["assetType"] as String)
            )
        } ?: emptyList()

        return InvestmentPost(
            investmentPostId = (doc["investmentPostId"] as Number).toLong(),
            userId = (doc["userId"] as Number).toLong(),
            portfolioId = (doc["portfolioId"] as? Number)?.toLong(),
            postType = InvestmentPostType.valueOf(doc["postType"] as String),
            title = doc["title"] as String,
            content = doc["content"] as String,
            assetReferences = assetRefs,
            likeCount = (doc["likeCount"] as? Number)?.toInt() ?: 0,
            commentCount = (doc["commentCount"] as? Number)?.toInt() ?: 0,
            bookmarkCount = (doc["bookmarkCount"] as? Number)?.toInt() ?: 0,
            viewCount = (doc["viewCount"] as? Number)?.toInt() ?: 0,
            voteCount = (doc["voteCount"] as? Number)?.toInt() ?: 0,
            createdAt = LocalDateTime.parse(doc["createdAt"] as String),
            updatedAt = LocalDateTime.parse(doc["updatedAt"] as String)
        )
    }
}

/**
 * Portfolio Follower Repository Adapter (Database Service - PostgreSQL)
 */
@Component
@Primary
class PortfolioFollowerRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient
) : PortfolioFollowerRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_portfolio_followers"
        private const val DB_TYPE = DatabaseServiceClient.DB_POSTGRES
    }

    override fun save(portfolioFollower: PortfolioFollower): PortfolioFollower {
        val document = mapOf(
            "portfolioFollowerId" to portfolioFollower.portfolioFollowerId,
            "portfolioId" to portfolioFollower.portfolioId,
            "userId" to portfolioFollower.userId,
            "createdAt" to portfolioFollower.createdAt.toString()
        )

        val response = databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        return documentToPortfolioFollower(response.data as Map<*, *>)
    }

    override fun delete(portfolioId: Long, userId: Long): Boolean {
        val response = databaseServiceClient.deleteMany(
            collection = COLLECTION,
            filter = mapOf("portfolioId" to portfolioId, "userId" to userId),
            databaseType = DB_TYPE
        )
        return (response.data as? Map<*, *>)?.get("deletedCount") as? Int ?: 0 > 0
    }

    override fun findByPortfolioId(portfolioId: Long, limit: Int, offset: Int): List<PortfolioFollower> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("portfolioId" to portfolioId),
            sort = mapOf("createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val followers = response.data as? List<*> ?: return emptyList()
        return followers.map { documentToPortfolioFollower(it as Map<*, *>) }
    }

    override fun exists(portfolioId: Long, userId: Long): Boolean {
        val count = databaseServiceClient.count(
            collection = COLLECTION,
            filter = mapOf("portfolioId" to portfolioId, "userId" to userId),
            databaseType = DB_TYPE
        )
        return (count.data as? Long ?: 0L) > 0
    }

    override fun countByPortfolioId(portfolioId: Long): Long {
        val response = databaseServiceClient.count(
            collection = COLLECTION,
            filter = mapOf("portfolioId" to portfolioId),
            databaseType = DB_TYPE
        )
        return (response.data as? Number)?.toLong() ?: 0L
    }

    private fun documentToPortfolioFollower(doc: Map<*, *>): PortfolioFollower {
        return PortfolioFollower(
            portfolioFollowerId = (doc["portfolioFollowerId"] as Number).toLong(),
            portfolioId = (doc["portfolioId"] as Number).toLong(),
            userId = (doc["userId"] as Number).toLong(),
            createdAt = LocalDateTime.parse(doc["createdAt"] as String)
        )
    }
}

/**
 * Watchlist Repository Adapter (Database Service - MongoDB)
 */
@Component
@Primary
class WatchlistRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient,
    private val objectMapper: ObjectMapper
) : WatchlistRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_watchlist"
        private const val DB_TYPE = DatabaseServiceClient.DB_MONGODB
    }

    override fun save(watchlistItem: WatchlistItem): WatchlistItem {
        val document = mapOf(
            "watchlistId" to watchlistItem.watchlistId,
            "userId" to watchlistItem.userId,
            "assetType" to watchlistItem.assetType.name,
            "symbol" to watchlistItem.symbol,
            "alertConditions" to watchlistItem.alertConditions.map {
                mapOf(
                    "id" to it.id,
                    "type" to it.type.name,
                    "value" to it.value.toString()
                )
            },
            "isActive" to watchlistItem.isActive,
            "createdAt" to watchlistItem.createdAt.toString(),
            "updatedAt" to watchlistItem.updatedAt.toString()
        )

        val response = if (watchlistItem.watchlistId == 0L) {
            databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        } else {
            databaseServiceClient.upsert(
                collection = COLLECTION,
                filter = mapOf("watchlistId" to watchlistItem.watchlistId),
                document = document,
                databaseType = DB_TYPE
            )
        }

        return documentToWatchlistItem(response.data as Map<*, *>)
    }

    override fun findById(watchlistId: Long): WatchlistItem? {
        val response = databaseServiceClient.findById(
            collection = COLLECTION,
            id = watchlistId.toString(),
            databaseType = DB_TYPE,
            responseType = Map::class.java
        ) ?: return null

        return documentToWatchlistItem(response.data as Map<*, *>)
    }

    override fun findByUserId(userId: Long): List<WatchlistItem> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("userId" to userId),
            sort = mapOf("createdAt" to -1),
            limit = 100,
            offset = 0,
            databaseType = DB_TYPE
        )

        val items = response.data as? List<*> ?: return emptyList()
        return items.map { documentToWatchlistItem(it as Map<*, *>) }
    }

    override fun findActiveByUserId(userId: Long): List<WatchlistItem> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("userId" to userId, "isActive" to true),
            sort = mapOf("createdAt" to -1),
            limit = 100,
            offset = 0,
            databaseType = DB_TYPE
        )

        val items = response.data as? List<*> ?: return emptyList()
        return items.map { documentToWatchlistItem(it as Map<*, *>) }
    }

    override fun findBySymbol(userId: Long, symbol: String): WatchlistItem? {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("userId" to userId, "symbol" to symbol),
            limit = 1,
            databaseType = DB_TYPE
        )

        val items = response.data as? List<*> ?: return null
        return if (items.isNotEmpty()) {
            documentToWatchlistItem(items[0] as Map<*, *>)
        } else {
            null
        }
    }

    override fun delete(watchlistId: Long): Boolean {
        val response = databaseServiceClient.delete(
            collection = COLLECTION,
            id = watchlistId.toString(),
            databaseType = DB_TYPE
        )
        return response.data as? Boolean ?: false
    }

    private fun documentToWatchlistItem(doc: Map<*, *>): WatchlistItem {
        val conditions = (doc["alertConditions"] as? List<*>)?.map {
            val cond = it as Map<*, *>
            AlertCondition(
                id = cond["id"] as String,
                type = AlertConditionType.valueOf(cond["type"] as String),
                value = BigDecimal(cond["value"].toString())
            )
        } ?: emptyList()

        return WatchlistItem(
            watchlistId = (doc["watchlistId"] as Number).toLong(),
            userId = (doc["userId"] as Number).toLong(),
            assetType = AssetType.valueOf(doc["assetType"] as String),
            symbol = doc["symbol"] as String,
            alertConditions = conditions,
            isActive = doc["isActive"] as? Boolean ?: true,
            createdAt = LocalDateTime.parse(doc["createdAt"] as String),
            updatedAt = LocalDateTime.parse(doc["updatedAt"] as String)
        )
    }
}

/**
 * Story Repository Adapter (Database Service - MongoDB)
 */
@Component
@Primary
class StoryRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient
) : StoryRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_stories"
        private const val DB_TYPE = DatabaseServiceClient.DB_MONGODB
    }

    override fun save(story: Story): Story {
        val document = mapOf(
            "storyId" to story.storyId,
            "userId" to story.userId,
            "mediaUrl" to story.mediaUrl,
            "mediaType" to story.mediaType.name,
            "caption" to story.caption,
            "viewCount" to story.viewCount,
            "expiresAt" to story.expiresAt.toString(),
            "createdAt" to story.createdAt.toString()
        )

        val response = databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        return documentToStory(response.data as Map<*, *>)
    }

    override fun findById(storyId: Long): Story? {
        val response = databaseServiceClient.findById(
            collection = COLLECTION,
            id = storyId.toString(),
            databaseType = DB_TYPE,
            responseType = Map::class.java
        ) ?: return null

        return documentToStory(response.data as Map<*, *>)
    }

    override fun findByUserId(userId: Long): List<Story> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("userId" to userId),
            sort = mapOf("createdAt" to -1),
            limit = 50,
            offset = 0,
            databaseType = DB_TYPE
        )

        val stories = response.data as? List<*> ?: return emptyList()
        return stories.map { documentToStory(it as Map<*, *>) }
    }

    override fun findActive(userId: Long, followingIds: List<Long>): List<Story> {
        val userIds = followingIds + userId
        val now = LocalDateTime.now()

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf(
                "userId" to mapOf("\$in" to userIds),
                "expiresAt" to mapOf("\$gt" to now.toString())
            ),
            sort = mapOf("createdAt" to -1),
            limit = 100,
            offset = 0,
            databaseType = DB_TYPE
        )

        val stories = response.data as? List<*> ?: return emptyList()
        return stories.map { documentToStory(it as Map<*, *>) }
    }

    override fun delete(storyId: Long): Boolean {
        val response = databaseServiceClient.delete(
            collection = COLLECTION,
            id = storyId.toString(),
            databaseType = DB_TYPE
        )
        return response.data as? Boolean ?: false
    }

    override fun deleteExpired(): Int {
        val now = LocalDateTime.now()
        val response = databaseServiceClient.deleteMany(
            collection = COLLECTION,
            filter = mapOf("expiresAt" to mapOf("\$lt" to now.toString())),
            databaseType = DB_TYPE
        )
        return (response.data as? Map<*, *>)?.get("deletedCount") as? Int ?: 0
    }

    private fun documentToStory(doc: Map<*, *>): Story {
        return Story(
            storyId = (doc["storyId"] as Number).toLong(),
            userId = (doc["userId"] as Number).toLong(),
            mediaUrl = doc["mediaUrl"] as String,
            mediaType = StoryMediaType.valueOf(doc["mediaType"] as String),
            caption = doc["caption"] as? String,
            viewCount = (doc["viewCount"] as? Number)?.toInt() ?: 0,
            expiresAt = LocalDateTime.parse(doc["expiresAt"] as String),
            createdAt = LocalDateTime.parse(doc["createdAt"] as String)
        )
    }
}

/**
 * Story View Repository Adapter (Database Service - MongoDB)
 */
@Component
@Primary
class StoryViewRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient
) : StoryViewRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_story_views"
        private const val DB_TYPE = DatabaseServiceClient.DB_MONGODB
    }

    override fun save(storyView: StoryView): StoryView {
        val document = mapOf(
            "storyViewId" to storyView.storyViewId,
            "storyId" to storyView.storyId,
            "viewerId" to storyView.viewerId,
            "viewedAt" to storyView.viewedAt.toString()
        )

        val response = databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        return documentToStoryView(response.data as Map<*, *>)
    }

    override fun findByStoryId(storyId: Long): List<StoryView> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("storyId" to storyId),
            sort = mapOf("viewedAt" to -1),
            limit = 100,
            offset = 0,
            databaseType = DB_TYPE
        )

        val views = response.data as? List<*> ?: return emptyList()
        return views.map { documentToStoryView(it as Map<*, *>) }
    }

    override fun exists(storyId: Long, viewerId: Long): Boolean {
        val count = databaseServiceClient.count(
            collection = COLLECTION,
            filter = mapOf("storyId" to storyId, "viewerId" to viewerId),
            databaseType = DB_TYPE
        )
        return (count.data as? Long ?: 0L) > 0
    }

    private fun documentToStoryView(doc: Map<*, *>): StoryView {
        return StoryView(
            storyViewId = (doc["storyViewId"] as Number).toLong(),
            storyId = (doc["storyId"] as Number).toLong(),
            viewerId = (doc["viewerId"] as Number).toLong(),
            viewedAt = LocalDateTime.parse(doc["viewedAt"] as String)
        )
    }
}
