package yousang.rest.domain.user

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.time.LocalDateTime

object UserTable : LongIdTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 100)
    val password = varchar("password", 255).nullable()
    val provider = varchar("provider", 20).default("local") // local, google, github, kakao
    val providerId = varchar("provider_id", 255).nullable()
    val profileImage = varchar("profile_image", 500).nullable()
    val isEnabled = bool("is_enabled").default(true)
    val isAccountNonExpired = bool("is_account_non_expired").default(true)
    val isAccountNonLocked = bool("is_account_non_locked").default(true)
    val isCredentialsNonExpired = bool("is_credentials_non_expired").default(true)
    val lastLoginAt = timestamp("last_login_at").nullable()
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    var email by UserTable.email
    var username by UserTable.username
    var password by UserTable.password
    var provider by UserTable.provider
    var providerId by UserTable.providerId
    var profileImage by UserTable.profileImage
    var isEnabled by UserTable.isEnabled
    var isAccountNonExpired by UserTable.isAccountNonExpired
    var isAccountNonLocked by UserTable.isAccountNonLocked
    var isCredentialsNonExpired by UserTable.isCredentialsNonExpired
    var lastLoginAt by UserTable.lastLoginAt
    var createdAt by UserTable.createdAt
    var updatedAt by UserTable.updatedAt

    companion object : LongEntityClass<UserEntity>(UserTable) {
        fun createOAuth2User(
            email: String,
            username: String,
            provider: String,
            providerId: String,
            profileImage: String? = null
        ): UserEntity {
            return new {
                this.email = email
                this.username = username
                this.provider = provider
                this.providerId = providerId
                this.profileImage = profileImage
                this.password = null // OAuth2 사용자는 비밀번호가 없음
            }
        }
    }
}
