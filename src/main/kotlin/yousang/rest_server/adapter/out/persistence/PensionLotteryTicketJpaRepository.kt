package yousang.rest_server.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import yousang.rest_server.adapter.out.persistence.entity.PensionLotteryTicketEntity

/**
 * 연금복권 티켓 Spring Data JPA Repository
 */
@Repository
interface PensionLotteryTicketJpaRepository : JpaRepository<PensionLotteryTicketEntity, Long> {
    fun findByUserId(userId: Long): List<PensionLotteryTicketEntity>

    fun findByUserIdAndDrawNumber(userId: Long, drawNumber: Int): List<PensionLotteryTicketEntity>

    @Query("SELECT t FROM PensionLotteryTicketEntity t WHERE t.userId = :userId AND t.isWinningChecked = true AND t.winningRank IS NOT NULL")
    fun findWinningTicketsByUserId(userId: Long): List<PensionLotteryTicketEntity>

    fun findByDrawNumber(drawNumber: Int): List<PensionLotteryTicketEntity>
}
