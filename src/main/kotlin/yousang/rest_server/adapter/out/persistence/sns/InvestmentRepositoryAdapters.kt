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
 * Investment Portfolio Repository Adapter (Database Service - PostgreSQL)
 */
@Component
@Primary
class InvestmentPortfolioRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient
) : InvestmentPortfolioRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_investment_portfolios"
        private const val DB_TYPE = DatabaseServiceClient.DB_POSTGRES
    }

    override fun save(portfolio: InvestmentPortfolio): InvestmentPortfolio {
        val document = mapOf(
            "portfolioId" to portfolio.portfolioId,
            "userId" to portfolio.userId,
            "name" to portfolio.name,
            "description" to portfolio.description,
            "isPublic" to portfolio.isPublic,
            "totalValue" to portfolio.totalValue.toString(),
            "totalCost" to portfolio.totalCost.toString(),
            "totalReturn" to portfolio.totalReturn.toString(),
            "returnRate" to portfolio.returnRate.toString(),
            "followerCount" to portfolio.followerCount,
            "createdAt" to portfolio.createdAt.toString(),
            "updatedAt" to portfolio.updatedAt.toString()
        )

        val response = if (portfolio.portfolioId == 0L) {
            databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        } else {
            databaseServiceClient.upsert(
                collection = COLLECTION,
                filter = mapOf("portfolioId" to portfolio.portfolioId),
                document = document,
                databaseType = DB_TYPE
            )
        }

        return documentToPortfolio(response.data as Map<*, *>)
    }

    override fun findById(portfolioId: Long): InvestmentPortfolio? {
        val response = databaseServiceClient.findById(
            collection = COLLECTION,
            id = portfolioId.toString(),
            databaseType = DB_TYPE,
            responseType = Map::class.java
        ) ?: return null

        return documentToPortfolio(response.data as Map<*, *>)
    }

    override fun findByUserId(userId: Long): List<InvestmentPortfolio> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("userId" to userId),
            sort = mapOf("createdAt" to -1),
            limit = 100,
            offset = 0,
            databaseType = DB_TYPE
        )

        val portfolios = response.data as? List<*> ?: return emptyList()
        return portfolios.map { documentToPortfolio(it as Map<*, *>) }
    }

    override fun findPublicPortfolios(limit: Int, offset: Int): List<InvestmentPortfolio> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("isPublic" to true),
            sort = mapOf("followerCount" to -1, "createdAt" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val portfolios = response.data as? List<*> ?: return emptyList()
        return portfolios.map { documentToPortfolio(it as Map<*, *>) }
    }

    override fun delete(portfolioId: Long): Boolean {
        val response = databaseServiceClient.delete(
            collection = COLLECTION,
            id = portfolioId.toString(),
            databaseType = DB_TYPE
        )
        return response.data as? Boolean ?: false
    }

    override fun search(query: String, limit: Int, offset: Int): List<InvestmentPortfolio> {
        val response = databaseServiceClient.search<Map<String, Any>>(
            collection = COLLECTION,
            searchQuery = query,
            fields = listOf("name", "description"),
            limit = limit,
            databaseType = DB_TYPE
        )

        val portfolios = response.data as? List<*> ?: return emptyList()
        return portfolios.map { documentToPortfolio(it as Map<*, *>) }
    }

    private fun documentToPortfolio(doc: Map<*, *>): InvestmentPortfolio {
        return InvestmentPortfolio(
            portfolioId = (doc["portfolioId"] as Number).toLong(),
            userId = (doc["userId"] as Number).toLong(),
            name = doc["name"] as String,
            description = doc["description"] as? String,
            isPublic = doc["isPublic"] as? Boolean ?: false,
            totalValue = BigDecimal(doc["totalValue"].toString()),
            totalCost = BigDecimal(doc["totalCost"].toString()),
            totalReturn = BigDecimal(doc["totalReturn"].toString()),
            returnRate = BigDecimal(doc["returnRate"].toString()),
            followerCount = (doc["followerCount"] as? Number)?.toInt() ?: 0,
            createdAt = LocalDateTime.parse(doc["createdAt"] as String),
            updatedAt = LocalDateTime.parse(doc["updatedAt"] as String)
        )
    }
}

