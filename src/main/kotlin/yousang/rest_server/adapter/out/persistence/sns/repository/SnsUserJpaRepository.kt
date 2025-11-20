package yousang.rest_server.adapter.out.persistence.sns.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import yousang.rest_server.adapter.out.persistence.sns.entity.SnsUserEntity

@Repository
interface SnsUserJpaRepository : JpaRepository<SnsUserEntity, Long>, SnsUserCustomRepository {
    fun findByEmail(email: String): SnsUserEntity?
    fun findByUsername(username: String): SnsUserEntity?
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean

    @Query(
        """
        SELECT u FROM SnsUserEntity u
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
        AND u.isActive = true
        ORDER BY u.followerCount DESC
        """
    )
    fun searchUsers(query: String, pageable: Pageable): List<SnsUserEntity>
}
