package yousang.rest_server.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import yousang.rest_server.adapter.out.persistence.entity.LottoDrawResultEntity

/**
 * 로또 추첨 결과 Spring Data JPA Repository
 */
@Repository
interface LottoDrawResultJpaRepository : JpaRepository<LottoDrawResultEntity, Long> {
    fun findByDrawNumber(drawNumber: Int): LottoDrawResultEntity?

    @Query("SELECT r FROM LottoDrawResultEntity r ORDER BY r.drawNumber DESC LIMIT 1")
    fun findLatest(): LottoDrawResultEntity?

    @Query("SELECT r FROM LottoDrawResultEntity r ORDER BY r.drawNumber DESC LIMIT :limit")
    fun findRecent(limit: Int): List<LottoDrawResultEntity>

    fun deleteByDrawNumber(drawNumber: Int)
}
