package yousang.rest.domain.lotto

import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date

import yousang.rest.interfaces.lotto.LottoDto

object LottoTable : LongIdTable("lotto") {
    val drwNo = integer("drwNo").uniqueIndex()
    val drwNoDate = date("drwNoDate")
    val drwtNo1 = integer("drwtNo1")
    val drwtNo2 = integer("drwtNo2")
    val drwtNo3 = integer("drwtNo3")
    val drwtNo4 = integer("drwtNo4")
    val drwtNo5 = integer("drwtNo5")
    val drwtNo6 = integer("drwtNo6")
    val bnusNo = integer("bnusNo")
    val firstPrzwnerCo = integer("firstPrzwnerCo")
    val firstAccumamnt = long("firstAccumamnt")
    val firstWinamnt = long("firstWinamnt")
    val totSellamnt = long("totSellamnt")
}

class LottoEntity(id: EntityID<Long>) : LongEntity(id) {
    var drwNo by LottoTable.drwNo
    var drwNoDate by LottoTable.drwNoDate
    var drwtNo1 by LottoTable.drwtNo1
    var drwtNo2 by LottoTable.drwtNo2
    var drwtNo3 by LottoTable.drwtNo3
    var drwtNo4 by LottoTable.drwtNo4
    var drwtNo5 by LottoTable.drwtNo5
    var drwtNo6 by LottoTable.drwtNo6
    var bnusNo by LottoTable.bnusNo
    var firstPrzwnerCo by LottoTable.firstPrzwnerCo
    var firstAccumamnt by LottoTable.firstAccumamnt
    var firstWinamnt by LottoTable.firstWinamnt
    var totSellamnt by LottoTable.totSellamnt

    companion object : LongEntityClass<LottoEntity>(LottoTable) {
        fun LottoEntity.applyFromDto(dto: LottoDto) {
            this.drwNo = dto.drwNo
            this.drwNoDate = dto.drwNoDate.toKotlinLocalDate()
            this.drwtNo1 = dto.drwtNo1
            this.drwtNo2 = dto.drwtNo2
            this.drwtNo3 = dto.drwtNo3
            this.drwtNo4 = dto.drwtNo4
            this.drwtNo5 = dto.drwtNo5
            this.drwtNo6 = dto.drwtNo6
            this.bnusNo = dto.bnusNo
            this.firstPrzwnerCo = dto.firstPrzwnerCo
            this.firstAccumamnt = dto.firstAccumamnt
            this.firstWinamnt = dto.firstWinamnt
            this.totSellamnt = dto.totSellamnt
        }
    }


    fun toDto(): LottoDto {
        return LottoDto(
            id = id.value,
            drwNo = drwNo,
            drwNoDate = drwNoDate.toJavaLocalDate(),
            drwtNo1 = drwtNo1,
            drwtNo2 = drwtNo2,
            drwtNo3 = drwtNo3,
            drwtNo4 = drwtNo4,
            drwtNo5 = drwtNo5,
            drwtNo6 = drwtNo6,
            bnusNo = bnusNo,
            firstPrzwnerCo = firstPrzwnerCo,
            firstAccumamnt = firstAccumamnt,
            firstWinamnt = firstWinamnt,
            totSellamnt = totSellamnt,
            returnValue = "",
        )
    }
}
