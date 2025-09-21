package yousang.rest_server.application.ports.`in`.greeting

/**
 * Input port (use case) for producing a greeting.
 * Part of the application (use case) layer – independent from frameworks.
 */
interface GetGreetingUseCase {
    fun greet(name: String?): GreetingDto
}

/**
 * Simple DTO returned by the use case.
 */
data class GreetingDto(val message: String)
