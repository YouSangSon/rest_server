package yousang.rest.shared.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 모든 컨트롤러에서 발생하는 예외를 일관되게 처리하는 글로벌 예외 핸들러
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = KotlinLogging.logger {}
    
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllExceptions(ex: Exception): ResponseEntity<Any> {
        logger.error(ex) { "Unhandled exception occurred" }
        return ResponseEntity(
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = ex.message ?: "An unexpected error occurred"
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
    
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Any> {
        logger.warn(ex) { "IllegalArgumentException occurred" }
        return ResponseEntity(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = ex.message ?: "Invalid argument provided"
            ),
            HttpStatus.BAD_REQUEST
        )
    }
    
    @ExceptionHandler(NoSuchElementException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<Any> {
        logger.warn(ex) { "NoSuchElementException occurred" }
        return ResponseEntity(
            ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error = "Not Found",
                message = ex.message ?: "Requested resource not found"
            ),
            HttpStatus.NOT_FOUND
        )
    }
    
    @ExceptionHandler(SecurityException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleSecurityException(ex: SecurityException): ResponseEntity<Any> {
        logger.warn(ex) { "SecurityException occurred" }
        return ResponseEntity(
            ErrorResponse(
                status = HttpStatus.FORBIDDEN.value(),
                error = "Forbidden",
                message = ex.message ?: "Access denied"
            ),
            HttpStatus.FORBIDDEN
        )
    }
    
    @ExceptionHandler(RuntimeException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<Any> {
        logger.error(ex) { "RuntimeException occurred" }
        return ResponseEntity(
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = ex.message ?: "An unexpected runtime error occurred"
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
    
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException): ResponseEntity<Any> {
        logger.warn(ex) { "ResponseStatusException occurred: ${ex.statusCode}" }
        return ResponseEntity(
            ErrorResponse(
                status = ex.statusCode.value(),
                error = ex.reason ?: ex.statusCode.toString(),
                message = ex.message ?: "An error occurred with status: ${ex.statusCode.value()}"
            ),
            ex.statusCode
        )
    }
    
    data class ErrorResponse(
        val status: Int,
        val error: String,
        val message: String
    )
}