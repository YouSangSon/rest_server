package yousang.rest.interfaces

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 기본 컨트롤러 클래스로, 공통 엔드포인트를 제공합니다.
 * 모든 엔드포인트는 코루틴을 통한 비동기 처리를 지원합니다.
 */
@Tag(name = "예제 API", description = "기본 테스트용 API")
@RestController
@RequestMapping
abstract class BaseController {

    /**
     * 기본 Hello World 엔드포인트
     * @return Hello World 메시지가 포함된 API 응답
     */
    @Operation(summary = "Hello world", description = "Hello world를 반환합니다.")
    @GetMapping("/hello-world", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun exampleHelloWorld(): ApiResponse {
        return try {
            ApiResponse(
                statusCode = HttpStatus.OK.value(),
                message = "Hello world! Powered by Kotlin Coroutines and Virtual Threads!"
            )
        } catch (e: Exception) {
            ApiResponse(
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = "Error occurred while processing request",
                data = null
            )
        }
    }

    /**
     * 서버 상태 확인 엔드포인트
     */
    @Operation(summary = "서버 상태 확인", description = "서버의 현재 상태를 확인합니다.")
    @GetMapping("/health", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun healthCheck(): ApiResponse {
        return try {
            ApiResponse(
                statusCode = HttpStatus.OK.value(),
                message = "Hello world!"
            )
        } catch (e: Exception) {
            ApiResponse(
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = "Error occurred while processing request",
                data = null
            )
        }
    }
}