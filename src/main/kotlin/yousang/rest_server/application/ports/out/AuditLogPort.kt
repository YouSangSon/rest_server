package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.model.AuditLog
import yousang.rest_server.domain.model.EventType
import java.time.LocalDateTime

interface AuditLogPort {
    fun save(auditLog: AuditLog): AuditLog
    fun findById(id: String): AuditLog?
    fun findByUsername(username: String): List<AuditLog>
    fun findByEventType(eventType: EventType): List<AuditLog>
    fun findByDateRange(start: LocalDateTime, end: LocalDateTime): List<AuditLog>
}
