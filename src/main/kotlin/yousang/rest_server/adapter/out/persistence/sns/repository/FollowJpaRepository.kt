package yousang.rest_server.adapter.out.persistence.sns.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import yousang.rest_server.adapter.out.persistence.sns.entity.FollowEntity

@Repository
interface FollowJpaRepository : JpaRepository<FollowEntity, Long> {
    fun findByFollowerIdAndFollowingId(followerId: Long, followingId: Long): FollowEntity?
    fun existsByFollowerIdAndFollowingId(followerId: Long, followingId: Long): Boolean
    fun deleteByFollowerIdAndFollowingId(followerId: Long, followingId: Long)

    fun findByFollowerId(followerId: Long, pageable: Pageable): List<FollowEntity>
    fun findByFollowingId(followingId: Long, pageable: Pageable): List<FollowEntity>

    fun countByFollowerId(followerId: Long): Long
    fun countByFollowingId(followingId: Long): Long
}
