package yousang.rest.adapter

import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import yousang.rest.domain.lotto.AnnuityLottoEntity
import yousang.rest.domain.lotto.AnnuityLottoRepository
import yousang.rest.domain.lotto.LottoEntity
import yousang.rest.domain.lotto.LottoRepository
import yousang.rest.interfaces.lotto.AnnuityLottoDto
import yousang.rest.interfaces.lotto.LottoDto
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

/**
 * 테스트용 Mock 로또 리포지토리 구현
 * 외부 API나 데이터베이스 대신 메모리에 고정된 테스트 데이터를 제공합니다.
 */
@Component
@Profile("test") // 테스트 프로파일에서만 사용
@Primary // 다른 구현체보다 우선 적용
class MockLotteryAdapter : LottoRepository, AnnuityLottoRepository {
    
    private val logger = LoggerFactory.getLogger(MockLotteryAdapter::class.java)
    private val lottoCache = ConcurrentHashMap<Int, LottoDto>()
    private val annuityLottoCache = ConcurrentHashMap<Int, AnnuityLottoDto>()
    private val latestLottoRound = 1001
    private val latestAnnuityLottoRound = 101
    
    init {
        // 테스트 데이터 초기화
        val mockLotto1 = LottoDto(
            id = 1000,
            drwNo = 1000, 
            drwNoDate = LocalDate.now().minusDays(7),
            drwtNo1 = 1,
            drwtNo2 = 2,
            drwtNo3 = 3,
            drwtNo4 = 4,
            drwtNo5 = 5,
            drwtNo6 = 6,
            bnusNo = 7,
            firstPrzwnerCo = 2,
            firstAccumamnt = 2000000000,
            firstWinamnt = 1000000000,
            totSellamnt = 5000000000,
            returnValue = "success"
        )
        
        val mockLotto2 = LottoDto(
            id = 1001,
            drwNo = 1001, 
            drwNoDate = LocalDate.now(),
            drwtNo1 = 7,
            drwtNo2 = 14,
            drwtNo3 = 19,
            drwtNo4 = 28,
            drwtNo5 = 36,
            drwtNo6 = 41,
            bnusNo = 42,
            firstPrzwnerCo = 3,
            firstAccumamnt = 1500000000,
            firstWinamnt = 500000000,
            totSellamnt = 4000000000,
            returnValue = "success"
        )
        
        lottoCache[1000] = mockLotto1
        lottoCache[1001] = mockLotto2
        
        // 연금복권 테스트 데이터 초기화
        val mockAnnuityLotto = AnnuityLottoDto(
            id = 100,
            drwNo = 100,
            drwNoDate = LocalDate.now().minusDays(7),
            groupNumber = 3,
            drwtNo1 = 1,
            drwtNo2 = 2,
            drwtNo3 = 3,
            drwtNo4 = 4,
            drwtNo5 = 5,
            drwtNo6 = 6,
            bonusNo1 = 7,
            bonusNo2 = 8,
            bonusNo3 = 9,
            bonusNo4 = 10,
            bonusNo5 = 11,
            bonusNo6 = 12
        )
        
        annuityLottoCache[100] = mockAnnuityLotto
    }
    
    // LottoRepository 구현
    override suspend fun getLatestLotto(): LottoEntity? {
        val dto = lottoCache[latestLottoRound]
        return if (dto != null) mockLottoEntityFromDto(dto) else null
    }
    
    override suspend fun getLotto(firstDrwNo: Int, lastDrwNo: Int): List<LottoEntity> {
        val result = mutableListOf<LottoEntity>()
        for (drwNo in firstDrwNo..lastDrwNo) {
            lottoCache[drwNo]?.let { 
                val entity = mockLottoEntityFromDto(it)
                if (entity != null) {
                    result.add(entity)
                }
            }
        }
        return result
    }
    
    override suspend fun save(lottoList: List<LottoEntity>) {
        logger.info("Mocked saving ${lottoList.size} lotto entities")
    }
    
    // AnnuityLottoRepository 구현
    override suspend fun getLatestAnnuityLotto(): AnnuityLottoEntity? {
        val dto = annuityLottoCache[latestAnnuityLottoRound]
        return if (dto != null) mockAnnuityLottoEntityFromDto(dto) else null
    }
    
    override suspend fun getAnnuityLotto(firstDrwNo: Int, lastDrwNo: Int): List<AnnuityLottoEntity> {
        val result = mutableListOf<AnnuityLottoEntity>()
        for (drwNo in firstDrwNo..lastDrwNo) {
            annuityLottoCache[drwNo]?.let {
                val entity = mockAnnuityLottoEntityFromDto(it)
                if (entity != null) {
                    result.add(entity)
                }
            }
        }
        return result
    }
    
    override suspend fun save(annuityLottoList: List<AnnuityLottoEntity>) {
        logger.info("Mocked saving ${annuityLottoList.size} annuity lotto entities")
    }
    
    // Helper methods
    private fun mockLottoEntityFromDto(dto: LottoDto?): LottoEntity? {
        if (dto == null) return null
        
        // 테스트용 목(mock) 엔티티 생성 - 실제 데이터베이스 작업 없이 테스트
        try {
            return LottoEntity.new {
                drwNo = dto.drwNo
                drwNoDate = dto.drwNoDate.toKotlinLocalDate()
                drwtNo1 = dto.drwtNo1
                drwtNo2 = dto.drwtNo2
                drwtNo3 = dto.drwtNo3
                drwtNo4 = dto.drwtNo4
                drwtNo5 = dto.drwtNo5
                drwtNo6 = dto.drwtNo6
                bnusNo = dto.bnusNo
                firstPrzwnerCo = dto.firstPrzwnerCo
                firstAccumamnt = dto.firstAccumamnt
                firstWinamnt = dto.firstWinamnt
                totSellamnt = dto.totSellamnt
            }
        } catch (e: Exception) {
            // 테스트 환경에서 예외 발생 시 더미 객체 생성
            logger.warn("Error creating mock LottoEntity: ${e.message}")
            return null
        }
    }
    
    private fun mockAnnuityLottoEntityFromDto(dto: AnnuityLottoDto?): AnnuityLottoEntity? {
        if (dto == null) return null
        
        // 테스트용 목(mock) 엔티티 생성 - 실제 데이터베이스 작업 없이 테스트
        try {
            return AnnuityLottoEntity.new {
                drwNo = dto.drwNo
                drwNoDate = dto.drwNoDate.toKotlinLocalDate()
                groupNumber = dto.groupNumber
                drwtNo1 = dto.drwtNo1
                drwtNo2 = dto.drwtNo2
                drwtNo3 = dto.drwtNo3
                drwtNo4 = dto.drwtNo4
                drwtNo5 = dto.drwtNo5
                drwtNo6 = dto.drwtNo6
                bonusNo1 = dto.bonusNo1
                bonusNo2 = dto.bonusNo2
                bonusNo3 = dto.bonusNo3
                bonusNo4 = dto.bonusNo4
                bonusNo5 = dto.bonusNo5
                bonusNo6 = dto.bonusNo6
            }
        } catch (e: Exception) {
            // 테스트 환경에서 예외 발생 시 더미 객체 생성
            logger.warn("Error creating mock AnnuityLottoEntity: ${e.message}")
            return null
        }
    }
} 