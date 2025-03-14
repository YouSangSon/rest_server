package yousang.rest.domain.lotto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import yousang.rest.interfaces.lotto.LottoDto
import yousang.rest.shared.Constants
import yousang.rest.shared.coroutine.dbQuery
import yousang.rest.shared.exception.DataAccessException
import yousang.rest.shared.exception.LottoServiceException
import yousang.rest.shared.log.LoggerDelegate
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * 로또 정보 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
class LottoService(
    private val webClientBuilder: WebClient.Builder,
    private val lottoRepository: LottoRepository,
    private val objectMapper: ObjectMapper
) {
    private val log by LoggerDelegate()

    // 커스텀 WebClient 설정
    private val webClient by lazy {
        // 최적화된 커넥션 풀 설정
        val connectionProvider = ConnectionProvider.builder("lotto-connection-pool")
            .maxConnections(10)  // 최대 동시 연결 수 제한
            .maxIdleTime(Duration.ofSeconds(60))  // 유휴 연결 최대 시간 증가
            .maxLifeTime(Duration.ofMinutes(10))  // 연결 수명 증가
            .build()

        // HttpClient 설정 (타임아웃 등)
        val httpClient = HttpClient.create(connectionProvider)
            .responseTimeout(Duration.ofSeconds(60))  // 응답 타임아웃 크게 증가
            .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)  // 연결 타임아웃 30초로 증가

        // 최종 WebClient 생성
        webClientBuilder
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl(Constants.LOTTO_URL)
            .build()
    }

    /**
     * 엔티티를 DTO로 변환하는 확장 함수
     */
    private fun LottoEntity.toDto() = LottoDto(
        id = id.value,
        drwNo = drwNo,
        drwNoDate = drwNoDate.toJavaLocalDate(),
        drwtNo1 = drwtNo1,
        drwtNo2 = drwtNo2,
        drwtNo3 = drwtNo3,
        drwtNo4 = drwtNo4,
        drwtNo5 = drwtNo5,
        drwtNo6 = drwtNo6,
        bnusNo = bnusNo,
        firstPrzwnerCo = firstPrzwnerCo,
        firstAccumamnt = firstAccumamnt,
        firstWinamnt = firstWinamnt,
        totSellamnt = totSellamnt,
        returnValue = ""
    )

    /**
     * 최신 로또 번호 정보를 가져옵니다.
     *
     * @return 최신 로또 DTO, 없을 경우 null
     */
    suspend fun getLatestLotto(): LottoDto? {
        return try {
            lottoRepository.getLatestLotto()?.toDto()
        } catch (e: DataAccessException) {
            log.error("Failed to retrieve latest lotto: ${e.message}")
            throw LottoServiceException("최신 로또 정보를 가져오는데 실패했습니다", e)
        } catch (e: Exception) {
            log.error("Unexpected error retrieving latest lotto: ${e.message}", e)
            throw LottoServiceException("최신 로또 정보를 가져오는 중 예상치 못한 오류가 발생했습니다", e)
        }
    }

    /**
     * 외부 API를 통해 로또 번호를 조회합니다.
     * 순차적 처리와 재시도 로직을 통해 안정성 향상
     *
     * @param firstDrwNo 시작 회차 번호
     * @param lastDrwNo 끝 회차 번호
     * @return 외부 API로부터 가져온 로또 정보 목록
     */
    private suspend fun getLottoFromWeb(firstDrwNo: Int, lastDrwNo: Int): List<LottoDto> = withContext(Dispatchers.IO) {
        val results = mutableListOf<LottoDto>()
        
        for (drwNo in firstDrwNo..lastDrwNo) {
            try {
                // 재시도 로직
                val result = retry(maxAttempts = 3, initialDelayMs = 2000) {
                    val responseJson = webClient.get()
                        .uri("/common.do?method=getLottoNumber&drwNo=$drwNo")
                        .retrieve()
                        .awaitBody<String>()
                    
                    objectMapper.readValue<LottoDto>(responseJson)
                }
                
                result?.let { results.add(it) }
            } catch (e: Exception) {
                log.error("Error fetching lotto draw number $drwNo: ${e.message}", e)
                // 오류가 발생해도 계속 진행
            }
        }
        
        results
    }
    
    /**
     * 지정된 작업을 재시도하는 헬퍼 함수
     */
    private suspend fun <T> retry(
        maxAttempts: Int,
        initialDelayMs: Long,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T? {
        var currentDelay = initialDelayMs
        
        repeat(maxAttempts) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                if (attempt == maxAttempts - 1) throw e
                
                log.warn("Retry attempt ${attempt + 1}/$maxAttempts failed, retrying in $currentDelay ms")
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong()
            }
        }
        
        return null
    }

    /**
     * 특정 회차 범위의 로또 번호를 조회합니다.
     *
     * @param firstDrwNo 시작 회차 번호
     * @param lastDrwNo 끝 회차 번호
     * @return 로또 DTO 목록
     * @throws LottoServiceException 조회 실패 시 예외 발생
     */
    suspend fun getLotto(firstDrwNo: Int, lastDrwNo: Int): List<LottoDto> {
        return try {
            lottoRepository.getLotto(firstDrwNo, lastDrwNo).map { it.toDto() }
        } catch (e: DataAccessException) {
            log.error("Failed to retrieve lotto numbers between $firstDrwNo and $lastDrwNo: ${e.message}")
            throw LottoServiceException(
                "회차 $firstDrwNo 부터 $lastDrwNo 까지의 로또 번호를 가져오는데 실패했습니다", e
            )
        } catch (e: Exception) {
            log.error("Unexpected error retrieving lotto numbers: ${e.message}", e)
            throw LottoServiceException("로또 번호를 가져오는 중 예상치 못한 오류가 발생했습니다", e)
        }
    }

    /**
     * 로또 번호를 저장합니다.
     * 외부 API에서 최신 로또 번호를 가져와 데이터베이스에 저장합니다.
     *
     * @throws LottoServiceException 저장 실패 시 예외 발생
     */
    suspend fun putLotto() {
        try {
            val latestLotto = getLatestLotto()
            val effectiveFirstDrwNo = latestLotto?.drwNo?.plus(1) ?: 1
            val currentDate = LocalDate.now(ZoneId.of("Asia/Seoul"))
            val effectiveLastDrwNo = (ChronoUnit.DAYS.between(Constants.LOTTO_FIRST_DATE, currentDate) / 7 + 1).toInt()

            log.info("Fetching lotto numbers from draw $effectiveFirstDrwNo to $effectiveLastDrwNo")
            val lottoList = getLottoFromWeb(effectiveFirstDrwNo, effectiveLastDrwNo)
            log.info("Successfully fetched ${lottoList.size} lotto numbers")

            if (lottoList.isEmpty()) {
                log.info("No new lotto numbers to save")
                return
            }

            // 모든 데이터베이스 작업을 하나의 transaction 내에서 처리
            dbQuery {
                for (dto in lottoList) {
                    try {
                        if (dto.returnValue != "success" && dto.returnValue.isNotEmpty()) {
                            log.warn("Skipping lotto draw ${dto.drwNo} with non-success return value: ${dto.returnValue}")
                            continue
                        }
                        
                        // 기존 엔티티 찾기
                        val existingEntity = LottoEntity.find { 
                            LottoTable.drwNo eq dto.drwNo 
                        }.firstOrNull()

                        if (existingEntity != null) {
                            // 기존 엔티티 업데이트
                            existingEntity.apply {
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
                            log.debug("Updated existing lotto entity for draw number ${dto.drwNo}")
                        } else {
                            // 새 엔티티 생성
                            LottoEntity.new {
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
                            log.debug("Created new lotto entity for draw number ${dto.drwNo}")
                        }
                    } catch (e: Exception) {
                        log.error("Error processing lotto for draw number ${dto.drwNo}: ${e.message}", e)
                    }
                }
            }
        } catch (e: DataAccessException) {
            log.error("Data access error while saving lotto numbers: ${e.message}", e)
            throw LottoServiceException("로또 번호 저장 중 데이터 접근 오류가 발생했습니다", e)
        } catch (e: Exception) {
            log.error("Failed to save lotto numbers: ${e.message}", e)
            throw LottoServiceException("로또 번호 저장에 실패했습니다", e)
        }
    }
}