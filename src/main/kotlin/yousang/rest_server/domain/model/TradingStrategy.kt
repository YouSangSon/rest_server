package yousang.rest_server.domain.model

import yousang.rest_server.application.service.StrategyType
import java.time.LocalDateTime

/**
 * 트레이딩 전략 도메인 모델
 */
data class TradingStrategy(
    val id: Long? = null,
    val userId: Long,
    val name: String,
    val strategyType: StrategyType,
    val symbols: List<String>,
    val exchange: String,
    val isActive: Boolean = true,
    val parameters: Map<String, String> = emptyMap(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(
            userId: Long,
            name: String,
            strategyType: StrategyType,
            symbols: List<String>,
            exchange: String,
            parameters: Map<String, String> = emptyMap()
        ): TradingStrategy {
            require(name.isNotBlank()) { "Strategy name cannot be blank" }
            require(symbols.isNotEmpty()) { "At least one symbol is required" }
            require(exchange.isNotBlank()) { "Exchange cannot be blank" }

            return TradingStrategy(
                userId = userId,
                name = name,
                strategyType = strategyType,
                symbols = symbols,
                exchange = exchange,
                parameters = parameters
            )
        }
    }

    fun activate(): TradingStrategy = copy(isActive = true, updatedAt = LocalDateTime.now())
    fun deactivate(): TradingStrategy = copy(isActive = false, updatedAt = LocalDateTime.now())

    fun updateSymbols(newSymbols: List<String>): TradingStrategy {
        require(newSymbols.isNotEmpty()) { "At least one symbol is required" }
        return copy(symbols = newSymbols, updatedAt = LocalDateTime.now())
    }

    fun updateParameters(newParameters: Map<String, String>): TradingStrategy {
        return copy(parameters = newParameters, updatedAt = LocalDateTime.now())
    }
}
