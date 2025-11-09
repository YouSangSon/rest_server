package yousang.rest_server.adapter.out.persistence.jpa.trading

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BacktestResultJpaRepository : JpaRepository<BacktestResultJpaEntity, Long> {
    fun findByStrategyIdOrderByCreatedAtDesc(strategyId: Long): List<BacktestResultJpaEntity>
    fun findTop10ByStrategyIdOrderByCreatedAtDesc(strategyId: Long): List<BacktestResultJpaEntity>
}
