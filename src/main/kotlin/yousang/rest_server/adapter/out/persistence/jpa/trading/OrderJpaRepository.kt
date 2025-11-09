package yousang.rest_server.adapter.out.persistence.jpa.trading

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import yousang.rest_server.domain.model.OrderStatus
import java.time.LocalDateTime

/**
 * 주문 JPA Repository
 */
@Repository
interface OrderJpaRepository : JpaRepository<OrderJpaEntity, Long> {

    /**
     * 주문 ID로 조회
     */
    fun findByOrderId(orderId: String): OrderJpaEntity?

    /**
     * 사용자별 주문 조회
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): List<OrderJpaEntity>

    /**
     * 사용자 + 심볼별 주문 조회
     */
    fun findByUserIdAndSymbolOrderByCreatedAtDesc(
        userId: Long,
        symbol: String,
        pageable: Pageable
    ): List<OrderJpaEntity>

    /**
     * 상태별 주문 조회
     */
    fun findByStatusOrderByCreatedAtDesc(status: OrderStatus, pageable: Pageable): List<OrderJpaEntity>

    /**
     * 전략별 주문 조회
     */
    fun findByStrategyIdOrderByCreatedAtDesc(strategyId: Long): List<OrderJpaEntity>

    /**
     * 날짜 범위 내 주문 조회
     */
    @Query("SELECT o FROM OrderJpaEntity o WHERE o.createdAt BETWEEN :from AND :to ORDER BY o.createdAt DESC")
    fun findByDateRange(from: LocalDateTime, to: LocalDateTime): List<OrderJpaEntity>

    /**
     * 사용자의 활성 주문 조회
     */
    @Query("SELECT o FROM OrderJpaEntity o WHERE o.userId = :userId AND o.status IN ('PENDING', 'SUBMITTED', 'PARTIALLY_FILLED') ORDER BY o.createdAt DESC")
    fun findActiveOrdersByUserId(userId: Long): List<OrderJpaEntity>

    /**
     * 심볼의 활성 주문 조회
     */
    @Query("SELECT o FROM OrderJpaEntity o WHERE o.symbol = :symbol AND o.status IN ('PENDING', 'SUBMITTED', 'PARTIALLY_FILLED') ORDER BY o.createdAt DESC")
    fun findActiveOrdersBySymbol(symbol: String): List<OrderJpaEntity>

    /**
     * 사용자의 체결된 주문 수
     */
    fun countByUserIdAndStatus(userId: Long, status: OrderStatus): Long

    /**
     * 전략의 총 거래 횟수
     */
    fun countByStrategyId(strategyId: Long): Long
}
