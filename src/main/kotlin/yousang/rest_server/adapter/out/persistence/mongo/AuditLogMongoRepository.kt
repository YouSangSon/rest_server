package yousang.rest_server.adapter.out.persistence.mongo

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import yousang.rest_server.domain.model.EventType
import java.time.LocalDateTime

@Repository
interface AuditLogMongoRepository : MongoRepository<AuditLogDocument, String> {
    fun findByUsername(username: String): List<AuditLogDocument>
    fun findByEventType(eventType: EventType): List<AuditLogDocument>
    fun findByTimestampBetween(start: LocalDateTime, end: LocalDateTime): List<AuditLogDocument>
    fun findByUsernameAndEventType(username: String, eventType: EventType): List<AuditLogDocument>
}
