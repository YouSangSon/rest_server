package yousang.rest_server.adapter.out.persistence.jpa.trading

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 백테스팅 결과 JPA Entity
 */
@Entity
@Table(name = "backtest_results")
class BacktestResultJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "strategy_id", nullable = false)
    val strategyId: Long,

    @Column(name = "start_date", nullable = false)
    val startDate: LocalDateTime,

    @Column(name = "end_date", nullable = false)
    val endDate: LocalDateTime,

    @Column(name = "initial_capital", nullable = false, precision = 20, scale = 8)
    val initialCapital: BigDecimal,

    @Column(name = "final_capital", nullable = false, precision = 20, scale = 8)
    val finalCapital: BigDecimal,

    @Column(name = "total_return", precision = 10, scale = 4)
    val totalReturn: BigDecimal? = null,

    @Column(name = "sharpe_ratio", precision = 10, scale = 4)
    val sharpeRatio: BigDecimal? = null,

    @Column(name = "max_drawdown", precision = 10, scale = 4)
    val maxDrawdown: BigDecimal? = null,

    @Column(name = "win_rate", precision = 10, scale = 4)
    val winRate: BigDecimal? = null,

    @Column(name = "total_trades")
    val totalTrades: Int? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 수익률을 계산합니다
     */
    fun calculateReturn(): BigDecimal {
        return if (initialCapital > BigDecimal.ZERO) {
            (finalCapital - initialCapital).divide(initialCapital, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
        } else {
            BigDecimal.ZERO
        }
    }

    /**
     * 순이익을 계산합니다
     */
    fun calculateProfit(): BigDecimal {
        return finalCapital - initialCapital
    }

    /**
     * 백테스팅이 성공적인지 확인합니다
     */
    fun isSuccessful(): Boolean {
        return finalCapital > initialCapital
    }

    /**
     * 성과 요약을 생성합니다
     */
    fun getPerformanceSummary(): String {
        return """
            |백테스팅 결과 요약:
            |기간: ${startDate.toLocalDate()} ~ ${endDate.toLocalDate()}
            |초기 자본: $initialCapital
            |최종 자본: $finalCapital
            |총 수익률: ${totalReturn ?: calculateReturn()}%
            |샤프 비율: ${sharpeRatio ?: "N/A"}
            |최대 손실률: ${maxDrawdown ?: "N/A"}%
            |승률: ${winRate ?: "N/A"}%
            |총 거래 횟수: ${totalTrades ?: "N/A"}
        """.trimMargin()
    }
}
