package yousang.rest_server.domain.model

import java.time.LocalDateTime

/**
 * User Domain Model
 * Pure domain object with no framework dependencies
 */
data class User(
    val id: Long? = null,
    val username: String,
    val email: String,
    val password: String,
    val roles: Set<Role> = setOf(Role.USER),
    val enabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(username.length >= 3) { "Username must be at least 3 characters" }
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(email.contains("@")) { "Email must be valid" }
        require(password.isNotBlank()) { "Password cannot be blank" }
    }

    fun withEncodedPassword(encodedPassword: String): User {
        return copy(password = encodedPassword)
    }

    fun disable(): User {
        return copy(enabled = false, updatedAt = LocalDateTime.now())
    }

    fun updateProfile(username: String? = null, email: String? = null): User {
        return copy(
            username = username ?: this.username,
            email = email ?: this.email,
            updatedAt = LocalDateTime.now()
        )
    }
}

enum class Role {
    USER,
    ADMIN,
    MODERATOR
}
