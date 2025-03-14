package yousang.rest.interfaces

import kotlinx.coroutines.delay
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CoroutineTestController {

    @GetMapping("/api/v1/coroutine-test")
    suspend fun coroutineTest(): String {
        // Simulate a non-blocking delay
        delay(1000L)
        return "코루틴 테스트 성공!"
    }
} 