package yousang.rest_server.adapter.out.persistence.jpa

import org.springframework.stereotype.Component
import yousang.rest_server.application.ports.out.UserRepositoryPort
import yousang.rest_server.domain.model.User

@Component
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserRepositoryPort {

    override fun save(user: User): User {
        val entity = UserEntity.fromDomain(user)
        val saved = userJpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: Long): User? {
        return userJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUsername(username: String): User? {
        return userJpaRepository.findByUsername(username)?.toDomain()
    }

    override fun findByEmail(email: String): User? {
        return userJpaRepository.findByEmail(email)?.toDomain()
    }

    override fun findAll(): List<User> {
        return userJpaRepository.findAll().map { it.toDomain() }
    }

    override fun delete(id: Long) {
        userJpaRepository.deleteById(id)
    }

    override fun existsByUsername(username: String): Boolean {
        return userJpaRepository.existsByUsername(username)
    }

    override fun existsByEmail(email: String): Boolean {
        return userJpaRepository.existsByEmail(email)
    }
}
