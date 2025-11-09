package yousang.rest_server.application.ports.`in`

import yousang.rest_server.application.service.CreateStrategyCommand
import yousang.rest_server.application.service.UpdateStrategyCommand
import yousang.rest_server.domain.model.Order
import yousang.rest_server.domain.model.TradingStrategy

/**
 * 트레이딩 전략 실행 Use Case
 */
interface ExecuteTradingStrategyUseCase {
    fun executeStrategy(strategyId: Long, userId: Long): List<Order>
    fun executeAllActiveStrategies(userId: Long): Map<Long, List<Order>>
}

/**
 * 트레이딩 전략 관리 Use Case
 */
interface ManageTradingStrategyUseCase {
    fun createStrategy(command: CreateStrategyCommand): TradingStrategy
    fun updateStrategy(strategyId: Long, command: UpdateStrategyCommand): TradingStrategy
    fun deleteStrategy(strategyId: Long, userId: Long)
    fun getStrategy(strategyId: Long): TradingStrategy?
    fun getStrategiesByUser(userId: Long): List<TradingStrategy>
    fun getActiveStrategies(userId: Long): List<TradingStrategy>
}
