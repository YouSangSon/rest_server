package yousang.rest_server.application.ports.`in`

import yousang.rest_server.domain.model.LottoTicket
import yousang.rest_server.domain.model.PensionLotteryTicket

/**
 * 복권 당첨 확인 Use Case
 */
interface CheckLotteryWinningUseCase {
    /**
     * 로또 당첨 확인
     * @param ticketId 티켓 ID
     * @return 당첨 정보가 업데이트된 티켓
     */
    fun checkLottoWinning(ticketId: Long): LottoTicket

    /**
     * 연금복권 당첨 확인
     * @param ticketId 티켓 ID
     * @return 당첨 정보가 업데이트된 티켓
     */
    fun checkPensionLotteryWinning(ticketId: Long): PensionLotteryTicket

    /**
     * 사용자의 모든 로또 티켓 당첨 확인
     * @param userId 사용자 ID
     * @param drawNumber 회차
     * @return 당첨 정보가 업데이트된 티켓 목록
     */
    fun checkAllLottoTickets(userId: Long, drawNumber: Int): List<LottoTicket>

    /**
     * 사용자의 모든 연금복권 티켓 당첨 확인
     * @param userId 사용자 ID
     * @param drawNumber 회차
     * @return 당첨 정보가 업데이트된 티켓 목록
     */
    fun checkAllPensionLotteryTickets(userId: Long, drawNumber: Int): List<PensionLotteryTicket>
}
