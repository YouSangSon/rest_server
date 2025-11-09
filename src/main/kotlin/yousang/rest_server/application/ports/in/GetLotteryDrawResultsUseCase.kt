package yousang.rest_server.application.ports.`in`

import yousang.rest_server.domain.model.LottoDrawResult
import yousang.rest_server.domain.model.PensionLotteryDrawResult

/**
 * 복권 추첨 결과 조회 Use Case
 */
interface GetLotteryDrawResultsUseCase {
    /**
     * 로또 추첨 결과 조회
     * @param drawNumber 회차
     * @return 로또 추첨 결과
     */
    fun getLottoDrawResult(drawNumber: Int): LottoDrawResult

    /**
     * 연금복권 추첨 결과 조회
     * @param drawNumber 회차
     * @return 연금복권 추첨 결과
     */
    fun getPensionLotteryDrawResult(drawNumber: Int): PensionLotteryDrawResult

    /**
     * 최신 로또 추첨 결과 조회
     * @return 최신 로또 추첨 결과
     */
    fun getLatestLottoDrawResult(): LottoDrawResult

    /**
     * 최신 연금복권 추첨 결과 조회
     * @return 최신 연금복권 추첨 결과
     */
    fun getLatestPensionLotteryDrawResult(): PensionLotteryDrawResult

    /**
     * 로또 추첨 결과 목록 조회
     * @param limit 조회할 개수 (최신순)
     * @return 로또 추첨 결과 목록
     */
    fun getLottoDrawResults(limit: Int = 10): List<LottoDrawResult>

    /**
     * 연금복권 추첨 결과 목록 조회
     * @param limit 조회할 개수 (최신순)
     * @return 연금복권 추첨 결과 목록
     */
    fun getPensionLotteryDrawResults(limit: Int = 10): List<PensionLotteryDrawResult>
}
