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
import yousang.rest_server.adapter.`in`.web.lotto.LottoNumbersController
import yousang.rest_server.application.ports.`in`.lottoOfficial.OfficialLottoDto
import yousang.rest_server.application.ports.`in`.lottoOfficial.OfficialLottoUseCase
import java.time.LocalDate

@WebMvcTest(LottoNumbersController::class)
class LottoNumbersControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var lottoUseCase: OfficialLottoUseCase

    @Test
    fun `GET lotto numbers range returns list wrapped in ApiResponse`() {
        val d1 = OfficialLottoDto(
            id = null,
            drwNo = 1,
            drwNoDate = LocalDate.of(2002, 12, 7),
            drwtNo1 = 1, drwtNo2 = 2, drwtNo3 = 3,
            drwtNo4 = 4, drwtNo5 = 5, drwtNo6 = 6,
            bnusNo = 7
        )
        val d2 = d1.copy(drwNo = 2, drwNoDate = d1.drwNoDate.plusWeeks(1))
        Mockito.`when`(lottoUseCase.getLotto(1, 2)).thenReturn(listOf(d1, d2))

        mockMvc.perform(
            get("/api/v1/lotto/numbers")
                .param("firstDrwNo", "1")
                .param("lastDrwNo", "2")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status_code").value(200))
            .andExpect(jsonPath("$.message").value("Retrieved lotto numbers from 1 to 2"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].drwNo").value(1))
            .andExpect(jsonPath("$.data[1].drwNo").value(2))
    }

    @Test
    fun `GET lotto numbers by draw returns single item wrapped in ApiResponse`() {
        val d1 = OfficialLottoDto(
            id = null,
            drwNo = 10,
            drwNoDate = LocalDate.of(2003, 2, 8),
            drwtNo1 = 10, drwtNo2 = 11, drwtNo3 = 12,
            drwtNo4 = 13, drwtNo5 = 14, drwtNo6 = 15,
            bnusNo = 16
        )
        Mockito.`when`(lottoUseCase.getLotto(10)).thenReturn(d1)

        mockMvc.perform(
            get("/api/v1/lotto/numbers/10")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status_code").value(200))
            .andExpect(jsonPath("$.message").value("Retrieved lotto number for draw 10"))
            .andExpect(jsonPath("$.data.drwNo").value(10))
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun officialLottoUseCase(): OfficialLottoUseCase = Mockito.mock(OfficialLottoUseCase::class.java)
    }
}
