package yousang.rest_server.application.service.db

import yousang.rest_server.application.ports.`in`.db.DbTimeDto
import yousang.rest_server.application.ports.`in`.db.GetDbTimeUseCase
import yousang.rest_server.application.ports.out.db.DbInfoPort

/**
 * Application service implementing the GetDbTimeUseCase.
 * Pure Kotlin, no framework annotations.
 */
class GetDbTimeService(
    private val dbInfoPort: DbInfoPort
) : GetDbTimeUseCase {
    override fun getCurrentTime(): DbTimeDto {
        val time = dbInfoPort.fetchCurrentTime()
        return DbTimeDto(time)
    }
}
