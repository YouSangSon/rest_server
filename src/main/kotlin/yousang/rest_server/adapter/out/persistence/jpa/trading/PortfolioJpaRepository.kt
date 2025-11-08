package yousang.rest_server.adapter.out.persistence.jpa.trading

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PortfolioJpaRepository : JpaRepository<PortfolioJpaEntity, Long> {
    fun findByUserIdAndSymbol(userId: Long, symbol: String): PortfolioJpaEntity?
    fun findByUserId(userId: Long): List<PortfolioJpaEntity>
}
