package yousang.rest_server.adapter.`in`.web

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import yousang.rest_server.adapter.`in`.web.greeting.GreetingController
import yousang.rest_server.application.ports.`in`.greeting.GetGreetingUseCase
import yousang.rest_server.application.ports.`in`.greeting.GreetingDto

@WebMvcTest(GreetingController::class)
class GreetingControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var getGreetingUseCase: GetGreetingUseCase

    @Test
    fun `GET greeting returns message from use case`() {
        Mockito.`when`(getGreetingUseCase.greet("Junie")).thenReturn(GreetingDto("Hello, Junie!"))

        mockMvc.perform(
            get("/api/v1/greetings")
                .param("name", "Junie")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Hello, Junie!"))
    }

    @Test
    fun `GET greeting without name uses default from use case`() {
        Mockito.`when`(getGreetingUseCase.greet(null)).thenReturn(GreetingDto("Hello, World!"))

        mockMvc.perform(
            get("/api/v1/greetings")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Hello, World!"))
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun getGreetingUseCase(): GetGreetingUseCase = Mockito.mock(GetGreetingUseCase::class.java)
    }
}
