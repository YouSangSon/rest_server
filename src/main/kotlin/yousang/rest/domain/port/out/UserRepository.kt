package yousang.rest.domain.port.out

import yousang.rest.domain.model.User
import java.util.UUID

/**
 * Output port for User persistence operations.
 * This interface is implemented by adapters and called by the application layer.
 */
interface UserRepository {
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun save(user: User): User
    fun findAll(): List<User>
    fun deleteById(id: UUID)
} 