package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.model.PensionLotteryDrawResult

/**
 * 연금복권 추첨 결과 저장소 포트
 */
interface PensionLotteryDrawResultRepositoryPort {
    /**
     * 연금복권 추첨 결과 저장
     */
    fun save(result: PensionLotteryDrawResult): PensionLotteryDrawResult

    /**
     * 회차로 연금복권 추첨 결과 조회
     */
    fun findByDrawNumber(drawNumber: Int): PensionLotteryDrawResult?

    /**
     * 최신 연금복권 추첨 결과 조회
     */
    fun findLatest(): PensionLotteryDrawResult?

    /**
     * 연금복권 추첨 결과 목록 조회 (최신순)
     */
    fun findRecent(limit: Int): List<PensionLotteryDrawResult>

    /**
     * 연금복권 추첨 결과 삭제
     */
    fun delete(drawNumber: Int)
}
