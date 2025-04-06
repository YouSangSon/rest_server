package yousang.rest.interfaces.lotto

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import yousang.rest.application.lotto.LottoUseCase
import yousang.rest.interfaces.ApiResponse
import yousang.rest.interfaces.BaseController
import yousang.rest.shared.log.LoggerDelegate

/**
 * 로또 번호 관련 API 컨트롤러
 */
@Tag(name = "로또 API", description = "로또 번호 조회 및 관리 API")
@RestController
@RequestMapping("/lotto")
class LottoController(private val lottoUseCase: LottoUseCase) : BaseController() {

    private val log by LoggerDelegate()

    /**
     * 특정 범위의 로또 번호를 조회
     * @param firstDrwNo 시작 회차 번호
     * @param lastDrwNo 끝 회차 번호
     * @return 로또 번호 정보가 포함된 API 응답
     */
    @Operation(summary = "로또 번호 조회", description = "지정된 범위 내의 로또 번호를 조회")
    @Parameter(name = "firstDrwNo", description = "시작 회차 번호", example = "1")
    @Parameter(name = "lastDrwNo", description = "끝 회차 번호", example = "10")
    @GetMapping("/numbers", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getLottoRange(
        @RequestParam firstDrwNo: Int,
        @RequestParam lastDrwNo: Int
    ): ApiResponse {
        log.info("Fetching lotto numbers from $firstDrwNo to $lastDrwNo")
        val lottoData = lottoUseCase.getLotto(firstDrwNo, lastDrwNo)
        return ApiResponse(
            statusCode = 200,
            message = "Retrieved lotto numbers from $firstDrwNo to $lastDrwNo",
            data = lottoData
        )
    }

    /**
     * 로또 번호를 저장합니다.
     * 
     * @return 저장 결과가 포함된 ApiResponse
     */
    @Operation(summary = "로또 번호 저장", description = "로또 번호를 저장합니다.")
    @PutMapping("/numbers", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun putLottoNumber(): ApiResponse {
        log.info("Saving lotto numbers")
        lottoUseCase.putLotto()
        return ApiResponse(
            statusCode = 200, 
            message = "Successfully saved lotto numbers", 
            data = null
        )
    }
}
