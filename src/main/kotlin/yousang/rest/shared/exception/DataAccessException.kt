package yousang.rest.shared.exception

/**
 * 데이터 접근 계층에서 발생하는 예외
 */
class DataAccessException(
    message: String,
    cause: Throwable? = null
) : ApplicationException(message, cause) 