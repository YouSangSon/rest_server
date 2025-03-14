package yousang.rest.adapter.out.persistence.mapper

import org.springframework.stereotype.Component
import yousang.rest.adapter.out.persistence.entity.UserEntity
import yousang.rest.domain.model.User

@Component
class UserMapper {

    fun mapToDomainEntity(userEntity: UserEntity): User {
        return User.reconstitute(
            id = userEntity.id,
            email = userEntity.email,
            name = userEntity.name,
            createdAt = userEntity.createdAt,
            updatedAt = userEntity.updatedAt
        )
    }

    fun mapToJpaEntity(user: User): UserEntity {
        return UserEntity(
            id = user.id,
            email = user.email,
            name = user.name,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
} 