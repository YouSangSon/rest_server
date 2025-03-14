package yousang.rest.interfaces.lotto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.exposed.sql.transactions.transaction
import yousang.rest.domain.lotto.LottoEntity
import yousang.rest.domain.lotto.LottoEntity.Companion.applyFromDto
import yousang.rest.domain.lotto.LottoTable
import yousang.rest.shared.log.log
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class LottoDto(
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
    val firstPrzwnerCo: Int,

    @JsonProperty("firstAccumamnt")
    val firstAccumamnt: Long,

    @JsonProperty("firstWinamnt")
    val firstWinamnt: Long,

    @JsonProperty("totSellamnt")
    val totSellamnt: Long,

    @JsonProperty("returnValue")
    val returnValue: String = "",
) {
    /**
     * DTO 데이터를 사용하여 새 엔티티 생성
     * 데이터베이스 조회 로직을 서비스 레이어로 이동
     */
    fun toEntity(): LottoEntity {
        return LottoEntity.new {
            this.drwNo = this@LottoDto.drwNo
            applyFromDto(this@LottoDto)
        }.also { log.debug("Created new lotto entity with draw number $drwNo") }
    }

    /**
     * DTO 데이터를 기존 엔티티에 적용
     */
    fun updateEntity(entity: LottoEntity): LottoEntity {
        entity.applyFromDto(this)
        return entity
    }
}