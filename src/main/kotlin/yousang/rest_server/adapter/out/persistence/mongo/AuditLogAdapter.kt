package yousang.rest_server.adapter.out.persistence.mongo

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import yousang.rest_server.application.ports.out.AuditLogPort
import yousang.rest_server.domain.model.AuditLog
import yousang.rest_server.domain.model.EventType
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(name = ["spring.data.mongodb.uri"])
class AuditLogAdapter(
    private val auditLogMongoRepository: AuditLogMongoRepository
) : AuditLogPort {

    override fun save(auditLog: AuditLog): AuditLog {
        val document = AuditLogDocument.fromDomain(auditLog)
        val saved = auditLogMongoRepository.save(document)
        return saved.toDomain()
    }

    override fun findById(id: String): AuditLog? {
        return auditLogMongoRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUsername(username: String): List<AuditLog> {
        return auditLogMongoRepository.findByUsername(username)
            .map { it.toDomain() }
    }

    override fun findByEventType(eventType: EventType): List<AuditLog> {
        return auditLogMongoRepository.findByEventType(eventType)
            .map { it.toDomain() }
    }

    override fun findByDateRange(start: LocalDateTime, end: LocalDateTime): List<AuditLog> {
        return auditLogMongoRepository.findByTimestampBetween(start, end)
            .map { it.toDomain() }
    }
}
