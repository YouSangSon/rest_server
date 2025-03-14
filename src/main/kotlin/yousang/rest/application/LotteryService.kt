package yousang.rest.application

import org.springframework.stereotype.Service
import yousang.rest.domain.model.Lotto
import yousang.rest.domain.model.PensionLottery
import yousang.rest.domain.port.`in`.LottoQueryUseCase
import yousang.rest.domain.port.`in`.PensionLotteryQueryUseCase
import yousang.rest.domain.port.out.LotteryDataProvider

@Service
class LotteryService(
    private val lotteryDataProvider: LotteryDataProvider
) : LottoQueryUseCase, PensionLotteryQueryUseCase {
    
    override fun getLottoByRound(round: Int): Lotto? {
        return lotteryDataProvider.getLottoByRound(round)
    }
    
    override fun getLatestLotto(): Lotto {
        return lotteryDataProvider.getLatestLotto()
    }
    
    override fun getLottoByRoundRange(query: LottoQueryUseCase.LottoRangeQuery): List<Lotto> {
        return lotteryDataProvider.getLottoByRoundRange(query.fromRound, query.toRound)
    }
    
    override fun getAllLottoRounds(): List<Lotto> {
        return lotteryDataProvider.getAllLottoRounds()
    }
    
    override fun getPensionLotteryByRound(round: Int): PensionLottery? {
        return lotteryDataProvider.getPensionLotteryByRound(round)
    }
    
    override fun getLatestPensionLottery(): PensionLottery {
        return lotteryDataProvider.getLatestPensionLottery()
    }
    
    override fun getPensionLotteryByRoundRange(query: PensionLotteryQueryUseCase.PensionLotteryRangeQuery): List<PensionLottery> {
        return lotteryDataProvider.getPensionLotteryByRoundRange(query.fromRound, query.toRound)
    }
    
    override fun getAllPensionLotteryRounds(): List<PensionLottery> {
        return lotteryDataProvider.getAllPensionLotteryRounds()
    }
} 