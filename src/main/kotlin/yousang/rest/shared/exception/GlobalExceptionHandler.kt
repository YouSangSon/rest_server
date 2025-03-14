package yousang.rest.shared.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import yousang.rest.interfaces.ApiResponse
import yousang.rest.shared.log.LoggerDelegate
import yousang.rest.shared.utils.DebugHelper

/**
 * 모든 컨트롤러에서 발생하는 예외를 일관되게 처리하는 글로벌 예외 핸들러
 */
@ControllerAdvice
class GlobalExceptionHandler {
    
    private val log by LoggerDelegate()
    
    @ExceptionHandler(BaseException::class)
    @ResponseBody
    fun handleBaseException(ex: BaseException, request: WebRequest): ApiResponse {
        // 디버그 모드에서 더 상세한 로깅 제공
        DebugHelper.logException(log, "BaseException 발생", ex)
        
        // 요청 정보 로깅
        val requestUri = request.getDescription(false).replace("uri=", "")
        log.error("Request: $requestUri resulted in error: ${ex.message}")
        
        return ApiResponse(
            statusCode = ex.status.value(), 
            message = ex.message ?: "An error occurred"
        )
    }
    
    @ExceptionHandler(ServiceException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun handleServiceException(ex: ServiceException, request: WebRequest): ApiResponse {
        // 디버그 모드에서 더 상세한 로깅 제공
        DebugHelper.logException(log, "서비스 예외 발생", ex)
        
        // 요청 정보 로깅
        val requestUri = request.getDescription(false).replace("uri=", "")
        log.error("Request: $requestUri resulted in service error: ${ex.message}")
        
        return ApiResponse(
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = ex.message ?: "A service error occurred"
        )
    }
    
    @ExceptionHandler(DataAccessException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun handleDataAccessException(ex: DataAccessException, request: WebRequest): ApiResponse {
        // 디버그 모드에서 더 상세한 로깅 제공
        DebugHelper.logException(log, "데이터 접근 예외 발생", ex)
        
        // 요청 정보 로깅
        val requestUri = request.getDescription(false).replace("uri=", "")
        log.error("Request: $requestUri resulted in data access error: ${ex.message}")
        
        return ApiResponse(
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "데이터 접근 중 오류가 발생했습니다."
        )
    }
    
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun handleAllExceptions(ex: Exception, request: WebRequest): ApiResponse {
        // 디버그 모드에서 더 상세한 로깅 제공
        DebugHelper.logException(log, "예상치 못한 예외 발생", ex)
        
        // 요청 정보 로깅
        val requestUri = request.getDescription(false).replace("uri=", "")
        log.error("Request: $requestUri resulted in unexpected error", ex)
        
        // 개발 환경에서는 실제 오류 메시지 포함
        val isDevMode = isDevMode()
        val message = if (isDevMode) {
            "서버 내부 오류가 발생했습니다: ${ex.message}"
        } else {
            "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."
        }
        
        return ApiResponse(
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = message
        )
    }
    
    /**
     * 현재 개발 모드인지 확인
     */
    private fun isDevMode(): Boolean {
        val env = System.getProperty("spring.profiles.active") ?: "default"
        return env.contains("dev") || env.contains("local") || env.contains("debug")
    }
}