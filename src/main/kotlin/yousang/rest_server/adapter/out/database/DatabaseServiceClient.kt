package yousang.rest_server.adapter.out.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject

/**
 * Database Service REST API 클라이언트
 *
 * 통합 데이터베이스 서비스를 통한 CRUD 작업
 */
@Component
class DatabaseServiceClient(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${database-service.url:http://localhost:8080}") private val baseUrl: String
) {

    companion object {
        const val API_VERSION = "v1"
        const val HEADER_DB_TYPE = "X-Database-Type"
        const val DB_POSTGRES = "postgres"
        const val DB_MONGODB = "mongodb"
    }

    // ==================== CRUD Operations ====================

    /**
     * 단일 문서 생성
     */
    fun <T> create(
        collection: String,
        document: T,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<T> {
        val url = "$baseUrl/api/$API_VERSION/documents"

        val request = mapOf(
            "collection" to collection,
            "document" to document
        )

        val headers = createHeaders(databaseType)
        val entity = HttpEntity(request, headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    /**
     * ID로 문서 조회
     */
    fun <T> findById(
        collection: String,
        id: String,
        databaseType: String = DB_MONGODB,
        responseType: Class<T>
    ): DatabaseServiceResponse<T>? {
        val url = "$baseUrl/api/$API_VERSION/documents/$collection/$id"

        return try {
            val headers = createHeaders(databaseType)
            val entity = HttpEntity<Any>(headers)

            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String::class.java
            )

            objectMapper.readValue(response.body ?: "{}")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 문서 업데이트 (Optimistic Locking)
     */
    fun <T> update(
        collection: String,
        id: String,
        updates: Map<String, Any>,
        version: Long? = null,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<T> {
        val url = "$baseUrl/api/$API_VERSION/documents/$collection/$id"

        val request = mutableMapOf<String, Any>(
            "updates" to updates
        )
        version?.let { request["version"] = it }

        val headers = createHeaders(databaseType)
        val entity = HttpEntity(request, headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.PUT,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    /**
     * 문서 삭제
     */
    fun delete(
        collection: String,
        id: String,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<Boolean> {
        val url = "$baseUrl/api/$API_VERSION/documents/$collection/$id"

        val headers = createHeaders(databaseType)
        val entity = HttpEntity<Any>(headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.DELETE,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    // ==================== Query Operations ====================

    /**
     * 필터링 및 페이지네이션으로 문서 조회
     */
    fun <T> find(
        collection: String,
        filter: Map<String, Any>? = null,
        sort: Map<String, Int>? = null,
        limit: Int = 100,
        offset: Int = 0,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<List<T>> {
        val url = StringBuilder("$baseUrl/api/$API_VERSION/documents/$collection")
        val params = mutableListOf<String>()

        filter?.let { params.add("filter=${objectMapper.writeValueAsString(it)}") }
        sort?.let { params.add("sort=${objectMapper.writeValueAsString(it)}") }
        params.add("limit=$limit")
        params.add("offset=$offset")

        if (params.isNotEmpty()) {
            url.append("?${params.joinToString("&")}")
        }

        val headers = createHeaders(databaseType)
        val entity = HttpEntity<Any>(headers)

        val response = restTemplate.exchange(
            url.toString(),
            HttpMethod.GET,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    /**
     * 전체 텍스트 검색
     */
    fun <T> search(
        collection: String,
        searchQuery: String,
        fields: List<String>? = null,
        limit: Int = 100,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<List<T>> {
        val url = "$baseUrl/api/$API_VERSION/documents/$collection/search"

        val request = mutableMapOf<String, Any>(
            "query" to searchQuery,
            "limit" to limit
        )
        fields?.let { request["fields"] = it }

        val headers = createHeaders(databaseType)
        val entity = HttpEntity(request, headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    /**
     * 문서 개수 세기
     */
    fun count(
        collection: String,
        filter: Map<String, Any>? = null,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<Long> {
        val url = "$baseUrl/api/$API_VERSION/documents/$collection/count"

        val request = filter?.let { mapOf("filter" to it) } ?: emptyMap<String, Any>()

        val headers = createHeaders(databaseType)
        val entity = HttpEntity(request, headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    // ==================== Atomic Operations ====================

    /**
     * Find and Update (원자적 업데이트)
     */
    fun <T> findAndUpdate(
        collection: String,
        filter: Map<String, Any>,
        updates: Map<String, Any>,
        returnNew: Boolean = true,
        upsert: Boolean = false,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<T> {
        val url = "$baseUrl/api/$API_VERSION/documents/$collection/find-update"

        val request = mapOf(
            "filter" to filter,
            "updates" to updates,
            "options" to mapOf(
                "returnNew" to returnNew,
                "upsert" to upsert
            )
        )

        val headers = createHeaders(databaseType)
        val entity = HttpEntity(request, headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    /**
     * Upsert (Insert or Update)
     */
    fun <T> upsert(
        collection: String,
        filter: Map<String, Any>,
        document: T,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<T> {
        val url = "$baseUrl/api/$API_VERSION/documents/$collection/upsert"

        val request = mapOf(
            "filter" to filter,
            "document" to document
        )

        val headers = createHeaders(databaseType)
        val entity = HttpEntity(request, headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    // ==================== Bulk Operations ====================

    /**
     * 대량 삽입
     */
    fun <T> bulkInsert(
        collection: String,
        documents: List<T>,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<BulkInsertResult> {
        val url = "$baseUrl/api/$API_VERSION/documents/$collection/bulk-insert"

        val request = mapOf(
            "documents" to documents,
            "options" to mapOf("ordered" to false)
        )

        val headers = createHeaders(databaseType)
        val entity = HttpEntity(request, headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    /**
     * 다중 업데이트
     */
    fun updateMany(
        collection: String,
        filter: Map<String, Any>,
        updates: Map<String, Any>,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<UpdateManyResult> {
        val url = "$baseUrl/api/$API_VERSION/documents/$collection/update-many"

        val request = mapOf(
            "filter" to filter,
            "updates" to updates
        )

        val headers = createHeaders(databaseType)
        val entity = HttpEntity(request, headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    /**
     * 다중 삭제
     */
    fun deleteMany(
        collection: String,
        filter: Map<String, Any>,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<DeleteManyResult> {
        val url = "$baseUrl/api/$API_VERSION/documents/$collection/delete-many"

        val request = mapOf("filter" to filter)

        val headers = createHeaders(databaseType)
        val entity = HttpEntity(request, headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    // ==================== Index Management ====================

    /**
     * 인덱스 생성
     */
    fun createIndex(
        collection: String,
        keys: Map<String, Int>,
        unique: Boolean = false,
        name: String? = null,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<String> {
        val url = "$baseUrl/api/$API_VERSION/indexes/$collection"

        val request = mutableMapOf<String, Any>(
            "keys" to keys,
            "options" to mapOf("unique" to unique)
        )
        name?.let { request["name"] = it }

        val headers = createHeaders(databaseType)
        val entity = HttpEntity(request, headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    /**
     * 인덱스 목록 조회
     */
    fun listIndexes(
        collection: String,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<List<IndexInfo>> {
        val url = "$baseUrl/api/$API_VERSION/indexes/$collection"

        val headers = createHeaders(databaseType)
        val entity = HttpEntity<Any>(headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    // ==================== Collection Management ====================

    /**
     * 컬렉션 생성
     */
    fun createCollection(
        name: String,
        databaseType: String = DB_MONGODB
    ): DatabaseServiceResponse<Boolean> {
        val url = "$baseUrl/api/$API_VERSION/collections"

        val request = mapOf("name" to name)

        val headers = createHeaders(databaseType)
        val entity = HttpEntity(request, headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        return objectMapper.readValue(response.body ?: "{}")
    }

    /**
     * 컬렉션 존재 여부 확인
     */
    fun collectionExists(
        collection: String,
        databaseType: String = DB_MONGODB
    ): Boolean {
        val url = "$baseUrl/api/$API_VERSION/collections/$collection/exists"

        return try {
            val headers = createHeaders(databaseType)
            val entity = HttpEntity<Any>(headers)

            val response: DatabaseServiceResponse<Boolean> = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String::class.java
            ).body?.let { objectMapper.readValue(it) } ?: return false

            response.data ?: false
        } catch (e: Exception) {
            false
        }
    }

    // ==================== Health Check ====================

    /**
     * 서비스 헬스 체크
     */
    fun healthCheck(): Boolean {
        return try {
            val response = restTemplate.getForObject<Map<String, Any>>(
                "$baseUrl/health"
            )
            response["status"] == "healthy"
        } catch (e: Exception) {
            false
        }
    }

    // ==================== Helper Methods ====================

    private fun createHeaders(databaseType: String): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set(HEADER_DB_TYPE, databaseType)
        }
    }
}

// ==================== Response Models ====================

data class DatabaseServiceResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: ErrorInfo? = null
)

data class ErrorInfo(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)

data class BulkInsertResult(
    val insertedCount: Int,
    val insertedIds: List<String>
)

data class UpdateManyResult(
    val matchedCount: Int,
    val modifiedCount: Int
)

data class DeleteManyResult(
    val deletedCount: Int
)

data class IndexInfo(
    val name: String,
    val keys: Map<String, Int>,
    val unique: Boolean = false
)
