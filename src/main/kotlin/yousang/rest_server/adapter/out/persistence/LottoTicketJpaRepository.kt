package yousang.rest_server.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import yousang.rest_server.adapter.out.persistence.entity.LottoTicketEntity

/**
 * 로또 티켓 Spring Data JPA Repository
 */
@Repository
interface LottoTicketJpaRepository : JpaRepository<LottoTicketEntity, Long> {
    fun findByUserId(userId: Long): List<LottoTicketEntity>

    fun findByUserIdAndDrawNumber(userId: Long, drawNumber: Int): List<LottoTicketEntity>

    @Query("SELECT t FROM LottoTicketEntity t WHERE t.userId = :userId AND t.isWinningChecked = true AND t.winningRank IS NOT NULL")
    fun findWinningTicketsByUserId(userId: Long): List<LottoTicketEntity>

    fun findByDrawNumber(drawNumber: Int): List<LottoTicketEntity>
}
