package yousang.rest.domain.port.out

import yousang.rest.domain.model.Lotto
import yousang.rest.domain.model.PensionLottery

/**
 * Output port for retrieving lottery data from external sources.
 */
interface LotteryDataProvider {
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
    fun getLottoByRoundRange(fromRound: Int, toRound: Int): List<Lotto>
    
    /**
     * Get all Lotto 6/45 data from the first round.
     */
    fun getAllLottoRounds(): List<Lotto>
    
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
    fun getPensionLotteryByRoundRange(fromRound: Int, toRound: Int): List<PensionLottery>
    
    /**
     * Get all Pension Lottery data from the first round.
     */
    fun getAllPensionLotteryRounds(): List<PensionLottery>
} 