/**
 * Asset Holding Repository Adapter (Database Service - PostgreSQL)
 */
@Component
@Primary
class AssetHoldingRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient
) : AssetHoldingRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_asset_holdings"
        private const val DB_TYPE = DatabaseServiceClient.DB_POSTGRES
    }

    override fun save(holding: AssetHolding): AssetHolding {
        val document = mapOf(
            "holdingId" to holding.holdingId,
            "portfolioId" to holding.portfolioId,
            "assetType" to holding.assetType.name,
            "symbol" to holding.symbol,
            "quantity" to holding.quantity.toString(),
            "averagePrice" to holding.averagePrice.toString(),
            "currentPrice" to holding.currentPrice.toString(),
            "createdAt" to holding.createdAt.toString(),
            "updatedAt" to holding.updatedAt.toString()
        )

        val response = if (holding.holdingId == 0L) {
            databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        } else {
            databaseServiceClient.upsert(
                collection = COLLECTION,
                filter = mapOf("holdingId" to holding.holdingId),
                document = document,
                databaseType = DB_TYPE
            )
        }

        return documentToHolding(response.data as Map<*, *>)
    }

    override fun findById(holdingId: Long): AssetHolding? {
        val response = databaseServiceClient.findById(
            collection = COLLECTION,
            id = holdingId.toString(),
            databaseType = DB_TYPE,
            responseType = Map::class.java
        ) ?: return null

        return documentToHolding(response.data as Map<*, *>)
    }

    override fun findByPortfolioId(portfolioId: Long): List<AssetHolding> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("portfolioId" to portfolioId),
            sort = mapOf("createdAt" to -1),
            limit = 100,
            offset = 0,
            databaseType = DB_TYPE
        )

        val holdings = response.data as? List<*> ?: return emptyList()
        return holdings.map { documentToHolding(it as Map<*, *>) }
    }

    override fun findBySymbol(portfolioId: Long, symbol: String): AssetHolding? {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("portfolioId" to portfolioId, "symbol" to symbol),
            limit = 1,
            databaseType = DB_TYPE
        )

        val holdings = response.data as? List<*> ?: return null
        return if (holdings.isNotEmpty()) {
            documentToHolding(holdings[0] as Map<*, *>)
        } else {
            null
        }
    }

    override fun delete(holdingId: Long): Boolean {
        val response = databaseServiceClient.delete(
            collection = COLLECTION,
            id = holdingId.toString(),
            databaseType = DB_TYPE
        )
        return response.data as? Boolean ?: false
    }

    override fun deleteByPortfolioId(portfolioId: Long): Int {
        val response = databaseServiceClient.deleteMany(
            collection = COLLECTION,
            filter = mapOf("portfolioId" to portfolioId),
            databaseType = DB_TYPE
        )
        return (response.data as? Map<*, *>)?.get("deletedCount") as? Int ?: 0
    }

    private fun documentToHolding(doc: Map<*, *>): AssetHolding {
        return AssetHolding(
            holdingId = (doc["holdingId"] as Number).toLong(),
            portfolioId = (doc["portfolioId"] as Number).toLong(),
            assetType = AssetType.valueOf(doc["assetType"] as String),
            symbol = doc["symbol"] as String,
            quantity = BigDecimal(doc["quantity"].toString()),
            averagePrice = BigDecimal(doc["averagePrice"].toString()),
            currentPrice = BigDecimal(doc["currentPrice"].toString()),
            createdAt = LocalDateTime.parse(doc["createdAt"] as String),
            updatedAt = LocalDateTime.parse(doc["updatedAt"] as String)
        )
    }
}

