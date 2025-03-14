package yousang.rest.adapter.out.lottery.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * DTO for Lotto 6/45 JSON response from dhlottery.co.kr
 */
data class LottoResponseDto(
    @JsonProperty("drwNo")
    val drawNo: Int,
    
    @JsonProperty("drwNoDate")
    val drawDateStr: String,
    
    @JsonProperty("drwtNo1")
    val number1: Int,
    
    @JsonProperty("drwtNo2")
    val number2: Int,
    
    @JsonProperty("drwtNo3")
    val number3: Int,
    
    @JsonProperty("drwtNo4")
    val number4: Int,
    
    @JsonProperty("drwtNo5")
    val number5: Int,
    
    @JsonProperty("drwtNo6")
    val number6: Int,
    
    @JsonProperty("bnusNo")
    val bonusNo: Int,
    
    @JsonProperty("firstWinamnt")
    val firstPrizeAmount: Long,
    
    @JsonProperty("firstPrzwnerCo")
    val firstPrizeWinners: Int
) {
    fun getDrawDate(): LocalDate {
        return LocalDate.parse(drawDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    
    fun getNumbers(): List<Int> {
        return listOf(number1, number2, number3, number4, number5, number6)
    }
}

/**
 * DTO for Pension Lottery response from dhlottery.co.kr
 * Note: The actual format may differ, this is a placeholder
 */
data class PensionLotteryResponseDto(
    val round: Int,
    val winnerGroup: Int,
    val number1: Int,
    val number2: Int,
    val number3: Int,
    val number4: Int,
    val number5: Int,
    val number6: Int,
    val drawDate: String,
    val firstPrizeAmount: Long,
    val firstPrizeWinners: Int
) {
    fun getDrawDate(): LocalDate {
        return LocalDate.parse(drawDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    
    fun getNumbers(): List<Int> {
        return listOf(number1, number2, number3, number4, number5, number6)
    }
} 