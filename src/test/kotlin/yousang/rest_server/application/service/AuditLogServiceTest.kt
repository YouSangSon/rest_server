package yousang.rest_server.application.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import yousang.rest_server.application.ports.out.AuditLogPort
import yousang.rest_server.domain.model.AuditLog
import yousang.rest_server.domain.model.EventType
import kotlin.test.assertEquals

class AuditLogServiceTest {

    private lateinit var auditLogService: AuditLogService
    private lateinit var auditLogPort: AuditLogPort

    @BeforeEach
    fun setup() {
        auditLogPort = mock()
        auditLogService = AuditLogService(auditLogPort)
    }

    @Test
    fun `should log user login event successfully`() {
        // Given
        val username = "testuser"
        val ipAddress = "127.0.0.1"
        val userAgent = "Mozilla/5.0"

        whenever(auditLogPort.save(any())).thenAnswer { it.getArgument(0) }

        // When
        auditLogService.logUserLogin(username, ipAddress, userAgent, true)

        // Then
        verify(auditLogPort).save(argThat {
            this.eventType == EventType.USER_LOGIN &&
                    this.username == username &&
                    this.ipAddress == ipAddress &&
                    this.userAgent == userAgent &&
                    this.success == true
        })
    }

    @Test
    fun `should log OAuth2 login event successfully`() {
        // Given
        val username = "testuser"
        val provider = "google"
        val ipAddress = "127.0.0.1"
        val userAgent = "Mozilla/5.0"

        whenever(auditLogPort.save(any())).thenAnswer { it.getArgument(0) }

        // When
        auditLogService.logOAuth2Login(username, provider, ipAddress, userAgent)

        // Then
        verify(auditLogPort).save(argThat {
            this.eventType == EventType.OAUTH2_LOGIN &&
                    this.username == username &&
                    this.details?.get("provider") == provider &&
                    this.ipAddress == ipAddress &&
                    this.userAgent == userAgent
        })
    }

    @Test
    fun `should get user logs successfully`() {
        // Given
        val username = "testuser"
        val expectedLogs = listOf(
            AuditLog(
                id = "1",
                eventType = EventType.USER_LOGIN,
                username = username,
                action = "login",
                resourceType = "user",
                resourceId = username,
                ipAddress = "127.0.0.1",
                userAgent = "Mozilla/5.0"
            ),
            AuditLog(
                id = "2",
                eventType = EventType.OAUTH2_LOGIN,
                username = username,
                action = "oauth2_login",
                resourceType = "user",
                resourceId = username,
                ipAddress = "127.0.0.1",
                userAgent = "Mozilla/5.0",
                details = mapOf("provider" to "google")
            )
        )

        whenever(auditLogPort.findByUsername(username)).thenReturn(expectedLogs)

        // When
        val result = auditLogService.getUserLogs(username)

        // Then
        assertEquals(2, result.size)
        assertEquals(EventType.USER_LOGIN, result[0].eventType)
        assertEquals(EventType.OAUTH2_LOGIN, result[1].eventType)
        verify(auditLogPort).findByUsername(username)
    }

    @Test
    fun `should get logs by event type successfully`() {
        // Given
        val eventType = EventType.USER_LOGIN
        val expectedLogs = listOf(
            AuditLog(
                id = "1",
                eventType = eventType,
                username = "user1",
                action = "login",
                resourceType = "user",
                resourceId = "user1",
                ipAddress = "127.0.0.1",
                userAgent = "Mozilla/5.0"
            )
        )

        whenever(auditLogPort.findByEventType(eventType)).thenReturn(expectedLogs)

        // When
        val result = auditLogService.getLogsByType(eventType)

        // Then
        assertEquals(1, result.size)
        assertEquals(eventType, result[0].eventType)
        verify(auditLogPort).findByEventType(eventType)
    }
}
