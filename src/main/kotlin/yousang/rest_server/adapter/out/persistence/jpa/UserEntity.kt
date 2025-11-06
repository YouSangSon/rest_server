package yousang.rest_server.adapter.out.persistence.jpa

import jakarta.persistence.*
import yousang.rest_server.domain.model.Role
import yousang.rest_server.domain.model.User
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false, length = 50)
    var username: String,

    @Column(unique = true, nullable = false, length = 100)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    var roles: MutableSet<Role> = mutableSetOf(Role.USER),

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): User {
        return User(
            id = id,
            username = username,
            email = email,
            password = password,
            roles = roles.toSet(),
            enabled = enabled,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                username = user.username,
                email = user.email,
                password = user.password,
                roles = user.roles.toMutableSet(),
                enabled = user.enabled,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}
