package yousang.rest.infra.user

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import yousang.rest.domain.user.UserEntity
import yousang.rest.domain.user.UserRepository
import yousang.rest.domain.user.UserTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

@Repository
class UserRepositoryImpl : UserRepository {
    
    override suspend fun findById(id: Long): UserEntity? {
        return transaction {
            UserEntity.findById(id)
        }
    }
    
    override suspend fun findByEmail(email: String): UserEntity? {
        return transaction {
            UserEntity.find { UserTable.email eq email }.firstOrNull()
        }
    }
    
    override suspend fun findByProviderAndProviderId(provider: String, providerId: String): UserEntity? {
        return transaction {
            UserEntity.find { 
                (UserTable.provider eq provider) and (UserTable.providerId eq providerId) 
            }.firstOrNull()
        }
    }
    
    override suspend fun save(user: UserEntity): UserEntity {
        return transaction {
            user.updatedAt = LocalDateTime.now()
            user.flush()
            user
        }
    }
    
    override suspend fun update(user: UserEntity): UserEntity {
        return transaction {
            user.updatedAt = LocalDateTime.now()
            user.flush()
            user
        }
    }
    
    override suspend fun delete(id: Long): Boolean {
        return transaction {
            val user = UserEntity.findById(id)
            user?.delete() != null
        }
    }
    
    override suspend fun existsByEmail(email: String): Boolean {
        return transaction {
            UserEntity.count { UserTable.email eq email } > 0
        }
    }
    
    override suspend fun existsByProviderAndProviderId(provider: String, providerId: String): Boolean {
        return transaction {
            UserEntity.count { 
                (UserTable.provider eq provider) and (UserTable.providerId eq providerId) 
            } > 0
        }
    }
}
