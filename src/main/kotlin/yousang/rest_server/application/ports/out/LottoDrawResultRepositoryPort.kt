package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.model.LottoDrawResult

/**
 * 로또 추첨 결과 저장소 포트
 */
interface LottoDrawResultRepositoryPort {
    /**
     * 로또 추첨 결과 저장
     */
    fun save(result: LottoDrawResult): LottoDrawResult

    /**
     * 회차로 로또 추첨 결과 조회
     */
    fun findByDrawNumber(drawNumber: Int): LottoDrawResult?

    /**
     * 최신 로또 추첨 결과 조회
     */
    fun findLatest(): LottoDrawResult?

    /**
     * 로또 추첨 결과 목록 조회 (최신순)
     */
    fun findRecent(limit: Int): List<LottoDrawResult>

    /**
     * 로또 추첨 결과 삭제
     */
    fun delete(drawNumber: Int)
}
