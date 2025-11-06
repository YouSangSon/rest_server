package yousang.rest_server.domain.exception

/**
 * Base exception for all domain-level errors
 */
abstract class DomainException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Thrown when a requested resource is not found
 */
class NotFoundException(message: String) : DomainException(message)

/**
 * Thrown when business validation fails
 */
class ValidationException(message: String) : DomainException(message)

/**
 * Thrown when authentication fails
 */
class UnauthorizedException(message: String) : DomainException(message)

/**
 * Thrown when user doesn't have permission
 */
class ForbiddenException(message: String) : DomainException(message)

/**
 * Thrown when a conflict occurs (e.g., duplicate resource)
 */
class ConflictException(message: String) : DomainException(message)

/**
 * Thrown when invalid input is provided
 */
class BadRequestException(message: String) : DomainException(message)
