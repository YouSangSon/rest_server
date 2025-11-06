package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.model.PensionLotteryTicket

/**
 * 연금복권 티켓 저장소 포트
 */
interface PensionLotteryTicketRepositoryPort {
    /**
     * 연금복권 티켓 저장
     */
    fun save(ticket: PensionLotteryTicket): PensionLotteryTicket

    /**
     * 여러 연금복권 티켓 저장
     */
    fun saveAll(tickets: List<PensionLotteryTicket>): List<PensionLotteryTicket>

    /**
     * ID로 연금복권 티켓 조회
     */
    fun findById(id: Long): PensionLotteryTicket?

    /**
     * 사용자의 연금복권 티켓 목록 조회
     */
    fun findByUserId(userId: Long): List<PensionLotteryTicket>

    /**
     * 사용자의 특정 회차 연금복권 티켓 목록 조회
     */
    fun findByUserIdAndDrawNumber(userId: Long, drawNumber: Int): List<PensionLotteryTicket>

    /**
     * 사용자의 당첨 티켓만 조회
     */
    fun findWinningTicketsByUserId(userId: Long): List<PensionLotteryTicket>

    /**
     * 특정 회차의 모든 티켓 조회
     */
    fun findByDrawNumber(drawNumber: Int): List<PensionLotteryTicket>

    /**
     * 연금복권 티켓 삭제
     */
    fun delete(id: Long)
}
