package yousang.rest_server.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import yousang.rest_server.adapter.out.persistence.entity.PensionLotteryDrawResultEntity

/**
 * 연금복권 추첨 결과 Spring Data JPA Repository
 */
@Repository
interface PensionLotteryDrawResultJpaRepository : JpaRepository<PensionLotteryDrawResultEntity, Long> {
    fun findByDrawNumber(drawNumber: Int): PensionLotteryDrawResultEntity?

    @Query("SELECT r FROM PensionLotteryDrawResultEntity r ORDER BY r.drawNumber DESC LIMIT 1")
    fun findLatest(): PensionLotteryDrawResultEntity?

    @Query("SELECT r FROM PensionLotteryDrawResultEntity r ORDER BY r.drawNumber DESC LIMIT :limit")
    fun findRecent(limit: Int): List<PensionLotteryDrawResultEntity>

    fun deleteByDrawNumber(drawNumber: Int)
}
