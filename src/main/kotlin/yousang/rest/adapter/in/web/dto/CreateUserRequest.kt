package yousang.rest.adapter.`in`.web.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import yousang.rest.domain.port.`in`.CreateUserUseCase

data class CreateUserRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    val email: String,
    
    @field:NotBlank(message = "Name is required")
    val name: String
) {
    fun toCommand(): CreateUserUseCase.CreateUserCommand {
        return CreateUserUseCase.CreateUserCommand(
            email = email,
            name = name
        )
    }
} 