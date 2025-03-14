package yousang.rest.domain.port.`in`

import yousang.rest.domain.model.User

/**
 * Input port for creating User.
 * This interface is implemented by application services and called by adapters.
 */
interface CreateUserUseCase {
    fun createUser(command: CreateUserCommand): User

    data class CreateUserCommand(
        val email: String,
        val name: String
    )
} 