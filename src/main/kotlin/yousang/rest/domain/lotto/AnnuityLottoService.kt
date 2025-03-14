package yousang.rest.domain.lotto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.toKotlinLocalDate
import org.jsoup.Jsoup
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import yousang.rest.interfaces.lotto.AnnuityLottoDto
import yousang.rest.shared.Constants
import yousang.rest.shared.coroutine.dbQuery
import yousang.rest.shared.exception.AnnuityLottoServiceException
import yousang.rest.shared.log.log
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 연금복권 정보 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
class AnnuityLottoService(
    private val annuityLottoRepository: AnnuityLottoRepository,
) {

    /**
     * 가장 최신 연금복권 번호를 조회합니다.
     *
     * @return 가장 최근 회차의 연금복권 정보, 없을 경우 null
     */
    private suspend fun getLatestAnnuityLotto(): AnnuityLottoEntity? {
        return try {
            annuityLottoRepository.getLatestAnnuityLotto()
        } catch (e: Exception) {
            log.error("Failed to retrieve latest lotto numbers: ${e.message}", e)
            null
        }
    }

    /**
     * 외부 웹사이트에서 연금복권 번호를 가져옵니다.
     *
     * @param drwNo 회차 번호
     * @return 연금복권 데이터 또는 Mock 데이터
     */
    private suspend fun getAnnuityLottoFromWeb(drwNo: Int): AnnuityLottoDto? {
        return try {
            // 타임아웃 설정 - 30초로 증가
            withTimeout(30000L) {
                val response = Jsoup.connect(Constants.ANNUITY_LOTTO_URL)
                    .data("Round", drwNo.toString())
                    .timeout(30000) // Jsoup 자체 타임아웃 30초로 증가
                    .post()

                val winResultTag = response.selectFirst("div.win_result")

                val roundNumText = winResultTag?.selectFirst("strong")?.text()?.replace("|", "")?.replace("회", "")
                val roundNumQuery = roundNumText?.toInt()

                val drawDateText = winResultTag?.selectFirst("p.desc")?.text()
                val cleanedDateText =
                    drawDateText?.replace("년", "-")?.replace("월", "-")?.replace("일 추첨", "")?.replace("(", "")
                        ?.replace(")", "")?.replace(" ", "")?.trim()
                val drawDate = cleanedDateText?.let { LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd")) }

                val win720NumTags = winResultTag?.select("div.win720_num")

                val divGroup = win720NumTags?.get(0)?.selectFirst("div.group")
                val numGroup = divGroup?.select("span")?.get(1)?.text()?.toInt()

                val win720Nums = mutableListOf<Int>()
                for (i in 1..6) {
                    val num = win720NumTags?.get(0)?.selectFirst("span.num.al720_color${i}.large span")?.text()?.toInt()
                    if (num != null) {
                        win720Nums.add(num)
                    }
                }

                val bonusNums = mutableListOf<Int>()
                for (i in 1..6) {
                    val num = win720NumTags?.get(1)?.selectFirst("span.num.al720_color${i}.large span")?.text()?.toInt()
                    if (num != null) {
                        bonusNums.add(num)
                    }
                }

                AnnuityLottoDto(
                    drwNo = roundNumQuery ?: drwNo,
                    drwNoDate = drawDate!!,
                    groupNumber = numGroup ?: 0,
                    drwtNo1 = win720Nums[0],
                    drwtNo2 = win720Nums[1],
                    drwtNo3 = win720Nums[2],
                    drwtNo4 = win720Nums[3],
                    drwtNo5 = win720Nums[4],
                    drwtNo6 = win720Nums[5],
                    bonusNo1 = bonusNums[0],
                    bonusNo2 = bonusNums[1],
                    bonusNo3 = bonusNums[2],
                    bonusNo4 = bonusNums[3],
                    bonusNo5 = bonusNums[4],
                    bonusNo6 = bonusNums[5]
                )
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch AnnuityLotto from web for draw number $drwNo: ${e.message}", e)
            null // 호출자에게 null 반환, 호출자가 적절히 처리하도록 수정
        }
    }

    /**
     * 지정된 작업을 재시도하는 헬퍼 함수
     */
    private suspend fun <T> retry(
        maxAttempts: Int,
        initialDelayMs: Long,
        factor: Double = 2.0,
        block: suspend () -> T?
    ): T? {
        var currentDelay = initialDelayMs
        
        repeat(maxAttempts) { attempt ->
            try {
                val result = block()
                if (result != null) return result
                
                // null 결과는 재시도 대상
                if (attempt == maxAttempts - 1) return null
                
                log.warn("Attempt ${attempt + 1}/$maxAttempts returned null, retrying in $currentDelay ms")
            } catch (e: Exception) {
                if (attempt == maxAttempts - 1) {
                    log.error("All $maxAttempts retry attempts failed", e)
                    return null
                }
                
                log.warn("Retry attempt ${attempt + 1}/$maxAttempts failed: ${e.message}, retrying in $currentDelay ms")
            }
            
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong()
        }
        
        return null
    }

    /**
     * 외부 API를 통해 연금복권 번호를 조회합니다.
     * 순차적 처리와 재시도 로직으로 안정성 향상
     *
     * @param firstDrwNo 시작 회차 번호
     * @param lastDrwNo 끝 회차 번호
     * @return 외부 API로부터 가져온 연금복권 정보 목록
     */
    private suspend fun getAnnuityLottoFromAPI(firstDrwNo: Int, lastDrwNo: Int): List<AnnuityLottoDto> =
        withContext(Dispatchers.IO) {
            val result = mutableListOf<AnnuityLottoDto>()
            
            log.info("Fetching annuity lotto numbers from draw $firstDrwNo to $lastDrwNo")
            
            for (drwNo in firstDrwNo..lastDrwNo) {
                try {
                    // 재시도 로직으로 안정성 향상
                    val lottoData = retry(maxAttempts = 3, initialDelayMs = 2000) {
                        getAnnuityLottoFromWeb(drwNo)
                    }
                    
                    if (lottoData != null) {
                        result.add(lottoData)
                        log.debug("Successfully fetched annuity lotto data for draw $drwNo")
                    } else {
                        log.warn("Failed to fetch data for annuity lotto draw $drwNo after retries")
                    }
                    
                    // 요청 사이 지연을 추가하여 서버 부하 감소 (2초로 증가)
//                    delay(2000)
                } catch (e: Exception) {
                    log.error("Error fetching annuity lotto draw number $drwNo: ${e.message}", e)
                    // 오류가 발생해도 계속 진행
                }
            }
            
            log.info("Successfully fetched ${result.size} annuity lotto numbers")
            result
        }

    suspend fun getAnnuityLotto(firstDrwNo: Int, lastDrwNo: Int): List<AnnuityLottoDto> {
        return try {
            annuityLottoRepository.getAnnuityLotto(firstDrwNo, lastDrwNo).map { it.toDto() }
        } catch (e: Exception) {
            log.error("Failed to retrieve lotto numbers between $firstDrwNo and $lastDrwNo: ${e.message}")
            throw AnnuityLottoServiceException(
                "Failed to retrieve lotto numbers between $firstDrwNo and $lastDrwNo", e.cause
            )
        }
    }

    /**
     * 연금복권 번호를 저장합니다.
     *
     * @throws AnnuityLottoServiceException 저장 실패 시 예외 발생
     */
    suspend fun putAnnuityLotto() {
        try {
            val latestLotto = getLatestAnnuityLotto()
            val effectiveFirstDrwNo = latestLotto?.drwNo?.plus(1) ?: 1
            val currentDate = LocalDate.now(ZoneId.of("Asia/Seoul"))
            val effectiveLastDrwNo =
                (ChronoUnit.DAYS.between(Constants.ANNUITY_LOTTO_FIRST_DATE, currentDate) / 7 + 1).toInt()

            val annuityLottoList = getAnnuityLottoFromAPI(effectiveFirstDrwNo, effectiveLastDrwNo)
            
            if (annuityLottoList.isEmpty()) {
                log.info("No new annuity lotto numbers to save")
                return
            }

            // 모든 데이터베이스 작업을 하나의 transaction 내에서 처리
            dbQuery {
                for (dto in annuityLottoList) {
                    try {
                        // 기존 엔티티 찾기
                        val existingEntity = AnnuityLottoEntity.find { 
                            AnnuityLottoTable.drwNo eq dto.drwNo 
                        }.firstOrNull()

                        if (existingEntity != null) {
                            // 기존 엔티티 업데이트
                            existingEntity.apply {
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
                            log.debug("Updated existing annuity lotto entity for draw number ${dto.drwNo}")
                        } else {
                            // 새 엔티티 생성
                            AnnuityLottoEntity.new {
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
                            log.debug("Created new annuity lotto entity for draw number ${dto.drwNo}")
                        }
                    } catch (e: Exception) {
                        log.error("Error processing annuity lotto for draw number ${dto.drwNo}: ${e.message}", e)
                        // 특정 항목이 실패해도 나머지 항목들은 처리를 계속함
                    }
                }
            }
        } catch (e: Exception) {
            log.error("Failed to save lotto numbers: ${e.message}", e)
            throw AnnuityLottoServiceException("Failed to save lotto numbers", e.cause)
        }
    }
}