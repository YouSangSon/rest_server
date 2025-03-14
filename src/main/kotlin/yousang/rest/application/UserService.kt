package yousang.rest.application

import org.springframework.stereotype.Service
import yousang.rest.domain.model.User
import yousang.rest.domain.port.`in`.CreateUserUseCase
import yousang.rest.domain.port.`in`.GetUserUseCase
import yousang.rest.domain.port.out.UserRepository
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) : CreateUserUseCase, GetUserUseCase {

    override fun createUser(command: CreateUserUseCase.CreateUserCommand): User {
        val existingUser = userRepository.findByEmail(command.email)
        if (existingUser != null) {
            throw IllegalArgumentException("User with email ${command.email} already exists")
        }
        
        val newUser = User.create(
            email = command.email,
            name = command.name
        )
        
        return userRepository.save(newUser)
    }

    override fun getUserById(id: UUID): User? {
        return userRepository.findById(id)
    }

    override fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
} 