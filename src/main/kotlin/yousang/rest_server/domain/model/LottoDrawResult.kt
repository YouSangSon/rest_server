package yousang.rest_server.domain.model

import java.time.LocalDateTime

/**
 * 로또 추첨 결과 도메인 모델
 *
 * 당첨 등수:
 * - 1등: 6개 번호 일치
 * - 2등: 5개 번호 + 보너스 번호 일치
 * - 3등: 5개 번호 일치
 * - 4등: 4개 번호 일치
 * - 5등: 3개 번호 일치
 */
data class LottoDrawResult(
    val id: Long? = null,
    val drawNumber: Int, // 회차
    val winningNumbers: List<Int>, // 당첨 번호 6개 (정렬됨)
    val bonusNumber: Int, // 보너스 번호
    val drawDate: LocalDateTime,
    val firstPrizeAmount: Long = 0, // 1등 상금
    val firstPrizeWinnerCount: Int = 0, // 1등 당첨자 수
    val totalSalesAmount: Long = 0 // 총 판매액
) {
    init {
        require(winningNumbers.size == 6) { "당첨 번호는 6개여야 합니다" }
        require(winningNumbers.distinct().size == 6) { "당첨 번호에 중복이 있습니다" }
        require(winningNumbers.all { it in 1..45 }) { "당첨 번호는 1~45 범위여야 합니다" }
        require(bonusNumber in 1..45) { "보너스 번호는 1~45 범위여야 합니다" }
        require(bonusNumber !in winningNumbers) { "보너스 번호는 당첨 번호와 중복될 수 없습니다" }
    }

    /**
     * 티켓의 당첨 등수 계산
     * @return 1~5등, null이면 미당첨
     */
    fun calculateRank(ticket: LottoTicket): Int? {
        val matchCount = ticket.numbers.count { it in winningNumbers }
        val hasBonusNumber = bonusNumber in ticket.numbers

        return when {
            matchCount == 6 -> 1 // 1등: 6개 일치
            matchCount == 5 && hasBonusNumber -> 2 // 2등: 5개 + 보너스
            matchCount == 5 -> 3 // 3등: 5개 일치
            matchCount == 4 -> 4 // 4등: 4개 일치
            matchCount == 3 -> 5 // 5등: 3개 일치
            else -> null // 미당첨
        }
    }

    /**
     * 등수별 예상 상금 계산 (실제 상금은 당첨자 수에 따라 변동)
     */
    fun getEstimatedPrizeAmount(rank: Int): Long {
        return when (rank) {
            1 -> firstPrizeAmount
            2 -> firstPrizeAmount / 10 // 약 1등 상금의 10%
            3 -> 1_500_000L // 고정 금액
            4 -> 50_000L // 고정 금액
            5 -> 5_000L // 고정 금액
            else -> 0L
        }
    }

    companion object {
        /**
         * 랜덤 추첨 결과 생성 (테스트용)
         */
        fun generateRandom(drawNumber: Int): LottoDrawResult {
            val allNumbers = (1..45).shuffled()
            val winningNumbers = allNumbers.take(6).sorted()
            val bonusNumber = allNumbers[6]

            return LottoDrawResult(
                drawNumber = drawNumber,
                winningNumbers = winningNumbers,
                bonusNumber = bonusNumber,
                drawDate = LocalDateTime.now(),
                firstPrizeAmount = 2_000_000_000L, // 예: 20억
                firstPrizeWinnerCount = 10,
                totalSalesAmount = 100_000_000_000L // 예: 1000억
            )
        }
    }
}
