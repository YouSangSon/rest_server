package yousang.rest_server.adapter.out.persistence.sns.repository

import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.Document
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import yousang.rest_server.adapter.out.persistence.sns.document.PostDocument
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Post 커스텀 리포지토리 구현
 * MongoTemplate을 사용한 Raw Query 및 Aggregation 실행
 *
 * Spring Data MongoDB는 이 클래스를 자동으로 발견합니다.
 * 네이밍 규칙: {RepositoryName}Impl
 */
class PostMongoRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
    private val objectMapper: ObjectMapper
) : PostCustomRepository {

    private val collectionName = "sns_posts"

    override fun executeRawQuery(query: String): List<PostDocument> {
        // JSON 문자열을 Document로 변환
        val document = Document.parse(query)
        val mongoQuery = Query()

        // Document를 Criteria로 변환
        document.forEach { key, value ->
            when (key) {
                "\$and", "\$or", "\$nor" -> {
                    // 복잡한 조건 처리
                    mongoQuery.addCriteria(Criteria.where(key).`is`(value))
                }
                else -> {
                    mongoQuery.addCriteria(Criteria.where(key).`is`(value))
                }
            }
        }

        return mongoTemplate.find(mongoQuery, PostDocument::class.java, collectionName)
    }

    override fun executeAggregation(pipeline: List<Map<String, Any>>): List<Map<String, Any>> {
        val collection = mongoTemplate.getCollection(collectionName)

        val bsonPipeline = pipeline.map { stage ->
            Document(stage)
        }

        val results = mutableListOf<Map<String, Any>>()
        collection.aggregate(bsonPipeline).forEach { doc ->
            results.add(doc.toMap())
        }

        return results
    }

    override fun countPostsByHashtag(): List<Map<String, Any>> {
        // Aggregation Pipeline: Unwind hashtags and count
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("isHidden").`is`(false)),
            Aggregation.unwind("hashtags"),
            Aggregation.group("hashtags")
                .count().`as`("count"),
            Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "count"),
            Aggregation.limit(50)
        )

        val results = mongoTemplate.aggregate(aggregation, collectionName, Map::class.java)
        return results.mappedResults
    }

    override fun findTopPostsByUser(userId: Long, limit: Int): List<PostDocument> {
        val query = Query()
        query.addCriteria(Criteria.where("userId").`is`(userId))
        query.addCriteria(Criteria.where("isHidden").`is`(false))
        query.with(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Order.desc("likeCount"),
            org.springframework.data.domain.Sort.Order.desc("commentCount")
        ))
        query.limit(limit)

        return mongoTemplate.find(query, PostDocument::class.java, collectionName)
    }

    override fun getPostStatistics(startDate: String, endDate: String): Map<String, Any> {
        val start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME)

        // Aggregation for statistics
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(
                Criteria.where("createdAt").gte(start).lte(end)
                    .and("isHidden").`is`(false)
            ),
            Aggregation.group()
                .count().`as`("totalPosts")
                .sum("likeCount").`as`("totalLikes")
                .sum("commentCount").`as`("totalComments")
                .sum("viewCount").`as`("totalViews")
                .avg("likeCount").`as`("avgLikes")
        )

        val result = mongoTemplate.aggregate(aggregation, collectionName, Map::class.java)
        return result.uniqueMappedResult ?: emptyMap()
    }

    override fun complexSearch(
        searchText: String?,
        hashtags: List<String>?,
        startDate: String?,
        endDate: String?,
        minLikes: Int?,
        limit: Int
    ): List<PostDocument> {
        val criteria = mutableListOf<Criteria>()

        // 기본 조건: 숨김 아닌 게시물
        criteria.add(Criteria.where("isHidden").`is`(false))

        // 텍스트 검색
        searchText?.let {
            if (it.isNotBlank()) {
                criteria.add(Criteria().orOperator(
                    Criteria.where("caption").regex(it, "i"),
                    Criteria.where("location").regex(it, "i")
                ))
            }
        }

        // 해시태그 검색
        hashtags?.let {
            if (it.isNotEmpty()) {
                criteria.add(Criteria.where("hashtags").`in`(it))
            }
        }

        // 기간 검색
        if (startDate != null && endDate != null) {
            val start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME)
            val end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME)
            criteria.add(Criteria.where("createdAt").gte(start).lte(end))
        }

        // 최소 좋아요 수
        minLikes?.let {
            criteria.add(Criteria.where("likeCount").gte(it))
        }

        // Query 생성
        val query = Query()
        if (criteria.isNotEmpty()) {
            query.addCriteria(Criteria().andOperator(*criteria.toTypedArray()))
        }

        query.with(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Order.desc("likeCount"),
            org.springframework.data.domain.Sort.Order.desc("createdAt")
        ))
        query.limit(limit)

        return mongoTemplate.find(query, PostDocument::class.java, collectionName)
    }
}
