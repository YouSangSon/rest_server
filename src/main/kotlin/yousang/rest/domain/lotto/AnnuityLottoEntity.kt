package yousang.rest.domain.lotto

import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import yousang.rest.interfaces.lotto.AnnuityLottoDto

object AnnuityLottoTable : LongIdTable("annuity_lotto") {
    val drwNo = integer("drwNo").uniqueIndex()
    val drwNoDate = date("drwNoDate")
    val groupNumber = integer("groupNumber")
    val drwtNo1 = integer("drwtNo1")
    val drwtNo2 = integer("drwtNo2")
    val drwtNo3 = integer("drwtNo3")
    val drwtNo4 = integer("drwtNo4")
    val drwtNo5 = integer("drwtNo5")
    val drwtNo6 = integer("drwtNo6")
    val bonusNo1 = integer("bonusNo1")
    val bonusNo2 = integer("bonusNo2")
    val bonusNo3 = integer("bonusNo3")
    val bonusNo4 = integer("bonusNo4")
    val bonusNo5 = integer("bonusNo5")
    val bonusNo6 = integer("bonusNo6")
}

class AnnuityLottoEntity(id: EntityID<Long>) : LongEntity(id) {
    var drwNo by AnnuityLottoTable.drwNo
    var drwNoDate by AnnuityLottoTable.drwNoDate
    var groupNumber by AnnuityLottoTable.groupNumber
    var drwtNo1 by AnnuityLottoTable.drwtNo1
    var drwtNo2 by AnnuityLottoTable.drwtNo2
    var drwtNo3 by AnnuityLottoTable.drwtNo3
    var drwtNo4 by AnnuityLottoTable.drwtNo4
    var drwtNo5 by AnnuityLottoTable.drwtNo5
    var drwtNo6 by AnnuityLottoTable.drwtNo6
    var bonusNo1 by AnnuityLottoTable.bonusNo1
    var bonusNo2 by AnnuityLottoTable.bonusNo2
    var bonusNo3 by AnnuityLottoTable.bonusNo3
    var bonusNo4 by AnnuityLottoTable.bonusNo4
    var bonusNo5 by AnnuityLottoTable.bonusNo5
    var bonusNo6 by AnnuityLottoTable.bonusNo6

    companion object : LongEntityClass<AnnuityLottoEntity>(AnnuityLottoTable) {
        fun AnnuityLottoEntity.applyFromDto(dto: AnnuityLottoDto) {
            this.drwNo = dto.drwNo
            this.drwNoDate = dto.drwNoDate.toKotlinLocalDate()
            this.groupNumber = dto.groupNumber
            this.drwtNo1 = dto.drwtNo1
            this.drwtNo2 = dto.drwtNo2
            this.drwtNo3 = dto.drwtNo3
            this.drwtNo4 = dto.drwtNo4
            this.drwtNo5 = dto.drwtNo5
            this.drwtNo6 = dto.drwtNo6
            this.bonusNo1 = dto.bonusNo1
            this.bonusNo2 = dto.bonusNo2
            this.bonusNo3 = dto.bonusNo3
            this.bonusNo4 = dto.bonusNo4
            this.bonusNo5 = dto.bonusNo5
            this.bonusNo6 = dto.bonusNo6
        }
    }


    fun toDto(): AnnuityLottoDto {
        return AnnuityLottoDto(
            id = id.value,
            drwNo = drwNo,
            drwNoDate = drwNoDate.toJavaLocalDate(),
            groupNumber = groupNumber,
            drwtNo1 = drwtNo1,
            drwtNo2 = drwtNo2,
            drwtNo3 = drwtNo3,
            drwtNo4 = drwtNo4,
            drwtNo5 = drwtNo5,
            drwtNo6 = drwtNo6,
            bonusNo1 = bonusNo1,
            bonusNo2 = bonusNo2,
            bonusNo3 = bonusNo3,
            bonusNo4 = bonusNo4,
            bonusNo5 = bonusNo5,
            bonusNo6 = bonusNo6,
        )
    }
}