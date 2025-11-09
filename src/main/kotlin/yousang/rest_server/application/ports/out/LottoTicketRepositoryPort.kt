package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.model.LottoTicket

/**
 * 로또 티켓 저장소 포트
 */
interface LottoTicketRepositoryPort {
    /**
     * 로또 티켓 저장
     */
    fun save(ticket: LottoTicket): LottoTicket

    /**
     * 여러 로또 티켓 저장
     */
    fun saveAll(tickets: List<LottoTicket>): List<LottoTicket>

    /**
     * ID로 로또 티켓 조회
     */
    fun findById(id: Long): LottoTicket?

    /**
     * 사용자의 로또 티켓 목록 조회
     */
    fun findByUserId(userId: Long): List<LottoTicket>

    /**
     * 사용자의 특정 회차 로또 티켓 목록 조회
     */
    fun findByUserIdAndDrawNumber(userId: Long, drawNumber: Int): List<LottoTicket>

    /**
     * 사용자의 당첨 티켓만 조회
     */
    fun findWinningTicketsByUserId(userId: Long): List<LottoTicket>

    /**
     * 특정 회차의 모든 티켓 조회
     */
    fun findByDrawNumber(drawNumber: Int): List<LottoTicket>

    /**
     * 로또 티켓 삭제
     */
    fun delete(id: Long)
}
