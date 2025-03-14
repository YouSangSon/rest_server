package yousang.rest.shared.exception

import org.springframework.http.HttpStatus

/**
 * 애플리케이션의 모든 예외의 추상 기본 클래스
 */
abstract class ApplicationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * HTTP 상태 코드를 포함하는 API 관련 예외
 */
class BaseException(
    val status: HttpStatus,
    message: String,
    cause: Throwable? = null
) : ApplicationException(message, cause)