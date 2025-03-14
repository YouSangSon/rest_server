package yousang.rest.domain.model

import java.time.LocalDate

/**
 * Pension Lottery (연금복권) domain entity
 */
class PensionLottery private constructor(
    val round: Int,
    val group: Int,
    val numbers: List<Int>,
    val drawDate: LocalDate,
    val firstPrizeAmount: Long,
    val firstPrizeWinners: Int
) {
    companion object {
        fun create(
            round: Int,
            group: Int,
            numbers: List<Int>,
            drawDate: LocalDate,
            firstPrizeAmount: Long,
            firstPrizeWinners: Int
        ): PensionLottery {
            require(numbers.size == 6) { "Pension lottery must have exactly 6 numbers" }
            require(numbers.all { it in 0..9 }) { "Pension lottery numbers must be between 0 and 9" }
            require(group in 1..5) { "Group must be between 1 and 5" }
            
            return PensionLottery(
                round = round,
                group = group,
                numbers = numbers,
                drawDate = drawDate,
                firstPrizeAmount = firstPrizeAmount,
                firstPrizeWinners = firstPrizeWinners
            )
        }
    }
    
    override fun toString(): String {
        return "PensionLottery(round=$round, group=$group, numbers=$numbers, drawDate=$drawDate)"
    }
} 