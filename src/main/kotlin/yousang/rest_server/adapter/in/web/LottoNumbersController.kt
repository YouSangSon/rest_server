package yousang.rest_server.adapter.`in`.web.lotto

import org.springframework.web.bind.annotation.*
import yousang.rest_server.adapter.`in`.web.common.ApiResponse
import yousang.rest_server.application.ports.`in`.lottoOfficial.OfficialLottoUseCase

@RestController
@RequestMapping("/api/v1/lotto")
class LottoNumbersController(
    private val lottoUseCase: OfficialLottoUseCase
) {

    @GetMapping("/numbers")
    fun getLottoRange(
        @RequestParam firstDrwNo: Int,
        @RequestParam lastDrwNo: Int
    ): ApiResponse {
        val data = lottoUseCase.getLotto(firstDrwNo, lastDrwNo)
        return ApiResponse(
            statusCode = 200,
            message = "Retrieved lotto numbers from $firstDrwNo to $lastDrwNo",
            data = data
        )
    }

    @PutMapping("/numbers")
    fun saveLottoNumbers(): ApiResponse {
        lottoUseCase.putLotto()
        return ApiResponse(
            statusCode = 200,
            message = "Successfully saved lotto numbers",
            data = null
        )
    }

    @GetMapping("/numbers/{drwNo}")
    fun getLottoByDrawNumber(@PathVariable drwNo: Int): ApiResponse {
        val lotto = lottoUseCase.getLotto(drwNo)
        return ApiResponse(
            statusCode = 200,
            message = "Retrieved lotto number for draw $drwNo",
            data = lotto
        )
    }
}