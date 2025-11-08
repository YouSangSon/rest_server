package yousang.rest_server.adapter.out.persistence.jpa.trading

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TradingStrategyJpaRepository : JpaRepository<TradingStrategyJpaEntity, Long> {
    fun findByUserId(userId: Long): List<TradingStrategyJpaEntity>
    fun findByUserIdAndIsActiveTrue(userId: Long): List<TradingStrategyJpaEntity>
    fun findByStrategyType(strategyType: String): List<TradingStrategyJpaEntity>
}
