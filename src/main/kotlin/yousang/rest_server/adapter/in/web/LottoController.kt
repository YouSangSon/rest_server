package yousang.rest_server.adapter.`in`.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import yousang.rest_server.adapter.`in`.web.dto.*
import yousang.rest_server.application.ports.`in`.*
import yousang.rest_server.application.ports.out.UserRepositoryPort

/**
 * 로또 6/45 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/lottery/lotto")
@Tag(name = "Lotto", description = "로또 6/45 API")
class LottoController(
    private val generateLottoNumbersUseCase: GenerateLottoNumbersUseCase,
    private val checkLotteryWinningUseCase: CheckLotteryWinningUseCase,
    private val getLotteryTicketsUseCase: GetLotteryTicketsUseCase,
    private val getLotteryDrawResultsUseCase: GetLotteryDrawResultsUseCase,
    private val userRepository: UserRepositoryPort
) {

    @PostMapping("/generate/auto")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "자동 로또 번호 생성", description = "랜덤으로 로또 번호를 생성합니다")
    fun generateAutoNumbers(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: GenerateAutoLottoRequest
    ): LottoTicketsResponse {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("사용자를 찾을 수 없습니다")

        val tickets = generateLottoNumbersUseCase.generateAutoNumbers(
            userId = user.id!!,
            drawNumber = request.drawNumber,
            count = request.count
        )

        return LottoTicketsResponse.from(tickets)
    }

    @PostMapping("/generate/manual")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "수동 로또 번호 생성", description = "사용자가 선택한 번호로 로또 티켓을 생성합니다")
    fun generateManualNumbers(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: GenerateManualLottoRequest
    ): LottoTicketsResponse {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("사용자를 찾을 수 없습니다")

        val tickets = generateLottoNumbersUseCase.generateManualNumbers(
            userId = user.id!!,
            drawNumber = request.drawNumber,
            numbersList = request.numbersList
        )

        return LottoTicketsResponse.from(tickets)
    }

    @GetMapping("/tickets")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "내 로또 티켓 조회", description = "사용자의 로또 티켓 목록을 조회합니다")
    fun getMyTickets(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) drawNumber: Int?
    ): LottoTicketsResponse {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("사용자를 찾을 수 없습니다")

        val tickets = getLotteryTicketsUseCase.getUserLottoTickets(user.id!!, drawNumber)
        return LottoTicketsResponse.from(tickets)
    }

    @GetMapping("/tickets/{ticketId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "로또 티켓 상세 조회", description = "특정 로또 티켓의 상세 정보를 조회합니다")
    fun getTicket(
        @PathVariable ticketId: Long
    ): LottoTicketResponse {
        val ticket = getLotteryTicketsUseCase.getLottoTicket(ticketId)
        return LottoTicketResponse.from(ticket)
    }

    @GetMapping("/tickets/winning")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "내 당첨 티켓 조회", description = "사용자의 당첨된 로또 티켓 목록을 조회합니다")
    fun getMyWinningTickets(
        @AuthenticationPrincipal userDetails: UserDetails
    ): LottoTicketsResponse {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("사용자를 찾을 수 없습니다")

        val tickets = getLotteryTicketsUseCase.getUserWinningLottoTickets(user.id!!)
        return LottoTicketsResponse.from(tickets)
    }

    @PostMapping("/tickets/{ticketId}/check")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "로또 당첨 확인", description = "특정 로또 티켓의 당첨 여부를 확인합니다")
    fun checkWinning(
        @PathVariable ticketId: Long
    ): LottoTicketResponse {
        val ticket = checkLotteryWinningUseCase.checkLottoWinning(ticketId)
        return LottoTicketResponse.from(ticket)
    }

    @PostMapping("/check-all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "회차별 전체 당첨 확인", description = "특정 회차의 모든 로또 티켓 당첨 여부를 확인합니다")
    fun checkAllTickets(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam drawNumber: Int
    ): LottoTicketsResponse {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("사용자를 찾을 수 없습니다")

        val tickets = checkLotteryWinningUseCase.checkAllLottoTickets(user.id!!, drawNumber)
        return LottoTicketsResponse.from(tickets)
    }

    @GetMapping("/draws/{drawNumber}")
    @Operation(summary = "로또 추첨 결과 조회", description = "특정 회차의 로또 추첨 결과를 조회합니다")
    fun getDrawResult(
        @PathVariable drawNumber: Int
    ): LottoDrawResultResponse {
        val result = getLotteryDrawResultsUseCase.getLottoDrawResult(drawNumber)
        return LottoDrawResultResponse.from(result)
    }

    @GetMapping("/draws/latest")
    @Operation(summary = "최신 로또 추첨 결과", description = "가장 최근 로또 추첨 결과를 조회합니다")
    fun getLatestDrawResult(): LottoDrawResultResponse {
        val result = getLotteryDrawResultsUseCase.getLatestLottoDrawResult()
        return LottoDrawResultResponse.from(result)
    }

    @GetMapping("/draws")
    @Operation(summary = "로또 추첨 결과 목록", description = "로또 추첨 결과 목록을 조회합니다 (최신순)")
    fun getDrawResults(
        @RequestParam(defaultValue = "10") limit: Int
    ): List<LottoDrawResultResponse> {
        val results = getLotteryDrawResultsUseCase.getLottoDrawResults(limit)
        return results.map { LottoDrawResultResponse.from(it) }
    }
}
