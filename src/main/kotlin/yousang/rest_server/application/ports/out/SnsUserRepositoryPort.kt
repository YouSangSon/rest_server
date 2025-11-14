package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.sns.SnsUser

/**
 * SNS 사용자 저장소 포트
 */
interface SnsUserRepositoryPort {
    /**
     * 사용자 저장
     */
    fun save(user: SnsUser): SnsUser

    /**
     * 사용자 ID로 조회
     */
    fun findById(userId: Long): SnsUser?

    /**
     * 이메일로 조회
     */
    fun findByEmail(email: String): SnsUser?

    /**
     * 사용자명으로 조회
     */
    fun findByUsername(username: String): SnsUser?

    /**
     * 사용자 검색
     */
    fun search(query: String, limit: Int = 20, offset: Int = 0): List<SnsUser>

    /**
     * 여러 사용자 ID로 조회
     */
    fun findByIds(userIds: List<Long>): List<SnsUser>

    /**
     * 사용자 삭제
     */
    fun delete(userId: Long): Boolean

    /**
     * 이메일 존재 여부 확인
     */
    fun existsByEmail(email: String): Boolean

    /**
     * 사용자명 존재 여부 확인
     */
    fun existsByUsername(username: String): Boolean
}
