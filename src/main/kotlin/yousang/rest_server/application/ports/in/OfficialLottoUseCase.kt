package yousang.rest_server.application.ports.`in`.lottoOfficial

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

/**
 * Use case for official lotto draw data (reference guide style).
 */
interface OfficialLottoUseCase {
    fun getLotto(firstDrwNo: Int, lastDrwNo: Int): List<OfficialLottoDto>
    fun putLotto()
    fun getLotto(drwNo: Int): OfficialLottoDto?
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class OfficialLottoDto(
    @JsonProperty("id")
    val id: Long? = null,

    @JsonProperty("drwNo")
    val drwNo: Int,

    @JsonProperty("drwNoDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val drwNoDate: LocalDate,

    @JsonProperty("drwtNo1")
    val drwtNo1: Int,
    @JsonProperty("drwtNo2")
    val drwtNo2: Int,
    @JsonProperty("drwtNo3")
    val drwtNo3: Int,
    @JsonProperty("drwtNo4")
    val drwtNo4: Int,
    @JsonProperty("drwtNo5")
    val drwtNo5: Int,
    @JsonProperty("drwtNo6")
    val drwtNo6: Int,

    @JsonProperty("bnusNo")
    val bnusNo: Int,

    @JsonProperty("firstPrzwnerCo")
    val firstPrzwnerCo: Int = 0,
    @JsonProperty("firstAccumamnt")
    val firstAccumamnt: Long = 0,
    @JsonProperty("firstWinamnt")
    val firstWinamnt: Long = 0,
    @JsonProperty("totSellamnt")
    val totSellamnt: Long = 0,

    @JsonProperty("returnValue")
    val returnValue: String = ""
)