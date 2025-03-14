package yousang.rest.shared.exception

/**
 * 서비스 계층에서 발생하는 기본 예외
 */
abstract class ServiceException(
    message: String,
    cause: Throwable? = null
) : ApplicationException(message, cause)

/**
 * Lotto 서비스에서 발생하는 예외
 */
class LottoServiceException(
    message: String,
    cause: Throwable? = null
) : ServiceException(message, cause)

/**
 * AnnuityLotto 서비스에서 발생하는 예외
 */
class AnnuityLottoServiceException(
    message: String,
    cause: Throwable? = null
) : ServiceException(message, cause) 