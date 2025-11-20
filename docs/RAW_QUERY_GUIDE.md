# Raw Query 가이드

REST Server에서 PostgreSQL과 MongoDB에 대한 Raw Query를 실행하는 방법을 설명합니다.

## 📋 목차

1. [PostgreSQL Raw Query (JdbcTemplate)](#postgresql-raw-query)
2. [MongoDB Raw Query (MongoTemplate)](#mongodb-raw-query)
3. [커스텀 리포지토리 생성](#커스텀-리포지토리-생성)
4. [사용 예제](#사용-예제)

---

## PostgreSQL Raw Query

### JdbcTemplate을 사용한 Raw SQL 실행

PostgreSQL에서는 `JdbcTemplate`과 커스텀 리포지토리를 사용하여 Raw SQL을 실행할 수 있습니다.

#### 1. 커스텀 리포지토리 인터페이스 정의

```kotlin
interface SnsUserCustomRepository {
    fun executeRawQuery(sql: String, params: Map<String, Any>): List<Map<String, Any>>
    fun searchUsersWithNativeQuery(searchTerm: String, limit: Int): List<SnsUserEntity>
    fun findTopUsersByFollowers(limit: Int): List<SnsUserEntity>
    fun getUserStatistics(userId: Long): Map<String, Any>
    fun batchInsertUsers(users: List<SnsUserEntity>): Int
}
```

#### 2. 커스텀 리포지토리 구현

```kotlin
@Repository
class SnsUserCustomRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : SnsUserCustomRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun executeRawQuery(sql: String, params: Map<String, Any>): List<Map<String, Any>> {
        // Named parameters 처리
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
        return jdbcTemplate.query(sql, { rs, _ -> mapResultSetToEntity(rs) },
            pattern, pattern, pattern, limit)
    }
}
```

#### 3. JPA Repository에 커스텀 리포지토리 상속

```kotlin
@Repository
interface SnsUserJpaRepository :
    JpaRepository<SnsUserEntity, Long>,
    SnsUserCustomRepository  // 커스텀 리포지토리 상속
{
    fun findByEmail(email: String): SnsUserEntity?
    fun findByUsername(username: String): SnsUserEntity?
}
```

### PostgreSQL Raw Query 예제

#### 예제 1: 단순 조회

```kotlin
val sql = "SELECT * FROM sns_users WHERE follower_count > 1000 ORDER BY follower_count DESC LIMIT 10"
val users = repository.executeRawQuery(sql, emptyMap())
```

#### 예제 2: Named Parameters

```kotlin
val sql = """
    SELECT * FROM sns_users
    WHERE created_at >= :startDate
    AND follower_count >= :minFollowers
"""
val params = mapOf(
    "startDate" to LocalDateTime.now().minusDays(30),
    "minFollowers" to 100
)
val users = repository.executeRawQuery(sql, params)
```

#### 예제 3: JOIN 쿼리

```kotlin
val sql = """
    SELECT
        u.user_id,
        u.username,
        COUNT(f.follow_id) as follower_count
    FROM sns_users u
    LEFT JOIN sns_follows f ON f.following_id = u.user_id
    WHERE u.is_active = true
    GROUP BY u.user_id, u.username
    HAVING COUNT(f.follow_id) > :minFollowers
    ORDER BY follower_count DESC
    LIMIT :limit
"""
val params = mapOf("minFollowers" to 500, "limit" to 20)
val result = repository.executeRawQuery(sql, params)
```

#### 예제 4: 통계 쿼리

```kotlin
val stats = repository.getUserStatistics(userId)
// 결과:
// {
//   "user_id": 123,
//   "username": "johndoe",
//   "follower_count": 1500,
//   "actual_follower_count": 1498,
//   "account_age_days": 365
// }
```

#### 예제 5: Batch Insert

```kotlin
val users = listOf(
    SnsUserEntity(...),
    SnsUserEntity(...),
    SnsUserEntity(...)
)
val insertedCount = repository.batchInsertUsers(users)
println("Inserted $insertedCount users")
```

---

## MongoDB Raw Query

### MongoTemplate을 사용한 Raw Query 및 Aggregation

MongoDB에서는 `MongoTemplate`과 커스텀 리포지토리를 사용하여 복잡한 쿼리와 Aggregation을 실행할 수 있습니다.

#### 1. 커스텀 리포지토리 인터페이스 정의

```kotlin
interface PostCustomRepository {
    fun executeRawQuery(query: String): List<PostDocument>
    fun executeAggregation(pipeline: List<Map<String, Any>>): List<Map<String, Any>>
    fun countPostsByHashtag(): List<Map<String, Any>>
    fun findTopPostsByUser(userId: Long, limit: Int): List<PostDocument>
    fun getPostStatistics(startDate: String, endDate: String): Map<String, Any>
    fun complexSearch(/* params */): List<PostDocument>
}
```

#### 2. 커스텀 리포지토리 구현

```kotlin
@Repository
class PostCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
    private val objectMapper: ObjectMapper
) : PostCustomRepository {

    private val collectionName = "sns_posts"

    override fun executeRawQuery(query: String): List<PostDocument> {
        val document = Document.parse(query)
        val mongoQuery = Query()

        document.forEach { key, value ->
            mongoQuery.addCriteria(Criteria.where(key).`is`(value))
        }

        return mongoTemplate.find(mongoQuery, PostDocument::class.java, collectionName)
    }

    override fun executeAggregation(pipeline: List<Map<String, Any>>): List<Map<String, Any>> {
        val collection = mongoTemplate.getCollection(collectionName)
        val bsonPipeline = pipeline.map { Document(it) }

        val results = mutableListOf<Map<String, Any>>()
        collection.aggregate(bsonPipeline).forEach { doc ->
            results.add(doc.toMap())
        }
        return results
    }
}
```

#### 3. MongoRepository에 커스텀 리포지토리 상속

```kotlin
@Repository
interface PostMongoRepository :
    MongoRepository<PostDocument, Long>,
    PostCustomRepository  // 커스텀 리포지토리 상속
{
    @Query("{ 'userId': ?0, 'isHidden': false }")
    fun findByUserId(userId: Long, pageable: Pageable): List<PostDocument>
}
```

### MongoDB Raw Query 예제

#### 예제 1: 단순 조회

```kotlin
val query = """
    {
        "isHidden": false,
        "likeCount": { "${'$'}gte": 100 }
    }
"""
val posts = repository.executeRawQuery(query)
```

#### 예제 2: 복잡한 필터

```kotlin
val query = """
    {
        "${'$'}and": [
            { "isHidden": false },
            { "likeCount": { "${'$'}gte": 100 } },
            { "${'$'}or": [
                { "hashtags": { "${'$'}in": ["investing", "stocks"] } },
                { "caption": { "${'$'}regex": "bitcoin", "${'$'}options": "i" } }
            ]}
        ]
    }
"""
val posts = repository.executeRawQuery(query)
```

#### 예제 3: Aggregation Pipeline

```kotlin
val pipeline = listOf(
    mapOf(
        "\$match" to mapOf(
            "isHidden" to false,
            "createdAt" to mapOf(
                "\$gte" to "2025-01-01T00:00:00",
                "\$lte" to "2025-12-31T23:59:59"
            )
        )
    ),
    mapOf(
        "\$group" to mapOf(
            "_id" to "\$userId",
            "totalPosts" to mapOf("\$sum" to 1),
            "totalLikes" to mapOf("\$sum" to "\$likeCount"),
            "avgLikes" to mapOf("\$avg" to "\$likeCount")
        )
    ),
    mapOf(
        "\$sort" to mapOf("totalLikes" to -1)
    ),
    mapOf(
        "\$limit" to 10
    )
)

val result = repository.executeAggregation(pipeline)
// 결과: 사용자별 게시물 통계 (상위 10명)
```

#### 예제 4: 해시태그별 집계

```kotlin
val hashtagCounts = repository.countPostsByHashtag()
// 결과:
// [
//   {"_id": "investing", "count": 1500},
//   {"_id": "stocks", "count": 1200},
//   {"_id": "crypto", "count": 950}
// ]
```

#### 예제 5: 복잡한 검색

```kotlin
val posts = repository.complexSearch(
    searchText = "bitcoin",
    hashtags = listOf("crypto", "investing"),
    startDate = "2025-01-01T00:00:00",
    endDate = "2025-12-31T23:59:59",
    minLikes = 50,
    limit = 20
)
```

#### 예제 6: 통계 쿼리

```kotlin
val stats = repository.getPostStatistics(
    startDate = "2025-01-01T00:00:00",
    endDate = "2025-01-31T23:59:59"
)
// 결과:
// {
//   "totalPosts": 5000,
//   "totalLikes": 125000,
//   "totalComments": 35000,
//   "totalViews": 500000,
//   "avgLikes": 25.0
// }
```

---

## 커스텀 리포지토리 생성

새로운 엔티티/도큐먼트에 대한 커스텀 리포지토리를 생성하는 방법:

### 1. 커스텀 리포지토리 인터페이스 생성

```kotlin
// PostgreSQL용
interface MyEntityCustomRepository {
    fun executeCustomQuery(params: Map<String, Any>): List<MyEntity>
}

// MongoDB용
interface MyDocumentCustomRepository {
    fun executeCustomAggregation(pipeline: List<Map<String, Any>>): List<Map<String, Any>>
}
```

### 2. 커스텀 리포지토리 구현

```kotlin
// PostgreSQL용
@Repository
class MyEntityCustomRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : MyEntityCustomRepository {

    override fun executeCustomQuery(params: Map<String, Any>): List<MyEntity> {
        val sql = "SELECT * FROM my_table WHERE column1 = ?"
        return jdbcTemplate.query(sql, { rs, _ ->
            // ResultSet을 Entity로 매핑
            MyEntity(...)
        }, params["value"])
    }
}

// MongoDB용
@Repository
class MyDocumentCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : MyDocumentCustomRepository {

    override fun executeCustomAggregation(pipeline: List<Map<String, Any>>): List<Map<String, Any>> {
        val collection = mongoTemplate.getCollection("my_collection")
        val bsonPipeline = pipeline.map { Document(it) }

        val results = mutableListOf<Map<String, Any>>()
        collection.aggregate(bsonPipeline).forEach { doc ->
            results.add(doc.toMap())
        }
        return results
    }
}
```

### 3. 메인 리포지토리에 상속

```kotlin
// PostgreSQL용
@Repository
interface MyEntityRepository : JpaRepository<MyEntity, Long>, MyEntityCustomRepository

// MongoDB용
@Repository
interface MyDocumentRepository : MongoRepository<MyDocument, Long>, MyDocumentCustomRepository
```

---

## 사용 예제

### Service Layer에서 사용

```kotlin
@Service
class SnsUserService(
    private val userRepository: SnsUserJpaRepository
) {

    fun searchUsersWithComplexCriteria(searchTerm: String): List<SnsUser> {
        // Spring Data 메서드 사용
        val simpleResults = userRepository.findByUsername(searchTerm)

        // Raw Query 사용
        val complexResults = userRepository.searchUsersWithNativeQuery(searchTerm, 20)

        // Custom Query 사용
        val sql = """
            SELECT u.* FROM sns_users u
            JOIN sns_follows f ON f.following_id = u.user_id
            WHERE f.follower_id = :userId
            AND u.is_active = true
        """
        val rawResults = userRepository.executeRawQuery(sql, mapOf("userId" to 123))

        return complexResults.map { it.toDomain() }
    }

    fun getUserStatistics(userId: Long): UserStatistics {
        val stats = userRepository.getUserStatistics(userId)

        return UserStatistics(
            userId = stats["user_id"] as Long,
            username = stats["username"] as String,
            followerCount = stats["follower_count"] as Int,
            accountAgeDays = stats["account_age_days"] as Long
        )
    }
}
```

```kotlin
@Service
class PostService(
    private val postRepository: PostMongoRepository
) {

    fun getTrendingHashtags(): List<HashtagCount> {
        val results = postRepository.countPostsByHashtag()

        return results.map {
            HashtagCount(
                hashtag = it["_id"] as String,
                count = (it["count"] as Number).toInt()
            )
        }
    }

    fun getPostAnalytics(startDate: String, endDate: String): PostAnalytics {
        val stats = postRepository.getPostStatistics(startDate, endDate)

        return PostAnalytics(
            totalPosts = (stats["totalPosts"] as Number).toInt(),
            totalLikes = (stats["totalLikes"] as Number).toInt(),
            totalComments = (stats["totalComments"] as Number).toInt(),
            avgLikes = (stats["avgLikes"] as Number).toDouble()
        )
    }
}
```

---

## 🎯 Best Practices

### PostgreSQL (JdbcTemplate)

1. **Prepared Statements 사용**
   ```kotlin
   // Good ✅
   jdbcTemplate.query("SELECT * FROM users WHERE id = ?", userId)

   // Bad ❌ (SQL Injection 위험)
   jdbcTemplate.query("SELECT * FROM users WHERE id = $userId")
   ```

2. **Named Parameters 활용**
   ```kotlin
   val sql = "SELECT * FROM users WHERE created_at > :startDate AND status = :status"
   val params = mapOf("startDate" to date, "status" to "active")
   ```

3. **Batch Operations**
   ```kotlin
   jdbcTemplate.batchUpdate(sql, batchArgs)  // 대량 데이터 처리 시
   ```

### MongoDB (MongoTemplate)

1. **Index 활용**
   ```kotlin
   @Document(collection = "posts")
   @CompoundIndex(name = "user_created_idx", def = "{'userId': 1, 'createdAt': -1}")
   data class PostDocument(...)
   ```

2. **Projection 사용**
   ```kotlin
   val query = Query(Criteria.where("userId").`is`(userId))
   query.fields().include("postId", "caption").exclude("_id")
   ```

3. **Aggregation Pipeline 최적화**
   ```kotlin
   // $match를 먼저 사용하여 데이터 필터링
   val pipeline = listOf(
       mapOf("\$match" to matchCriteria),  // 먼저 필터링
       mapOf("\$group" to groupBy),
       mapOf("\$sort" to sortBy)
   )
   ```

---

## 🔍 디버깅

### SQL 로깅 활성화

```yaml
# application.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
logging:
  level:
    org.springframework.jdbc.core: DEBUG
    org.springframework.data.mongodb.core: DEBUG
```

### Query 실행 시간 측정

```kotlin
val startTime = System.currentTimeMillis()
val results = repository.executeRawQuery(sql, params)
val executionTime = System.currentTimeMillis() - startTime
logger.info("Query executed in ${executionTime}ms")
```

---

## 📚 참고 자료

- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Data MongoDB Documentation](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
- [MongoDB Aggregation Framework](https://www.mongodb.com/docs/manual/aggregation/)
- [JdbcTemplate API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html)

---

**Last Updated**: 2025-11-20 | **Version**: 1.0.0
