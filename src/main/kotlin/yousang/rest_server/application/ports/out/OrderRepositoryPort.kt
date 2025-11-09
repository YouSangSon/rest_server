package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.model.Order
import yousang.rest_server.domain.model.OrderStatus
import java.time.LocalDateTime

/**
 * 주문 Repository Port (Outbound Port)
 *
 * 주문 데이터 저장 및 조회를 위한 포트.
 * PostgreSQL JPA로 구현됩니다.
 */
interface OrderRepositoryPort {
    /**
     * 주문을 저장합니다.
     */
    fun save(order: Order): Order

    /**
     * ID로 주문을 조회합니다.
     */
    fun findById(id: Long): Order?

    /**
     * 주문 ID로 조회합니다.
     */
    fun findByOrderId(orderId: String): Order?

    /**
     * 사용자의 주문 목록을 조회합니다.
     */
    fun findByUserId(userId: Long, limit: Int = 100): List<Order>

    /**
     * 사용자의 특정 심볼 주문을 조회합니다.
     */
    fun findByUserIdAndSymbol(userId: Long, symbol: String, limit: Int = 100): List<Order>

    /**
     * 특정 상태의 주문을 조회합니다.
     */
    fun findByStatus(status: OrderStatus, limit: Int = 100): List<Order>

    /**
     * 전략 ID로 주문을 조회합니다 (자동매매).
     */
    fun findByStrategyId(strategyId: Long): List<Order>

    /**
     * 날짜 범위 내의 주문을 조회합니다.
     */
    fun findByDateRange(from: LocalDateTime, to: LocalDateTime): List<Order>

    /**
     * 주문을 삭제합니다.
     */
    fun deleteById(id: Long)
}
