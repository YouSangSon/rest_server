package yousang.rest_server.adapter.`in`.web.db

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import yousang.rest_server.application.ports.`in`.db.GetDbTimeUseCase

@RestController
@Profile("postgres")
@RequestMapping("/api/v1/db")
class DbInfoController(
    private val getDbTimeUseCase: GetDbTimeUseCase
) {
    @GetMapping("/time")
    fun getDbTime(): DbTimeResponse {
        val dto = getDbTimeUseCase.getCurrentTime()
        return DbTimeResponse(dto.time)
    }
}

data class DbTimeResponse(val time: String)
