package yousang.rest.adapter.`in`.web.dto

import yousang.rest.domain.model.Lotto
import yousang.rest.domain.model.PensionLottery
import java.time.LocalDate

data class LottoResponseDto(
    val round: Int,
    val numbers: List<Int>,
    val bonusNumber: Int,
    val drawDate: LocalDate,
    val firstPrizeAmount: Long,
    val firstPrizeWinners: Int
) {
    companion object {
        fun fromDomain(lotto: Lotto): LottoResponseDto {
            return LottoResponseDto(
                round = lotto.round,
                numbers = lotto.numbers,
                bonusNumber = lotto.bonusNumber,
                drawDate = lotto.drawDate,
                firstPrizeAmount = lotto.firstPrizeAmount,
                firstPrizeWinners = lotto.firstPrizeWinners
            )
        }
    }
}

data class PensionLotteryResponseDto(
    val round: Int,
    val group: Int,
    val numbers: List<Int>,
    val drawDate: LocalDate,
    val firstPrizeAmount: Long,
    val firstPrizeWinners: Int
) {
    companion object {
        fun fromDomain(pensionLottery: PensionLottery): PensionLotteryResponseDto {
            return PensionLotteryResponseDto(
                round = pensionLottery.round,
                group = pensionLottery.group,
                numbers = pensionLottery.numbers,
                drawDate = pensionLottery.drawDate,
                firstPrizeAmount = pensionLottery.firstPrizeAmount,
                firstPrizeWinners = pensionLottery.firstPrizeWinners
            )
        }
    }
} 