package yousang.rest.domain.user

interface UserRepository {
    suspend fun findById(id: Long): UserEntity?
    suspend fun findByEmail(email: String): UserEntity?
    suspend fun findByProviderAndProviderId(provider: String, providerId: String): UserEntity?
    suspend fun save(user: UserEntity): UserEntity
    suspend fun update(user: UserEntity): UserEntity
    suspend fun delete(id: Long): Boolean
    suspend fun existsByEmail(email: String): Boolean
    suspend fun existsByProviderAndProviderId(provider: String, providerId: String): Boolean
}
