package yousang.rest_server.adapter.out.persistence.sns.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import yousang.rest_server.adapter.out.persistence.sns.entity.InvestmentPortfolioEntity

@Repository
interface InvestmentPortfolioJpaRepository : JpaRepository<InvestmentPortfolioEntity, Long> {
    fun findByUserId(userId: Long, pageable: Pageable): List<InvestmentPortfolioEntity>
    fun findByIsPublicTrue(pageable: Pageable): List<InvestmentPortfolioEntity>
    fun countByUserId(userId: Long): Long
}
