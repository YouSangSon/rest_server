package yousang.rest_server.adapter.`in`.web.greeting

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import yousang.rest_server.application.ports.`in`.greeting.GetGreetingUseCase

@RestController
@RequestMapping("/api/v1/greetings")
class GreetingController(
    private val getGreetingUseCase: GetGreetingUseCase
) {
    @GetMapping
    fun getGreeting(@RequestParam(required = false) name: String?): GreetingResponse {
        val result = getGreetingUseCase.greet(name)
        return GreetingResponse(message = result.message)
    }
}

data class GreetingResponse(val message: String)
