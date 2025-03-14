package yousang.rest.domain.port.`in`

import yousang.rest.domain.model.PensionLottery

/**
 * Input port for retrieving Pension Lottery data.
 */
interface PensionLotteryQueryUseCase {
    /**
     * Get Pension Lottery data for a specific round.
     */
    fun getPensionLotteryByRound(round: Int): PensionLottery?
    
    /**
     * Get Pension Lottery data for the latest round.
     */
    fun getLatestPensionLottery(): PensionLottery
    
    /**
     * Get Pension Lottery data for a range of rounds.
     */
    fun getPensionLotteryByRoundRange(query: PensionLotteryRangeQuery): List<PensionLottery>
    
    /**
     * Get all Pension Lottery data from the first round.
     */
    fun getAllPensionLotteryRounds(): List<PensionLottery>
    
    data class PensionLotteryRangeQuery(
        val fromRound: Int,
        val toRound: Int
    ) {
        init {
            require(fromRound > 0) { "From round must be positive" }
            require(toRound >= fromRound) { "To round must be greater than or equal to from round" }
        }
    }
} 