package yousang.rest_server.adapter.out.persistence.sns.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.jdbc.core.JdbcTemplate
import yousang.rest_server.adapter.out.persistence.sns.entity.SnsUserEntity
import java.sql.ResultSet

/**
 * SNS 사용자 커스텀 리포지토리 구현
 * JdbcTemplate과 EntityManager를 사용한 Raw Query 실행
 *
 * Spring Data JPA는 이 클래스를 자동으로 발견합니다.
 * 네이밍 규칙: {RepositoryName}Impl
 * @Repository 어노테이션 불필요 - Spring Data가 자동으로 빈 생성
 */
class SnsUserJpaRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : SnsUserCustomRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun executeRawQuery(sql: String, params: Map<String, Any>): List<Map<String, Any>> {
        // Named parameters를 위한 간단한 구현
        var processedSql = sql
        val orderedParams = mutableListOf<Any>()

        params.forEach { (key, value) ->
            if (processedSql.contains(":$key")) {
                processedSql = processedSql.replace(":$key", "?")
                orderedParams.add(value)
            }
        }

        return jdbcTemplate.query(processedSql, orderedParams.toTypedArray()) { rs, _ ->
            val metaData = rs.metaData
            val columnCount = metaData.columnCount
            val row = mutableMapOf<String, Any>()

            for (i in 1..columnCount) {
                val columnName = metaData.getColumnName(i)
                val value = rs.getObject(i)
                if (value != null) {
                    row[columnName] = value
                }
            }
            row
        }
    }

    override fun searchUsersWithNativeQuery(searchTerm: String, limit: Int): List<SnsUserEntity> {
        val sql = """
            SELECT * FROM sns_users
            WHERE is_active = true
            AND (
                LOWER(username) LIKE LOWER(?)
                OR LOWER(full_name) LIKE LOWER(?)
                OR LOWER(email) LIKE LOWER(?)
            )
            ORDER BY follower_count DESC, created_at DESC
            LIMIT ?
        """.trimIndent()

        val pattern = "%$searchTerm%"

        return jdbcTemplate.query(sql, { rs, _ -> mapResultSetToEntity(rs) }, pattern, pattern, pattern, limit)
    }

    override fun findTopUsersByFollowers(limit: Int): List<SnsUserEntity> {
        val sql = """
            SELECT * FROM sns_users
            WHERE is_active = true
            AND is_verified = true
            ORDER BY follower_count DESC, post_count DESC
            LIMIT ?
        """.trimIndent()

        return jdbcTemplate.query(sql, { rs, _ -> mapResultSetToEntity(rs) }, limit)
    }

    override fun getUserStatistics(userId: Long): Map<String, Any> {
        val sql = """
            SELECT
                u.user_id,
                u.username,
                u.follower_count,
                u.following_count,
                u.post_count,
                COUNT(DISTINCT f1.follow_id) as actual_follower_count,
                COUNT(DISTINCT f2.follow_id) as actual_following_count,
                u.created_at,
                EXTRACT(DAY FROM (NOW() - u.created_at)) as account_age_days
            FROM sns_users u
            LEFT JOIN sns_follows f1 ON f1.following_id = u.user_id
            LEFT JOIN sns_follows f2 ON f2.follower_id = u.user_id
            WHERE u.user_id = ?
            GROUP BY u.user_id
        """.trimIndent()

        return jdbcTemplate.queryForMap(sql, userId)
    }

    override fun batchInsertUsers(users: List<SnsUserEntity>): Int {
        val sql = """
            INSERT INTO sns_users (
                email, username, full_name, bio, profile_image_url,
                follower_count, following_count, post_count,
                is_verified, is_active, email_verified, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val batchArgs = users.map { user ->
            arrayOf(
                user.email,
                user.username,
                user.fullName,
                user.bio,
                user.profileImageUrl,
                user.followerCount,
                user.followingCount,
                user.postCount,
                user.isVerified,
                user.isActive,
                user.emailVerified,
                user.createdAt,
                user.updatedAt
            )
        }

        val results = jdbcTemplate.batchUpdate(sql, batchArgs)
        return results.sum()
    }

    private fun mapResultSetToEntity(rs: ResultSet): SnsUserEntity {
        return SnsUserEntity(
            userId = rs.getLong("user_id"),
            email = rs.getString("email"),
            username = rs.getString("username"),
            fullName = rs.getString("full_name"),
            bio = rs.getString("bio"),
            profileImageUrl = rs.getString("profile_image_url"),
            followerCount = rs.getInt("follower_count"),
            followingCount = rs.getInt("following_count"),
            postCount = rs.getInt("post_count"),
            isVerified = rs.getBoolean("is_verified"),
            isActive = rs.getBoolean("is_active"),
            emailVerified = rs.getBoolean("email_verified"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime()
        )
    }
}
