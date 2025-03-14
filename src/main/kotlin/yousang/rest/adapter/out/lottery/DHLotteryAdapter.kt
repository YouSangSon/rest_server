package yousang.rest.adapter.out.lottery

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import yousang.rest.adapter.out.lottery.dto.LottoResponseDto
import yousang.rest.domain.model.Lotto
import yousang.rest.domain.model.PensionLottery
import yousang.rest.domain.port.out.LotteryDataProvider
import java.util.concurrent.ConcurrentHashMap

@Component
class DHLotteryAdapter(
    private val restTemplate: RestTemplate
) : LotteryDataProvider {
    
    private val logger = LoggerFactory.getLogger(DHLotteryAdapter::class.java)
    private val lottoCache = ConcurrentHashMap<Int, Lotto>()
    private val pensionLotteryCache = ConcurrentHashMap<Int, PensionLottery>()
    private var latestLottoRound: Int? = null
    private var latestPensionLotteryRound: Int? = null
    
    companion object {
        private const val LOTTO_API_URL = "https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo={round}"
    }
    
    @Cacheable("lotto-round")
    override fun getLottoByRound(round: Int): Lotto? {
        if (round <= 0) {
            throw IllegalArgumentException("Round must be positive")
        }
        
        return lottoCache.computeIfAbsent(round) {
            try {
                val response = restTemplate.getForObject(
                    LOTTO_API_URL, 
                    LottoResponseDto::class.java,
                    mapOf("round" to round)
                )
                
                response?.let { 
                    Lotto.create(
                        round = it.drawNo,
                        numbers = it.getNumbers(),
                        bonusNumber = it.bonusNo,
                        drawDate = it.getDrawDate(),
                        firstPrizeAmount = it.firstPrizeAmount,
                        firstPrizeWinners = it.firstPrizeWinners
                    )
                } ?: throw RuntimeException("Failed to fetch lotto data for round $round")
            } catch (e: Exception) {
                logger.error("Error fetching lotto data for round $round", e)
                throw RuntimeException("Failed to fetch lotto data for round $round", e)
            }
        }
    }
    
    @Cacheable("latest-lotto")
    override fun getLatestLotto(): Lotto {
        if (latestLottoRound == null) {
            findLatestLottoRound()
        }
        
        return getLottoByRound(latestLottoRound!!)
            ?: throw RuntimeException("Failed to fetch latest lotto data")
    }
    
    override fun getLottoByRoundRange(fromRound: Int, toRound: Int): List<Lotto> {
        return (fromRound..toRound).mapNotNull { getLottoByRound(it) }
    }
    
    override fun getAllLottoRounds(): List<Lotto> {
        if (latestLottoRound == null) {
            findLatestLottoRound()
        }
        
        return getLottoByRoundRange(1, latestLottoRound!!)
    }
    
    // Note: Methods below are placeholders and need actual implementation based on API
    // as the pension lottery endpoints are not documented
    
    override fun getPensionLotteryByRound(round: Int): PensionLottery? {
        // Placeholder implementation
        throw NotImplementedError("Pension lottery API not implemented")
    }
    
    override fun getLatestPensionLottery(): PensionLottery {
        // Placeholder implementation
        throw NotImplementedError("Pension lottery API not implemented")
    }
    
    override fun getPensionLotteryByRoundRange(fromRound: Int, toRound: Int): List<PensionLottery> {
        // Placeholder implementation
        throw NotImplementedError("Pension lottery API not implemented")
    }
    
    override fun getAllPensionLotteryRounds(): List<PensionLottery> {
        // Placeholder implementation
        throw NotImplementedError("Pension lottery API not implemented")
    }
    
    private fun findLatestLottoRound() {
        // Binary search to find the latest round
        // Starting from an estimated high value and working backwards
        var low = 1
        var high = 1500 // Arbitrary high value that should exceed the latest round
        
        while (low <= high) {
            val mid = (low + high) / 2
            try {
                val lotto = getLottoByRound(mid)
                if (lotto != null) {
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            } catch (e: Exception) {
                high = mid - 1
            }
        }
        
        latestLottoRound = high
        logger.info("Latest lotto round found: $latestLottoRound")
    }
} 