/**
 * Trade History Repository Adapter (Database Service - PostgreSQL)
 */
@Component
@Primary
class TradeHistoryRepositoryAdapter(
    private val databaseServiceClient: DatabaseServiceClient
) : TradeHistoryRepositoryPort {

    companion object {
        private const val COLLECTION = "sns_trade_history"
        private const val DB_TYPE = DatabaseServiceClient.DB_POSTGRES
    }

    override fun save(trade: TradeHistory): TradeHistory {
        val document = mapOf(
            "tradeId" to trade.tradeId,
            "portfolioId" to trade.portfolioId,
            "assetType" to trade.assetType.name,
            "symbol" to trade.symbol,
            "tradeType" to trade.tradeType.name,
            "quantity" to trade.quantity.toString(),
            "price" to trade.price.toString(),
            "fee" to trade.fee.toString(),
            "tradeDate" to trade.tradeDate.toString(),
            "createdAt" to trade.createdAt.toString()
        )

        val response = databaseServiceClient.create(COLLECTION, document, DB_TYPE)
        return documentToTrade(response.data as Map<*, *>)
    }

    override fun findById(tradeId: Long): TradeHistory? {
        val response = databaseServiceClient.findById(
            collection = COLLECTION,
            id = tradeId.toString(),
            databaseType = DB_TYPE,
            responseType = Map::class.java
        ) ?: return null

        return documentToTrade(response.data as Map<*, *>)
    }

    override fun findByPortfolioId(portfolioId: Long, limit: Int, offset: Int): List<TradeHistory> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("portfolioId" to portfolioId),
            sort = mapOf("tradeDate" to -1),
            limit = limit,
            offset = offset,
            databaseType = DB_TYPE
        )

        val trades = response.data as? List<*> ?: return emptyList()
        return trades.map { documentToTrade(it as Map<*, *>) }
    }

    override fun findByPortfolioIdAndSymbol(portfolioId: Long, symbol: String, limit: Int): List<TradeHistory> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf("portfolioId" to portfolioId, "symbol" to symbol),
            sort = mapOf("tradeDate" to -1),
            limit = limit,
            offset = 0,
            databaseType = DB_TYPE
        )

        val trades = response.data as? List<*> ?: return emptyList()
        return trades.map { documentToTrade(it as Map<*, *>) }
    }

    override fun findByDateRange(
        portfolioId: Long,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<TradeHistory> {
        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = COLLECTION,
            filter = mapOf(
                "portfolioId" to portfolioId,
                "tradeDate" to mapOf(
                    "\$gte" to from.toString(),
                    "\$lte" to to.toString()
                )
            ),
            sort = mapOf("tradeDate" to -1),
            limit = 1000,
            offset = 0,
            databaseType = DB_TYPE
        )

        val trades = response.data as? List<*> ?: return emptyList()
        return trades.map { documentToTrade(it as Map<*, *>) }
    }

    override fun delete(tradeId: Long): Boolean {
        val response = databaseServiceClient.delete(
            collection = COLLECTION,
            id = tradeId.toString(),
            databaseType = DB_TYPE
        )
        return response.data as? Boolean ?: false
    }

    private fun documentToTrade(doc: Map<*, *>): TradeHistory {
        return TradeHistory(
            tradeId = (doc["tradeId"] as Number).toLong(),
            portfolioId = (doc["portfolioId"] as Number).toLong(),
            assetType = AssetType.valueOf(doc["assetType"] as String),
            symbol = doc["symbol"] as String,
            tradeType = TradeType.valueOf(doc["tradeType"] as String),
            quantity = BigDecimal(doc["quantity"].toString()),
            price = BigDecimal(doc["price"].toString()),
            fee = BigDecimal(doc["fee"].toString()),
            tradeDate = LocalDateTime.parse(doc["tradeDate"] as String),
            createdAt = LocalDateTime.parse(doc["createdAt"] as String)
        )
    }
}
