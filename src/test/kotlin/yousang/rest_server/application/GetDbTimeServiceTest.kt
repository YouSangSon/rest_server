package yousang.rest_server.application

import kotlin.test.Test
import kotlin.test.assertEquals
import yousang.rest_server.application.ports.out.db.DbInfoPort
import yousang.rest_server.application.service.db.GetDbTimeService

class GetDbTimeServiceTest {

    private class FakeDbInfoPort : DbInfoPort {
        override fun fetchCurrentTime(): String = "2025-01-01T12:34:56.789+09:00"
    }

    @Test
    fun `getCurrentTime returns value from port`() {
        val service = GetDbTimeService(FakeDbInfoPort())
        val dto = service.getCurrentTime()
        assertEquals("2025-01-01T12:34:56.789+09:00", dto.time)
    }
}
