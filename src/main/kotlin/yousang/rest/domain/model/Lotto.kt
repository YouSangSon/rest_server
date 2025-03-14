package yousang.rest.domain.model

import java.time.LocalDate

/**
 * Lotto 6/45 domain entity
 */
class Lotto private constructor(
    val round: Int,
    val numbers: List<Int>,
    val bonusNumber: Int,
    val drawDate: LocalDate,
    val firstPrizeAmount: Long,
    val firstPrizeWinners: Int
) {
    companion object {
        fun create(
            round: Int,
            numbers: List<Int>,
            bonusNumber: Int,
            drawDate: LocalDate,
            firstPrizeAmount: Long,
            firstPrizeWinners: Int
        ): Lotto {
            require(numbers.size == 6) { "Lotto must have exactly 6 numbers" }
            require(numbers.all { it in 1..45 }) { "Lotto numbers must be between 1 and 45" }
            require(bonusNumber in 1..45) { "Bonus number must be between 1 and 45" }
            require(!numbers.contains(bonusNumber)) { "Bonus number must not be in the main numbers" }
            
            return Lotto(
                round = round,
                numbers = numbers.sorted(),
                bonusNumber = bonusNumber,
                drawDate = drawDate,
                firstPrizeAmount = firstPrizeAmount,
                firstPrizeWinners = firstPrizeWinners
            )
        }
    }
    
    override fun toString(): String {
        return "Lotto(round=$round, numbers=$numbers, bonusNumber=$bonusNumber, drawDate=$drawDate)"
    }
} 