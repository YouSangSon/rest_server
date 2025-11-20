package yousang.rest_server.adapter.out.persistence.sns.repository

import yousang.rest_server.adapter.out.persistence.sns.document.PostDocument

/**
 * Post 커스텀 리포지토리 인터페이스
 * MongoDB Raw Query 및 Aggregation을 위한 인터페이스
 */
interface PostCustomRepository {

    /**
     * MongoDB Raw Query 실행
     */
    fun executeRawQuery(query: String): List<PostDocument>

    /**
     * Aggregation Pipeline 실행
     */
    fun executeAggregation(pipeline: List<Map<String, Any>>): List<Map<String, Any>>

    /**
     * 해시태그별 게시물 수 집계
     */
    fun countPostsByHashtag(): List<Map<String, Any>>

    /**
     * 사용자별 인기 게시물 조회 (좋아요 수 기준)
     */
    fun findTopPostsByUser(userId: Long, limit: Int): List<PostDocument>

    /**
     * 기간별 게시물 통계
     */
    fun getPostStatistics(startDate: String, endDate: String): Map<String, Any>

    /**
     * 복잡한 검색 (텍스트 + 해시태그 + 기간)
     */
    fun complexSearch(
        searchText: String?,
        hashtags: List<String>?,
        startDate: String?,
        endDate: String?,
        minLikes: Int?,
        limit: Int
    ): List<PostDocument>
}
