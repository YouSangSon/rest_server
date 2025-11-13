package yousang.rest_server.adapter.out.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import yousang.rest_server.adapter.out.database.DatabaseServiceClient
import yousang.rest_server.application.ports.out.OrderRepositoryPort
import yousang.rest_server.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Order Repository - Database Service (PostgreSQL) 기반 구현
 */
@Component
@Primary
class OrderRepositoryDatabaseServiceAdapter(
    private val databaseServiceClient: DatabaseServiceClient,
    private val objectMapper: ObjectMapper
) : OrderRepositoryPort {

    companion object {
        const val TABLE_ORDERS = "orders"
    }

    override fun save(order: Order): Order {
        val document = order.toDocument()

        // Upsert: orderId로 찾아서 있으면 업데이트, 없으면 생성
        val filter = mapOf("orderId" to order.orderId)

        val response = databaseServiceClient.upsert(
            collection = TABLE_ORDERS,
            filter = filter,
            document = document,
            databaseType = DatabaseServiceClient.DB_POSTGRES
        )

        return if (response.success) {
            response.data?.let {
                objectMapper.convertValue(it, OrderDocument::class.java).toDomain()
            } ?: order
        } else {
            throw IllegalStateException("Failed to save order: ${response.error?.message}")
        }
    }

    override fun findByOrderId(orderId: String): Order? {
        val filter = mapOf("orderId" to orderId)

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = TABLE_ORDERS,
            filter = filter,
            limit = 1,
            databaseType = DatabaseServiceClient.DB_POSTGRES
        )

        return response.data?.firstOrNull()?.let {
            objectMapper.convertValue(it, OrderDocument::class.java).toDomain()
        }
    }

    override fun findByUserId(userId: Long, limit: Int): List<Order> {
        val filter = mapOf("userId" to userId)
        val sort = mapOf("createdAt" to -1)

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = TABLE_ORDERS,
            filter = filter,
            sort = sort,
            limit = limit,
            databaseType = DatabaseServiceClient.DB_POSTGRES
        )

        return response.data?.map {
            objectMapper.convertValue(it, OrderDocument::class.java).toDomain()
        } ?: emptyList()
    }

    override fun findByUserIdAndSymbol(userId: Long, symbol: String, limit: Int): List<Order> {
        val filter = mapOf(
            "userId" to userId,
            "symbol" to symbol
        )
        val sort = mapOf("createdAt" to -1)

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = TABLE_ORDERS,
            filter = filter,
            sort = sort,
            limit = limit,
            databaseType = DatabaseServiceClient.DB_POSTGRES
        )

        return response.data?.map {
            objectMapper.convertValue(it, OrderDocument::class.java).toDomain()
        } ?: emptyList()
    }

    override fun findByStatus(status: OrderStatus, limit: Int): List<Order> {
        val filter = mapOf("status" to status.name)
        val sort = mapOf("createdAt" to -1)

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = TABLE_ORDERS,
            filter = filter,
            sort = sort,
            limit = limit,
            databaseType = DatabaseServiceClient.DB_POSTGRES
        )

        return response.data?.map {
            objectMapper.convertValue(it, OrderDocument::class.java).toDomain()
        } ?: emptyList()
    }

    override fun findByDateRange(from: LocalDateTime, to: LocalDateTime): List<Order> {
        val filter = mapOf(
            "createdAt" to mapOf(
                "\$gte" to from.toString(),
                "\$lte" to to.toString()
            )
        )
        val sort = mapOf("createdAt" to -1)

        val response = databaseServiceClient.find<Map<String, Any>>(
            collection = TABLE_ORDERS,
            filter = filter,
            sort = sort,
            limit = 10000,
            databaseType = DatabaseServiceClient.DB_POSTGRES
        )

        return response.data?.map {
            objectMapper.convertValue(it, OrderDocument::class.java).toDomain()
        } ?: emptyList()
    }
}

// ==================== Document Model ====================

data class OrderDocument(
    val _id: String? = null,
    val id: Long? = null,
    val orderId: String,
    val userId: Long,
    val symbol: String,
    val exchange: String,
    val type: String,
    val side: String,
    val quantity: String, // BigDecimal as String
    val price: String? = null,
    val stopPrice: String? = null,
    val timeInForce: String,
    val status: String,
    val executedQty: String,
    val averagePrice: String? = null,
    val strategyId: Long? = null,
    val errorMessage: String? = null,
    val createdAt: String,
    val updatedAt: String
) {
    fun toDomain(): Order {
        return Order(
            id = id,
            orderId = orderId,
            userId = userId,
            symbol = symbol,
            exchange = exchange,
            type = OrderType.valueOf(type),
            side = OrderSide.valueOf(side),
            quantity = BigDecimal(quantity),
            price = price?.let { BigDecimal(it) },
            stopPrice = stopPrice?.let { BigDecimal(it) },
            timeInForce = TimeInForce.valueOf(timeInForce),
            status = OrderStatus.valueOf(status),
            executedQty = BigDecimal(executedQty),
            averagePrice = averagePrice?.let { BigDecimal(it) },
            strategyId = strategyId,
            errorMessage = errorMessage,
            createdAt = LocalDateTime.parse(createdAt),
            updatedAt = LocalDateTime.parse(updatedAt)
        )
    }
}

fun Order.toDocument(): OrderDocument {
    return OrderDocument(
        id = this.id,
        orderId = this.orderId,
        userId = this.userId,
        symbol = this.symbol,
        exchange = this.exchange,
        type = this.type.name,
        side = this.side.name,
        quantity = this.quantity.toString(),
        price = this.price?.toString(),
        stopPrice = this.stopPrice?.toString(),
        timeInForce = this.timeInForce.name,
        status = this.status.name,
        executedQty = this.executedQty.toString(),
        averagePrice = this.averagePrice?.toString(),
        strategyId = this.strategyId,
        errorMessage = this.errorMessage,
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString()
    )
}
