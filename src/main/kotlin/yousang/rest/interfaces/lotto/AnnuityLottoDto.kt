package yousang.rest.interfaces.lotto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import yousang.rest.domain.lotto.AnnuityLottoEntity
import yousang.rest.domain.lotto.AnnuityLottoEntity.Companion.applyFromDto
import yousang.rest.shared.log.log
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnnuityLottoDto(
    @JsonProperty("id")
    val id: Long? = null,

    @JsonProperty("drwNo")
    val drwNo: Int,

    @JsonProperty("drwNoDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val drwNoDate: LocalDate,

    @JsonProperty("groupNumber")
    val groupNumber: Int,

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

    @JsonProperty("bonusNo1")
    val bonusNo1: Int,

    @JsonProperty("bonusNo2")
    val bonusNo2: Int,

    @JsonProperty("bonusNo3")
    val bonusNo3: Int,

    @JsonProperty("bonusNo4")
    val bonusNo4: Int,

    @JsonProperty("bonusNo5")
    val bonusNo5: Int,

    @JsonProperty("bonusNo6")
    val bonusNo6: Int,
) {
    /**
     * DTO 데이터를 사용하여 새 엔티티 생성
     * 데이터베이스 조회 로직을 서비스 레이어로 이동
     */
    fun toEntity(): AnnuityLottoEntity {
        return AnnuityLottoEntity.new {
            this.drwNo = this@AnnuityLottoDto.drwNo
            applyFromDto(this@AnnuityLottoDto)
        }.also { log.debug("Created new annuity lotto entity with draw number $drwNo") }
    }

    /**
     * DTO 데이터를 기존 엔티티에 적용
     */
    fun updateEntity(entity: AnnuityLottoEntity): AnnuityLottoEntity {
        entity.applyFromDto(this)
        return entity
    }
}