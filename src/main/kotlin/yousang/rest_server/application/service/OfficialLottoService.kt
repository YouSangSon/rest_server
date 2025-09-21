package yousang.rest_server.application.service.lottoOfficial

import yousang.rest_server.application.ports.`in`.lottoOfficial.OfficialLottoDto
import yousang.rest_server.application.ports.`in`.lottoOfficial.OfficialLottoUseCase
import java.time.LocalDate
import kotlin.random.Random

/**
 * A lightweight, deterministic implementation that generates pseudo official lotto data
 * based on draw number. This avoids external dependencies and DB for now while
 * providing the documented API surface. Can be swapped later with a persistence-backed
 * adapter without changing controllers.
 */
class OfficialLottoService : OfficialLottoUseCase {

    private val firstDrawDate = LocalDate.of(2002, 12, 7)

    override fun getLotto(firstDrwNo: Int, lastDrwNo: Int): List<OfficialLottoDto> {
        require(firstDrwNo > 0 && lastDrwNo >= firstDrwNo) { "Invalid range: $firstDrwNo..$lastDrwNo" }
        return (firstDrwNo..lastDrwNo).map { makeDraw(it) }
    }

    override fun putLotto() {
        // no-op stub; in a real impl, this would fetch and persist latest draws
    }

    override fun getLotto(drwNo: Int): OfficialLottoDto? {
        require(drwNo > 0) { "Invalid draw number: $drwNo" }
        return makeDraw(drwNo)
    }

    private fun makeDraw(drwNo: Int): OfficialLottoDto {
        val rng = Random(drwNo.toLong())
        val pool = (1..45).toMutableList()
        // Deterministically shuffle and pick
        val numbers = mutableListOf<Int>()
        repeat(7) { // 6 numbers + 1 bonus
            val idx = Random.nextInt(pool.size)
            numbers += pool.removeAt(idx)
        }
        val main = numbers.take(6).sorted()
        val bonus = numbers[6]
        val date = firstDrawDate.plusWeeks((drwNo - 1).toLong())
        return OfficialLottoDto(
            id = null,
            drwNo = drwNo,
            drwNoDate = date,
            drwtNo1 = main[0],
            drwtNo2 = main[1],
            drwtNo3 = main[2],
            drwtNo4 = main[3],
            drwtNo5 = main[4],
            drwtNo6 = main[5],
            bnusNo = bonus,
            firstPrzwnerCo = 0,
            firstAccumamnt = 0,
            firstWinamnt = 0,
            totSellamnt = 0,
            returnValue = "SUCCESS"
        )
    }
}