package yousang.rest.domain.port.`in`

import yousang.rest.domain.model.User
import java.util.UUID

/**
 * Input port for retrieving User information.
 * This interface is implemented by application services and called by adapters.
 */
interface GetUserUseCase {
    fun getUserById(id: UUID): User?
    fun getAllUsers(): List<User>
} 