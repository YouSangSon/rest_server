package yousang.rest_server.adapter.out.persistence.sns.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import yousang.rest_server.adapter.out.persistence.sns.entity.AssetHoldingEntity

@Repository
interface AssetHoldingJpaRepository : JpaRepository<AssetHoldingEntity, Long> {
    fun findByPortfolioId(portfolioId: Long): List<AssetHoldingEntity>
    fun findByPortfolioIdAndAssetSymbol(portfolioId: Long, assetSymbol: String): AssetHoldingEntity?
    fun deleteByPortfolioId(portfolioId: Long)
}
