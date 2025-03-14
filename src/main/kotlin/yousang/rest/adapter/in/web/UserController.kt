package yousang.rest.adapter.`in`.web

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import yousang.rest.adapter.`in`.web.dto.CreateUserRequest
import yousang.rest.adapter.`in`.web.dto.UserResponse
import yousang.rest.domain.port.`in`.CreateUserUseCase
import yousang.rest.domain.port.`in`.GetUserUseCase
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(
    private val createUserUseCase: CreateUserUseCase,
    private val getUserUseCase: GetUserUseCase
) {

    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        val user = createUserUseCase.createUser(request.toCommand())
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(UserResponse.fromDomain(user))
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val user = getUserUseCase.getUserById(id)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(UserResponse.fromDomain(user))
    }

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = getUserUseCase.getAllUsers()
        return ResponseEntity.ok(users.map { UserResponse.fromDomain(it) })
    }
} 