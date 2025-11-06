package yousang.rest_server.adapter.`in`.web.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

/**
 * 자동 로또 번호 생성 요청
 */
data class GenerateAutoLottoRequest(
    @field:Min(1, message = "회차는 1 이상이어야 합니다")
    val drawNumber: Int,

    @field:Min(1, message = "생성 개수는 1 이상이어야 합니다")
    @field:Max(100, message = "생성 개수는 100 이하여야 합니다")
    val count: Int = 5
)

/**
 * 수동 로또 번호 생성 요청
 */
data class GenerateManualLottoRequest(
    @field:Min(1, message = "회차는 1 이상이어야 합니다")
    val drawNumber: Int,

    @field:NotEmpty(message = "번호 목록은 비어있을 수 없습니다")
    @field:Size(min = 1, max = 100, message = "번호 목록은 1~100개여야 합니다")
    val numbersList: List<@Size(min = 6, max = 6, message = "각 게임은 6개의 번호를 가져야 합니다") List<Int>>
)

/**
 * 자동 연금복권 번호 생성 요청
 */
data class GenerateAutoPensionLotteryRequest(
    @field:Min(1, message = "회차는 1 이상이어야 합니다")
    val drawNumber: Int,

    @field:Min(1, message = "생성 개수는 1 이상이어야 합니다")
    @field:Max(100, message = "생성 개수는 100 이하여야 합니다")
    val count: Int = 5
)

/**
 * 수동 연금복권 번호 생성 요청
 */
data class GenerateManualPensionLotteryRequest(
    @field:Min(1, message = "회차는 1 이상이어야 합니다")
    val drawNumber: Int,

    @field:NotEmpty(message = "티켓 목록은 비어있을 수 없습니다")
    @field:Size(min = 1, max = 100, message = "티켓 목록은 1~100개여야 합니다")
    val tickets: List<PensionLotteryTicketInput>
)

/**
 * 연금복권 티켓 입력
 */
data class PensionLotteryTicketInput(
    @field:Min(1, message = "조는 1~5 범위여야 합니다")
    @field:Max(5, message = "조는 1~5 범위여야 합니다")
    val group: Int,

    @field:Min(0, message = "번호는 0~999999 범위여야 합니다")
    @field:Max(999999, message = "번호는 0~999999 범위여야 합니다")
    val number: Int
)
