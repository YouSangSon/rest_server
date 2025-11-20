package yousang.rest_server.adapter.out.persistence.sns

import org.springframework.context.annotation.Primary
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import yousang.rest_server.adapter.out.persistence.sns.entity.SnsUserEntity
import yousang.rest_server.adapter.out.persistence.sns.repository.SnsUserJpaRepository
import yousang.rest_server.application.ports.out.SnsUserRepositoryPort
import yousang.rest_server.domain.sns.SnsUser

/**
 * SNS 사용자 Repository Adapter (Direct PostgreSQL via Spring Data JPA)
 */
@Component
@Primary
class SnsUserRepositoryAdapter(
    private val repository: SnsUserJpaRepository
) : SnsUserRepositoryPort {

    override fun save(user: SnsUser): SnsUser {
        val entity = SnsUserEntity.from(user)
        val saved = repository.save(entity)
        return saved.toDomain()
    }

    override fun findById(userId: Long): SnsUser? {
        return repository.findById(userId)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByEmail(email: String): SnsUser? {
        return repository.findByEmail(email)?.toDomain()
    }

    override fun findByUsername(username: String): SnsUser? {
        return repository.findByUsername(username)?.toDomain()
    }

    override fun existsByEmail(email: String): Boolean {
        return repository.existsByEmail(email)
    }

    override fun existsByUsername(username: String): Boolean {
        return repository.existsByUsername(username)
    }

    override fun delete(userId: Long) {
        repository.deleteById(userId)
    }

    override fun search(query: String, limit: Int, offset: Int): List<SnsUser> {
        val pageable = PageRequest.of(offset / limit, limit)
        return repository.searchUsers(query, pageable)
            .map { it.toDomain() }
    }

    override fun findAll(limit: Int, offset: Int): List<SnsUser> {
        val pageable = PageRequest.of(offset / limit, limit)
        return repository.findAll(pageable)
            .content
            .map { it.toDomain() }
    }
}
