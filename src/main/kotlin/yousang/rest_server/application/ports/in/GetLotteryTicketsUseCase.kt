package yousang.rest_server.application.ports.`in`

import yousang.rest_server.domain.model.LottoTicket
import yousang.rest_server.domain.model.PensionLotteryTicket

/**
 * 복권 티켓 조회 Use Case
 */
interface GetLotteryTicketsUseCase {
    /**
     * 로또 티켓 조회
     * @param ticketId 티켓 ID
     * @return 로또 티켓
     */
    fun getLottoTicket(ticketId: Long): LottoTicket

    /**
     * 연금복권 티켓 조회
     * @param ticketId 티켓 ID
     * @return 연금복권 티켓
     */
    fun getPensionLotteryTicket(ticketId: Long): PensionLotteryTicket

    /**
     * 사용자의 로또 티켓 목록 조회
     * @param userId 사용자 ID
     * @param drawNumber 회차 (null이면 전체)
     * @return 로또 티켓 목록
     */
    fun getUserLottoTickets(userId: Long, drawNumber: Int? = null): List<LottoTicket>

    /**
     * 사용자의 연금복권 티켓 목록 조회
     * @param userId 사용자 ID
     * @param drawNumber 회차 (null이면 전체)
     * @return 연금복권 티켓 목록
     */
    fun getUserPensionLotteryTickets(userId: Long, drawNumber: Int? = null): List<PensionLotteryTicket>

    /**
     * 사용자의 당첨 티켓만 조회 (로또)
     * @param userId 사용자 ID
     * @return 당첨된 로또 티켓 목록
     */
    fun getUserWinningLottoTickets(userId: Long): List<LottoTicket>

    /**
     * 사용자의 당첨 티켓만 조회 (연금복권)
     * @param userId 사용자 ID
     * @return 당첨된 연금복권 티켓 목록
     */
    fun getUserWinningPensionLotteryTickets(userId: Long): List<PensionLotteryTicket>
}
