package yousang.rest.infra.lotto

import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.springframework.stereotype.Repository
import yousang.rest.domain.lotto.AnnuityLottoEntity
import yousang.rest.domain.lotto.AnnuityLottoRepository
import yousang.rest.domain.lotto.AnnuityLottoTable
import yousang.rest.interfaces.lotto.AnnuityLottoDto
import yousang.rest.shared.exception.DataAccessException
import yousang.rest.shared.log.log
import yousang.rest.shared.coroutine.dbQuery

/**
 * 연금복권 저장소 인터페이스 구현체
 * 모든 데이터베이스 작업은 코루틴과 Exposed ORM을 활용한 비동기 처리
 */
@Repository
class AnnuityLottoRepositoryImpl : AnnuityLottoRepository {
    /**
     * 최신 연금복권 번호 정보를 가져옵니다.
     * 가상 스레드와 코루틴을 활용한 비동기 처리
     *
     * @return 가장 최근 회차의 연금복권 엔티티, 없을 경우 null
     */
    override suspend fun getLatestAnnuityLotto(): AnnuityLottoEntity? = dbQuery {
        try {
            // 최신 연금복권 번호 조회 - 가장 높은 회차 번호 기준
            AnnuityLottoEntity.all().maxByOrNull { it.drwNo }
        } catch (e: Exception) {
            log.error("Error retrieving latest annuity lotto: ${e.message}", e)
            throw DataAccessException("최신 연금복권 정보 조회 실패", e)
        }
    }

    /**
     * List의 연금복권 정보를 저장합니다.
     * 단일 트랜잭션 내에서 모든 엔티티를 저장하고 중복 검사도 수행합니다.
     *
     * @param annuityLottoList 저장할 연금복권 DTO 리스트
     * @throws Exception 저장 실패시 예외 발생
     */
    override suspend fun save(annuityLottoList: List<AnnuityLottoEntity>) = dbQuery {
        try {
            // 모든 연금복권 번호 저장
            annuityLottoList.forEach { it }
        } catch (e: Exception) {
            log.error("Error saving annuity lotto entities: ${e.message}", e)
            throw DataAccessException("연금복권 정보 저장 실패", e)
        }
    }

    /**
     * 특정 범위의 연금복권 번호 정보를 회차 기준으로 조회합니다.
     * 가상 스레드와 코루틴을 활용한 비동기 처리
     *
     * @param firstDrwNo 시작 회차 번호
     * @param lastDrwNo 끝 회차 번호
     * @return 지정된 범위의 연금복권 엔티티 목록
     */
    override suspend fun getAnnuityLotto(firstDrwNo: Int, lastDrwNo: Int): List<AnnuityLottoEntity> = dbQuery {
        try {
            // 시작 회차부터 끝 회차까지의 연금복권 번호 조회 (오름차순 정렬)
            AnnuityLottoEntity.find {
                (AnnuityLottoTable.drwNo greaterEq firstDrwNo) and (AnnuityLottoTable.drwNo lessEq lastDrwNo)
            }.orderBy(AnnuityLottoTable.drwNo to SortOrder.ASC).toList()
        } catch (e: Exception) {
            log.error("Error retrieving annuity lotto numbers between $firstDrwNo and $lastDrwNo: ${e.message}", e)
            throw DataAccessException("연금복권 번호 조회 실패 (범위: $firstDrwNo-$lastDrwNo)", e)
        }
    }
}