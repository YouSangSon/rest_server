package yousang.rest_server.adapter.out.persistence

import org.springframework.stereotype.Component
import yousang.rest_server.adapter.out.persistence.entity.PensionLotteryTicketEntity
import yousang.rest_server.application.ports.out.PensionLotteryTicketRepositoryPort
import yousang.rest_server.domain.model.PensionLotteryTicket

/**
 * 연금복권 티켓 저장소 어댑터
 * Repository Port를 구현하여 도메인과 인프라를 연결
 */
@Component
class PensionLotteryTicketRepositoryAdapter(
    private val jpaRepository: PensionLotteryTicketJpaRepository
) : PensionLotteryTicketRepositoryPort {

    override fun save(ticket: PensionLotteryTicket): PensionLotteryTicket {
        val entity = PensionLotteryTicketEntity.fromDomain(ticket)
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun saveAll(tickets: List<PensionLotteryTicket>): List<PensionLotteryTicket> {
        val entities = tickets.map { PensionLotteryTicketEntity.fromDomain(it) }
        val saved = jpaRepository.saveAll(entities)
        return saved.map { it.toDomain() }
    }

    override fun findById(id: Long): PensionLotteryTicket? {
        return jpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUserId(userId: Long): List<PensionLotteryTicket> {
        return jpaRepository.findByUserId(userId)
            .map { it.toDomain() }
    }

    override fun findByUserIdAndDrawNumber(userId: Long, drawNumber: Int): List<PensionLotteryTicket> {
        return jpaRepository.findByUserIdAndDrawNumber(userId, drawNumber)
            .map { it.toDomain() }
    }

    override fun findWinningTicketsByUserId(userId: Long): List<PensionLotteryTicket> {
        return jpaRepository.findWinningTicketsByUserId(userId)
            .map { it.toDomain() }
    }

    override fun findByDrawNumber(drawNumber: Int): List<PensionLotteryTicket> {
        return jpaRepository.findByDrawNumber(drawNumber)
            .map { it.toDomain() }
    }

    override fun delete(id: Long) {
        jpaRepository.deleteById(id)
    }
}
