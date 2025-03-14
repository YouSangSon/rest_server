package yousang.rest.adapter.out.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    val id: UUID,
    
    val email: String,
    
    val name: String,
    
    val createdAt: LocalDateTime,
    
    val updatedAt: LocalDateTime
) 