package yousang.rest_server.application

import kotlin.test.Test
import kotlin.test.assertEquals
import yousang.rest_server.application.service.greeting.GetGreetingService

class GetGreetingServiceTest {

    private val service = GetGreetingService()

    @Test
    fun `greet returns Hello Name when name provided`() {
        val result = service.greet("Junie")
        assertEquals("Hello, Junie!", result.message)
    }

    @Test
    fun `greet defaults to World when name is null or blank`() {
        assertEquals("Hello, World!", service.greet(null).message)
        assertEquals("Hello, World!", service.greet("").message)
        assertEquals("Hello, World!", service.greet("   ").message)
    }
}
