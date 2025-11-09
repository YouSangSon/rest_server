package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.model.User

interface UserRepositoryPort {
    fun save(user: User): User
    fun findById(id: Long): User?
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun findAll(): List<User>
    fun delete(id: Long)
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}
