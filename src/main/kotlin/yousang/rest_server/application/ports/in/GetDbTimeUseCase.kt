package yousang.rest_server.application.ports.`in`.db

/**
 * Input port (use case) for retrieving current database time.
 */
interface GetDbTimeUseCase {
    fun getCurrentTime(): DbTimeDto
}

/**
 * DTO for DB time response
 */
data class DbTimeDto(val time: String)
