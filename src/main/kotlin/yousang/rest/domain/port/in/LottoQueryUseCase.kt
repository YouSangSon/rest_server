package yousang.rest.domain.port.`in`

import yousang.rest.domain.model.Lotto

/**
 * Input port for retrieving Lotto 6/45 data.
 */
interface LottoQueryUseCase {
    /**
     * Get Lotto 6/45 data for a specific round.
     */
    fun getLottoByRound(round: Int): Lotto?
    
    /**
     * Get Lotto 6/45 data for the latest round.
     */
    fun getLatestLotto(): Lotto
    
    /**
     * Get Lotto 6/45 data for a range of rounds.
     */
    fun getLottoByRoundRange(query: LottoRangeQuery): List<Lotto>
    
    /**
     * Get all Lotto 6/45 data from the first round.
     */
    fun getAllLottoRounds(): List<Lotto>
    
    data class LottoRangeQuery(
        val fromRound: Int,
        val toRound: Int
    ) {
        init {
            require(fromRound > 0) { "From round must be positive" }
            require(toRound >= fromRound) { "To round must be greater than or equal to from round" }
        }
    }
} 