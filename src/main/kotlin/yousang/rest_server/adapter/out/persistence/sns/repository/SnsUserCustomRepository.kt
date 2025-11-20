package yousang.rest_server.adapter.out.persistence.sns.repository

import yousang.rest_server.adapter.out.persistence.sns.entity.SnsUserEntity

/**
 * SNS 사용자 커스텀 리포지토리 인터페이스
 * Raw Query 및 복잡한 쿼리를 위한 인터페이스
 */
interface SnsUserCustomRepository {

    /**
     * Raw SQL 쿼리 실행
     */
    fun executeRawQuery(sql: String, params: Map<String, Any>): List<Map<String, Any>>

    /**
     * Native Query로 사용자 검색 (LIKE 검색)
     */
    fun searchUsersWithNativeQuery(searchTerm: String, limit: Int): List<SnsUserEntity>

    /**
     * 팔로워 수 기준 상위 사용자 조회 (Raw Query)
     */
    fun findTopUsersByFollowers(limit: Int): List<SnsUserEntity>

    /**
     * 복잡한 통계 쿼리 실행
     */
    fun getUserStatistics(userId: Long): Map<String, Any>

    /**
     * Batch Insert (Raw JDBC)
     */
    fun batchInsertUsers(users: List<SnsUserEntity>): Int
}
