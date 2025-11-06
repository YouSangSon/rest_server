package yousang.rest_server.application.ports.`in`

import yousang.rest_server.domain.model.LottoTicket

/**
 * 로또 번호 생성 Use Case
 */
interface GenerateLottoNumbersUseCase {
    /**
     * 자동 로또 번호 생성
     * @param userId 사용자 ID
     * @param drawNumber 회차
     * @param count 생성할 게임 수 (기본 5게임)
     * @return 생성된 로또 티켓 목록
     */
    fun generateAutoNumbers(userId: Long, drawNumber: Int, count: Int = 5): List<LottoTicket>

    /**
     * 수동 로또 번호 생성
     * @param userId 사용자 ID
     * @param drawNumber 회차
     * @param numbersList 각 게임의 번호 목록
     * @return 생성된 로또 티켓 목록
     */
    fun generateManualNumbers(userId: Long, drawNumber: Int, numbersList: List<List<Int>>): List<LottoTicket>
}
