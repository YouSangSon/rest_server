package yousang.rest_server.adapter.out.persistence.jpa.trading

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import yousang.rest_server.application.ports.out.OrderRepositoryPort
import yousang.rest_server.domain.model.Order
import yousang.rest_server.domain.model.OrderStatus
import java.time.LocalDateTime

/**
 * 주문 Repository Adapter
 *
 * OrderRepositoryPort를 구현하여 JPA를 통해 주문 데이터를 저장/조회합니다.
 */
@Component
class OrderRepositoryAdapter(
    private val orderJpaRepository: OrderJpaRepository
) : OrderRepositoryPort {

    override fun save(order: Order): Order {
        val entity = OrderJpaEntity.fromDomain(order)
        val saved = orderJpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: Long): Order? {
        return orderJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByOrderId(orderId: String): Order? {
        return orderJpaRepository.findByOrderId(orderId)?.toDomain()
    }

    override fun findByUserId(userId: Long, limit: Int): List<Order> {
        val pageable = PageRequest.of(0, limit)
        return orderJpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map { it.toDomain() }
    }

    override fun findByUserIdAndSymbol(userId: Long, symbol: String, limit: Int): List<Order> {
        val pageable = PageRequest.of(0, limit)
        return orderJpaRepository.findByUserIdAndSymbolOrderByCreatedAtDesc(userId, symbol, pageable)
            .map { it.toDomain() }
    }

    override fun findByStatus(status: OrderStatus, limit: Int): List<Order> {
        val pageable = PageRequest.of(0, limit)
        return orderJpaRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
            .map { it.toDomain() }
    }

    override fun findByStrategyId(strategyId: Long): List<Order> {
        return orderJpaRepository.findByStrategyIdOrderByCreatedAtDesc(strategyId)
            .map { it.toDomain() }
    }

    override fun findByDateRange(from: LocalDateTime, to: LocalDateTime): List<Order> {
        return orderJpaRepository.findByDateRange(from, to)
            .map { it.toDomain() }
    }

    override fun deleteById(id: Long) {
        orderJpaRepository.deleteById(id)
    }
}
