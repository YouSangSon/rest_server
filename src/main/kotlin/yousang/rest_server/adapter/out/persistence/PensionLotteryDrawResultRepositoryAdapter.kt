package yousang.rest_server.adapter.out.persistence

import org.springframework.stereotype.Component
import yousang.rest_server.adapter.out.persistence.entity.PensionLotteryDrawResultEntity
import yousang.rest_server.application.ports.out.PensionLotteryDrawResultRepositoryPort
import yousang.rest_server.domain.model.PensionLotteryDrawResult

/**
 * 연금복권 추첨 결과 저장소 어댑터
 * Repository Port를 구현하여 도메인과 인프라를 연결
 */
@Component
class PensionLotteryDrawResultRepositoryAdapter(
    private val jpaRepository: PensionLotteryDrawResultJpaRepository
) : PensionLotteryDrawResultRepositoryPort {

    override fun save(result: PensionLotteryDrawResult): PensionLotteryDrawResult {
        val entity = PensionLotteryDrawResultEntity.fromDomain(result)
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findByDrawNumber(drawNumber: Int): PensionLotteryDrawResult? {
        return jpaRepository.findByDrawNumber(drawNumber)?.toDomain()
    }

    override fun findLatest(): PensionLotteryDrawResult? {
        return jpaRepository.findLatest()?.toDomain()
    }

    override fun findRecent(limit: Int): List<PensionLotteryDrawResult> {
        return jpaRepository.findRecent(limit)
            .map { it.toDomain() }
    }

    override fun delete(drawNumber: Int) {
        jpaRepository.deleteByDrawNumber(drawNumber)
    }
}
