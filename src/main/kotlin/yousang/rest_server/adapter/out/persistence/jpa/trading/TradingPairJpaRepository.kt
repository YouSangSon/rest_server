package yousang.rest_server.adapter.out.persistence.jpa.trading

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * 거래 쌍 JPA Repository
 */
@Repository
interface TradingPairJpaRepository : JpaRepository<TradingPairJpaEntity, Long> {

    /**
     * 심볼과 거래소로 조회
     */
    fun findBySymbolAndExchange(symbol: String, exchange: String): TradingPairJpaEntity?

    /**
     * 심볼로 조회 (모든 거래소)
     */
    fun findBySymbol(symbol: String): List<TradingPairJpaEntity>

    /**
     * 거래소로 조회
     */
    fun findByExchange(exchange: String): List<TradingPairJpaEntity>

    /**
     * 활성 거래 쌍 조회
     */
    fun findByIsActiveTrue(): List<TradingPairJpaEntity>

    /**
     * 특정 거래소의 활성 거래 쌍 조회
     */
    fun findByExchangeAndIsActiveTrue(exchange: String): List<TradingPairJpaEntity>

    /**
     * 심볼 검색 (부분 일치)
     */
    @Query("SELECT tp FROM TradingPairJpaEntity tp WHERE tp.symbol LIKE %:query% AND tp.isActive = true")
    fun searchBySymbol(query: String): List<TradingPairJpaEntity>
}
