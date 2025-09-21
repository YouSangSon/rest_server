package yousang.rest_server.application.service.greeting

import yousang.rest_server.application.ports.`in`.greeting.GetGreetingUseCase
import yousang.rest_server.application.ports.`in`.greeting.GreetingDto

/**
 * Application service implementing the use case.
 * No Spring annotations here to keep the core independent.
 */
class GetGreetingService : GetGreetingUseCase {
    override fun greet(name: String?): GreetingDto {
        val normalized = name?.trim().orEmpty()
        val finalName = if (normalized.isEmpty()) "World" else normalized
        val message = "Hello, $finalName!"
        return GreetingDto(message)
    }
}
