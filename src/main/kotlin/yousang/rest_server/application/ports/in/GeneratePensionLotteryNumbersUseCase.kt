package yousang.rest_server.application.ports.`in`

import yousang.rest_server.domain.model.PensionLotteryTicket

/**
 * 연금복권 번호 생성 Use Case
 */
interface GeneratePensionLotteryNumbersUseCase {
    /**
     * 자동 연금복권 번호 생성
     * @param userId 사용자 ID
     * @param drawNumber 회차
     * @param count 생성할 게임 수 (기본 5게임)
     * @return 생성된 연금복권 티켓 목록
     */
    fun generateAutoNumbers(userId: Long, drawNumber: Int, count: Int = 5): List<PensionLotteryTicket>

    /**
     * 수동 연금복권 번호 생성
     * @param userId 사용자 ID
     * @param drawNumber 회차
     * @param tickets 각 게임의 조와 번호 목록
     * @return 생성된 연금복권 티켓 목록
     */
    fun generateManualNumbers(
        userId: Long,
        drawNumber: Int,
        tickets: List<Pair<Int, Int>> // (조, 번호)
    ): List<PensionLotteryTicket>
}
