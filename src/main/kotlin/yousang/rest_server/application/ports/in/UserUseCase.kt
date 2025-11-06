package yousang.rest_server.application.ports.`in`

import yousang.rest_server.domain.model.User

interface RegisterUserUseCase {
    fun register(command: RegisterUserCommand): User
}

interface GetUserUseCase {
    fun getUserById(id: Long): User?
    fun getUserByUsername(username: String): User?
    fun getUserByEmail(email: String): User?
    fun getAllUsers(): List<User>
}

interface UpdateUserUseCase {
    fun updateUser(id: Long, command: UpdateUserCommand): User
}

interface DeleteUserUseCase {
    fun deleteUser(id: Long)
}

interface AuthenticateUserUseCase {
    fun authenticate(command: LoginCommand): AuthenticationResult
}

// Commands
data class RegisterUserCommand(
    val username: String,
    val email: String,
    val password: String
)

data class UpdateUserCommand(
    val username: String?,
    val email: String?
)

data class LoginCommand(
    val username: String,
    val password: String
)

data class AuthenticationResult(
    val user: User,
    val accessToken: String,
    val refreshToken: String
)
