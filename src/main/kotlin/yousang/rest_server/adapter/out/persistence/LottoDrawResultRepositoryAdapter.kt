package yousang.rest_server.adapter.out.persistence

import org.springframework.stereotype.Component
import yousang.rest_server.adapter.out.persistence.entity.LottoDrawResultEntity
import yousang.rest_server.application.ports.out.LottoDrawResultRepositoryPort
import yousang.rest_server.domain.model.LottoDrawResult

/**
 * 로또 추첨 결과 저장소 어댑터
 * Repository Port를 구현하여 도메인과 인프라를 연결
 */
@Component
class LottoDrawResultRepositoryAdapter(
    private val jpaRepository: LottoDrawResultJpaRepository
) : LottoDrawResultRepositoryPort {

    override fun save(result: LottoDrawResult): LottoDrawResult {
        val entity = LottoDrawResultEntity.fromDomain(result)
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findByDrawNumber(drawNumber: Int): LottoDrawResult? {
        return jpaRepository.findByDrawNumber(drawNumber)?.toDomain()
    }

    override fun findLatest(): LottoDrawResult? {
        return jpaRepository.findLatest()?.toDomain()
    }

    override fun findRecent(limit: Int): List<LottoDrawResult> {
        return jpaRepository.findRecent(limit)
            .map { it.toDomain() }
    }

    override fun delete(drawNumber: Int) {
        jpaRepository.deleteByDrawNumber(drawNumber)
    }
}
