package yousang.rest_server.domain.model

import java.time.LocalDateTime

/**
 * 연금복권 추첨 결과 도메인 모델
 *
 * 당첨 등수:
 * - 1등: 조+6자리 모두 일치 (월 700만원 × 20년)
 * - 2등: 6자리 모두 일치 (1억원)
 * - 3등: 끝 5자리 일치 (100만원)
 * - 4등: 끝 4자리 일치 (10만원)
 * - 5등: 끝 3자리 일치 (5만원)
 * - 6등: 끝 2자리 일치 (5천원)
 * - 7등: 보너스 번호 일치 (1천원)
 */
data class PensionLotteryDrawResult(
    val id: Long? = null,
    val drawNumber: Int, // 회차
    val winningGroup: Int, // 당첨 조 (1~5)
    val winningNumber: Int, // 당첨 6자리 번호
    val bonusNumbers: List<Int>, // 7등 보너스 번호들 (여러 개)
    val drawDate: LocalDateTime,
    val firstPrizeWinnerCount: Int = 0, // 1등 당첨자 수
    val totalSalesAmount: Long = 0 // 총 판매액
) {
    init {
        require(winningGroup in 1..5) { "당첨 조는 1~5 범위여야 합니다" }
        require(winningNumber in 0..999999) { "당첨 번호는 0~999999 범위여야 합니다" }
        require(bonusNumbers.all { it in 0..999999 }) { "보너스 번호는 0~999999 범위여야 합니다" }
    }

    /**
     * 티켓의 당첨 등수 계산
     * @return 1~7등, null이면 미당첨
     */
    fun calculateRank(ticket: PensionLotteryTicket): Int? {
        // 7등: 보너스 번호 확인
        if (ticket.number in bonusNumbers) {
            return 7
        }

        // 당첨 조가 다르면 2등 이상 불가능 (3등 이하는 가능)
        val isGroupMatch = ticket.group == winningGroup

        // 끝자리 비교를 위해 문자열로 변환
        val ticketNumberStr = ticket.number.toString().padStart(6, '0')
        val winningNumberStr = winningNumber.toString().padStart(6, '0')

        return when {
            // 1등: 조 + 6자리 모두 일치
            isGroupMatch && ticket.number == winningNumber -> 1

            // 2등: 조는 다르지만 6자리 모두 일치
            !isGroupMatch && ticket.number == winningNumber -> 2

            // 3등 이하: 끝자리 비교
            ticketNumberStr.takeLast(5) == winningNumberStr.takeLast(5) -> 3
            ticketNumberStr.takeLast(4) == winningNumberStr.takeLast(4) -> 4
            ticketNumberStr.takeLast(3) == winningNumberStr.takeLast(3) -> 5
            ticketNumberStr.takeLast(2) == winningNumberStr.takeLast(2) -> 6

            else -> null // 미당첨
        }
    }

    /**
     * 등수별 상금 계산
     */
    fun getPrizeAmount(rank: Int): Long {
        return when (rank) {
            1 -> 7_000_000L * 12 * 20 // 월 700만원 × 12개월 × 20년 = 총 16.8억
            2 -> 100_000_000L // 1억원
            3 -> 1_000_000L // 100만원
            4 -> 100_000L // 10만원
            5 -> 50_000L // 5만원
            6 -> 5_000L // 5천원
            7 -> 1_000L // 1천원
            else -> 0L
        }
    }

    /**
     * 1등의 월 지급액
     */
    fun getMonthlyPension(rank: Int): Long? {
        return if (rank == 1) 7_000_000L else null
    }

    /**
     * 당첨 번호를 포맷팅
     */
    fun getFormattedWinningNumber(): String = "${winningGroup}조-${winningNumber.toString().padStart(6, '0')}"

    companion object {
        /**
         * 랜덤 추첨 결과 생성 (테스트용)
         */
        fun generateRandom(drawNumber: Int): PensionLotteryDrawResult {
            val winningGroup = (1..5).random()
            val winningNumber = (0..999999).random()
            val bonusNumbers = List(5) { (0..999999).random() }.distinct()

            return PensionLotteryDrawResult(
                drawNumber = drawNumber,
                winningGroup = winningGroup,
                winningNumber = winningNumber,
                bonusNumbers = bonusNumbers,
                drawDate = LocalDateTime.now(),
                firstPrizeWinnerCount = 5,
                totalSalesAmount = 50_000_000_000L // 예: 500억
            )
        }
    }
}
