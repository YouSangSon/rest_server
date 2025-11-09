package yousang.rest_server.adapter.`in`.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import yousang.rest_server.application.ports.`in`.*
import yousang.rest_server.domain.model.Role
import yousang.rest_server.domain.model.User
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "APIs for user management")
class UserController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) {

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account")
    fun register(@Valid @RequestBody request: RegisterUserRequest): ResponseEntity<UserResponse> {
        val command = RegisterUserCommand(
            username = request.username,
            email = request.email,
            password = request.password
        )
        val user = registerUserUseCase.register(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user))
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get user by ID", description = "Retrieve user information by ID")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = getUserUseCase.getUserById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(UserResponse.from(user))
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all users", description = "Retrieve all users (Admin only)")
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = getUserUseCase.getAllUsers()
        return ResponseEntity.ok(users.map { UserResponse.from(it) })
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update user", description = "Update user profile information")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val command = UpdateUserCommand(
            username = request.username,
            email = request.email
        )
        val user = updateUserUseCase.updateUser(id, command)
        return ResponseEntity.ok(UserResponse.from(user))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete user", description = "Delete a user account (Admin only)")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        deleteUserUseCase.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
}

data class RegisterUserRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String
)

data class UpdateUserRequest(
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String?,

    @field:Email(message = "Email must be valid")
    val email: String?
)

data class UserResponse(
    val id: Long?,
    val username: String,
    val email: String,
    val roles: Set<Role>,
    val enabled: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                username = user.username,
                email = user.email,
                roles = user.roles,
                enabled = user.enabled,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}
