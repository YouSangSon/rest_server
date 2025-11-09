package yousang.rest_server.adapter.`in`.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import yousang.rest_server.application.ports.`in`.AuthenticateUserUseCase
import yousang.rest_server.application.ports.`in`.LoginCommand

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "APIs for user authentication")
class AuthController(
    private val authenticateUserUseCase: AuthenticateUserUseCase
) {

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and get JWT tokens")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val command = LoginCommand(
            username = request.username,
            password = request.password
        )
        val result = authenticateUserUseCase.authenticate(command)
        return ResponseEntity.ok(
            AuthResponse(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
                user = UserResponse.from(result.user)
            )
        )
    }
}

data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)
