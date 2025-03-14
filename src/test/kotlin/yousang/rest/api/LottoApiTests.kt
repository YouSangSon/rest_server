package yousang.rest.api

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import yousang.rest.application.lotto.LottoUseCase
import yousang.rest.domain.lotto.LottoEntity
import yousang.rest.interfaces.lotto.LottoController
import yousang.rest.interfaces.lotto.LottoDto
import java.time.LocalDate

@WebMvcTest(LottoController::class)
class LottoApiTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var lottoUseCase: LottoUseCase

    @Test
    fun testGetLottoRangeShouldReturn200OK() = runBlocking<Unit> {
        // Arrange
        val firstDrwNo = 1
        val lastDrwNo = 5
        val lottoList = listOf(
            LottoDto(
                drwNo = firstDrwNo,
                drwNoDate = LocalDate.now().minusDays(7),
                drwtNo1 = 1,
                drwtNo2 = 2,
                drwtNo3 = 3,
                drwtNo4 = 4,
                drwtNo5 = 5,
                drwtNo6 = 6,
                bnusNo = 7,
                firstPrzwnerCo = 2,
                firstAccumamnt = 2000000000,
                firstWinamnt = 1000000000,
                totSellamnt = 5000000000,
                returnValue = "success"
            )
        )
        
        Mockito.`when`(lottoUseCase.getLotto(firstDrwNo, lastDrwNo)).thenReturn(lottoList)
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/lotto/numbers?firstDrwNo=$firstDrwNo&lastDrwNo=$lastDrwNo"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data[0].drwNo").value(firstDrwNo))
            .andDo(print())
    }
    
    @Test
    fun testGetSingleLottoShouldReturn200OK() = runBlocking<Unit> {
        // Arrange
        val drwNo = 1000
        val lottoList = listOf(
            LottoDto(
                drwNo = drwNo,
                drwNoDate = LocalDate.now().minusDays(7),
                drwtNo1 = 1,
                drwtNo2 = 2,
                drwtNo3 = 3,
                drwtNo4 = 4,
                drwtNo5 = 5,
                drwtNo6 = 6,
                bnusNo = 7,
                firstPrzwnerCo = 2,
                firstAccumamnt = 2000000000,
                firstWinamnt = 1000000000,
                totSellamnt = 5000000000,
                returnValue = "success"
            )
        )
        
        Mockito.`when`(lottoUseCase.getLotto(drwNo, drwNo)).thenReturn(lottoList)
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/lotto/single?drwNo=$drwNo"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data[0].drwNo").value(drwNo))
            .andDo(print())
    }
} 