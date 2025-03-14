package yousang.rest.infra.lotto

import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.springframework.stereotype.Repository
import yousang.rest.domain.lotto.LottoEntity
import yousang.rest.domain.lotto.LottoTable
import yousang.rest.domain.lotto.LottoRepository
import yousang.rest.shared.exception.DataAccessException
import yousang.rest.shared.log.log
import yousang.rest.shared.coroutine.dbQuery

/**
 * 로또 저장소 인터페이스 구현체
 * 모든 데이터베이스 작업은 코루틴과 Exposed ORM을 활용한 비동기 처리
 */
@Repository
class LottoRepositoryImpl : LottoRepository {
    /**
     * 최신 로또 번호 정보를 가져옵니다.
     * 가상 스레드와 코루틴을 활용한 비동기 처리
     *
     * @return 가장 최근 회차의 로또 엔티티, 없을 경우 null
     */
    override suspend fun getLatestLotto(): LottoEntity? = dbQuery {
        try {
            // 최신 로또 번호 조회 - 가장 높은 회차 번호 기준
            LottoEntity.all().maxByOrNull { it.drwNo }
        } catch (e: Exception) {
            log.error("Error retrieving latest lotto: ${e.message}", e)
            throw DataAccessException("최신 로또 정보 조회 실패", e)
        }
    }

    /**
     * 로또 번호 정보 목록을 저장합니다.
     * 가상 스레드와 코루틴을 활용한 비동기 처리
     *
     * @param lottoList 저장할 로또 엔티티 목록
     */
    override suspend fun save(lottoList: List<LottoEntity>) = dbQuery {
        try {
            // 모든 로또 번호 저장
            lottoList.forEach { it }
        } catch (e: Exception) {
            log.error("Error saving lotto entities: ${e.message}", e)
            throw DataAccessException("로또 정보 저장 실패", e)
        }
    }

    /**
     * 특정 범위의 로또 번호 정보를 회차 기준으로 조회합니다.
     * 가상 스레드와 코루틴을 활용한 비동기 처리
     *
     * @param firstDrwNo 시작 회차 번호
     * @param lastDrwNo 끝 회차 번호
     * @return 지정된 범위의 로또 엔티티 목록
     */
    override suspend fun getLotto(firstDrwNo: Int, lastDrwNo: Int): List<LottoEntity> = dbQuery {
        try {
            // 시작 회차부터 끝 회차까지의 로또 번호 조회 (오름차순 정렬)
            LottoEntity.find {
                (LottoTable.drwNo greaterEq firstDrwNo) and (LottoTable.drwNo lessEq lastDrwNo)
            }.orderBy(LottoTable.drwNo to SortOrder.ASC).toList()
        } catch (e: Exception) {
            log.error("Error retrieving lotto numbers between $firstDrwNo and $lastDrwNo: ${e.message}", e)
            throw DataAccessException("로또 번호 조회 실패 (범위: $firstDrwNo-$lastDrwNo)", e)
        }
    }
}