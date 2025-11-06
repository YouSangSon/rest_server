package yousang.rest_server.adapter.out.persistence

import org.springframework.stereotype.Component
import yousang.rest_server.adapter.out.persistence.entity.LottoTicketEntity
import yousang.rest_server.application.ports.out.LottoTicketRepositoryPort
import yousang.rest_server.domain.model.LottoTicket

/**
 * 로또 티켓 저장소 어댑터
 * Repository Port를 구현하여 도메인과 인프라를 연결
 */
@Component
class LottoTicketRepositoryAdapter(
    private val jpaRepository: LottoTicketJpaRepository
) : LottoTicketRepositoryPort {

    override fun save(ticket: LottoTicket): LottoTicket {
        val entity = LottoTicketEntity.fromDomain(ticket)
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun saveAll(tickets: List<LottoTicket>): List<LottoTicket> {
        val entities = tickets.map { LottoTicketEntity.fromDomain(it) }
        val saved = jpaRepository.saveAll(entities)
        return saved.map { it.toDomain() }
    }

    override fun findById(id: Long): LottoTicket? {
        return jpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUserId(userId: Long): List<LottoTicket> {
        return jpaRepository.findByUserId(userId)
            .map { it.toDomain() }
    }

    override fun findByUserIdAndDrawNumber(userId: Long, drawNumber: Int): List<LottoTicket> {
        return jpaRepository.findByUserIdAndDrawNumber(userId, drawNumber)
            .map { it.toDomain() }
    }

    override fun findWinningTicketsByUserId(userId: Long): List<LottoTicket> {
        return jpaRepository.findWinningTicketsByUserId(userId)
            .map { it.toDomain() }
    }

    override fun findByDrawNumber(drawNumber: Int): List<LottoTicket> {
        return jpaRepository.findByDrawNumber(drawNumber)
            .map { it.toDomain() }
    }

    override fun delete(id: Long) {
        jpaRepository.deleteById(id)
    }
}
