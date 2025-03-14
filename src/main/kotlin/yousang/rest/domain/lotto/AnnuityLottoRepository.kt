package yousang.rest.domain.lotto

import yousang.rest.interfaces.lotto.AnnuityLottoDto

/**
 * 연금복권 정보 관련 데이터 액세스를 위한 리포지토리 인터페이스
 * 모든 메서드는 코루틴을 통한 비동기 처리를 지원합니다.
 */
interface AnnuityLottoRepository {

    /**
     * 최신 연금복권 번호 정보를 가져옵니다.
     *
     * @return 가장 최근 회차의 연금복권 정보, 없을 경우 null
     */
    suspend fun getLatestAnnuityLotto(): AnnuityLottoEntity?

    /**
     * 연금복권 번호 정보 목록을 저장합니다.
     *
     * @param annuityLottoList 저장할 연금복권 list
     */
    suspend fun save(annuityLottoList: List<AnnuityLottoEntity>)

    /**
     * 특정 범위의 연금복권 번호 정보를 회차 기준으로 조회합니다.
     * @param firstDrwNo 시작 회차 번호
     * @param lastDrwNo 끝 회차 번호
     */
    suspend fun getAnnuityLotto(firstDrwNo: Int, lastDrwNo: Int): List<AnnuityLottoEntity>
}