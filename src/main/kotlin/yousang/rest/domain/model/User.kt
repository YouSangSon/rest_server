package yousang.rest.domain.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * User entity represents a user in the system.
 * This is a domain entity and should not be coupled with any frameworks.
 */
class User private constructor(
    val id: UUID,
    val email: String,
    val name: String,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
) {
    companion object {
        fun create(email: String, name: String): User {
            val now = LocalDateTime.now()
            return User(
                id = UUID.randomUUID(),
                email = email,
                name = name,
                createdAt = now,
                updatedAt = now
            )
        }

        fun reconstitute(id: UUID, email: String, name: String, createdAt: LocalDateTime, updatedAt: LocalDateTime): User {
            return User(id, email, name, createdAt, updatedAt)
        }
    }

    fun updateName(newName: String): User {
        return User(
            id = this.id,
            email = this.email,
            name = newName,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }
} 