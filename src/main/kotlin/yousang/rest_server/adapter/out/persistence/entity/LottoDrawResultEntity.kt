package yousang.rest_server.adapter.out.persistence.entity

import jakarta.persistence.*
import yousang.rest_server.domain.model.LottoDrawResult
import java.time.LocalDateTime

/**
 * 로또 추첨 결과 JPA 엔티티
 */
@Entity
@Table(name = "lotto_draw_results")
data class LottoDrawResultEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val drawNumber: Int,

    @Column(nullable = false, columnDefinition = "TEXT")
    val winningNumbers: String, // 쉼표로 구분된 번호

    @Column(nullable = false)
    val bonusNumber: Int,

    @Column(nullable = false)
    val drawDate: LocalDateTime,

    @Column(nullable = false)
    val firstPrizeAmount: Long = 0,

    @Column(nullable = false)
    val firstPrizeWinnerCount: Int = 0,

    @Column(nullable = false)
    val totalSalesAmount: Long = 0
) {
    /**
     * JPA 엔티티를 도메인 모델로 변환
     */
    fun toDomain(): LottoDrawResult {
        return LottoDrawResult(
            id = id,
            drawNumber = drawNumber,
            winningNumbers = winningNumbers.split(",").map { it.toInt() },
            bonusNumber = bonusNumber,
            drawDate = drawDate,
            firstPrizeAmount = firstPrizeAmount,
            firstPrizeWinnerCount = firstPrizeWinnerCount,
            totalSalesAmount = totalSalesAmount
        )
    }

    companion object {
        /**
         * 도메인 모델을 JPA 엔티티로 변환
         */
        fun fromDomain(domain: LottoDrawResult): LottoDrawResultEntity {
            return LottoDrawResultEntity(
                id = domain.id,
                drawNumber = domain.drawNumber,
                winningNumbers = domain.winningNumbers.joinToString(","),
                bonusNumber = domain.bonusNumber,
                drawDate = domain.drawDate,
                firstPrizeAmount = domain.firstPrizeAmount,
                firstPrizeWinnerCount = domain.firstPrizeWinnerCount,
                totalSalesAmount = domain.totalSalesAmount
            )
        }
    }
}
