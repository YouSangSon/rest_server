package yousang.rest_server.adapter.out.persistence.entity

import jakarta.persistence.*
import yousang.rest_server.domain.model.PensionLotteryDrawResult
import java.time.LocalDateTime

/**
 * 연금복권 추첨 결과 JPA 엔티티
 */
@Entity
@Table(name = "pension_lottery_draw_results")
data class PensionLotteryDrawResultEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val drawNumber: Int,

    @Column(nullable = false, name = "winning_group")
    val winningGroup: Int,

    @Column(nullable = false)
    val winningNumber: Int,

    @Column(nullable = false, columnDefinition = "TEXT")
    val bonusNumbers: String, // 쉼표로 구분된 보너스 번호들

    @Column(nullable = false)
    val drawDate: LocalDateTime,

    @Column(nullable = false)
    val firstPrizeWinnerCount: Int = 0,

    @Column(nullable = false)
    val totalSalesAmount: Long = 0
) {
    /**
     * JPA 엔티티를 도메인 모델로 변환
     */
    fun toDomain(): PensionLotteryDrawResult {
        return PensionLotteryDrawResult(
            id = id,
            drawNumber = drawNumber,
            winningGroup = winningGroup,
            winningNumber = winningNumber,
            bonusNumbers = bonusNumbers.split(",").map { it.toInt() },
            drawDate = drawDate,
            firstPrizeWinnerCount = firstPrizeWinnerCount,
            totalSalesAmount = totalSalesAmount
        )
    }

    companion object {
        /**
         * 도메인 모델을 JPA 엔티티로 변환
         */
        fun fromDomain(domain: PensionLotteryDrawResult): PensionLotteryDrawResultEntity {
            return PensionLotteryDrawResultEntity(
                id = domain.id,
                drawNumber = domain.drawNumber,
                winningGroup = domain.winningGroup,
                winningNumber = domain.winningNumber,
                bonusNumbers = domain.bonusNumbers.joinToString(","),
                drawDate = domain.drawDate,
                firstPrizeWinnerCount = domain.firstPrizeWinnerCount,
                totalSalesAmount = domain.totalSalesAmount
            )
        }
    }
}
