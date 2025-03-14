package yousang.rest.adapter.out.persistence

import org.springframework.stereotype.Component
import yousang.rest.adapter.out.persistence.mapper.UserMapper
import yousang.rest.adapter.out.persistence.repository.UserJpaRepository
import yousang.rest.domain.model.User
import yousang.rest.domain.port.out.UserRepository
import java.util.UUID

@Component
class UserPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository,
    private val userMapper: UserMapper
) : UserRepository {

    override fun findById(id: UUID): User? {
        return userJpaRepository.findById(id)
            .map { userMapper.mapToDomainEntity(it) }
            .orElse(null)
    }

    override fun findByEmail(email: String): User? {
        return userJpaRepository.findByEmail(email)?.let {
            userMapper.mapToDomainEntity(it)
        }
    }

    override fun save(user: User): User {
        val userEntity = userMapper.mapToJpaEntity(user)
        val savedEntity = userJpaRepository.save(userEntity)
        return userMapper.mapToDomainEntity(savedEntity)
    }

    override fun findAll(): List<User> {
        return userJpaRepository.findAll()
            .map { userMapper.mapToDomainEntity(it) }
    }

    override fun deleteById(id: UUID) {
        userJpaRepository.deleteById(id)
    }
} 