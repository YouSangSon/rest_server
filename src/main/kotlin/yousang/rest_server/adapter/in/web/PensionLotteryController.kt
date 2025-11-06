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
 * 연금복권 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/lottery/pension")
@Tag(name = "Pension Lottery", description = "연금복권 API")
class PensionLotteryController(
    private val generatePensionLotteryNumbersUseCase: GeneratePensionLotteryNumbersUseCase,
    private val checkLotteryWinningUseCase: CheckLotteryWinningUseCase,
    private val getLotteryTicketsUseCase: GetLotteryTicketsUseCase,
    private val getLotteryDrawResultsUseCase: GetLotteryDrawResultsUseCase,
    private val userRepository: UserRepositoryPort
) {

    @PostMapping("/generate/auto")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "자동 연금복권 번호 생성", description = "랜덤으로 연금복권 번호를 생성합니다")
    fun generateAutoNumbers(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: GenerateAutoPensionLotteryRequest
    ): PensionLotteryTicketsResponse {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("사용자를 찾을 수 없습니다")

        val tickets = generatePensionLotteryNumbersUseCase.generateAutoNumbers(
            userId = user.id!!,
            drawNumber = request.drawNumber,
            count = request.count
        )

        return PensionLotteryTicketsResponse.from(tickets)
    }

    @PostMapping("/generate/manual")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "수동 연금복권 번호 생성", description = "사용자가 선택한 번호로 연금복권 티켓을 생성합니다")
    fun generateManualNumbers(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: GenerateManualPensionLotteryRequest
    ): PensionLotteryTicketsResponse {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("사용자를 찾을 수 없습니다")

        val tickets = generatePensionLotteryNumbersUseCase.generateManualNumbers(
            userId = user.id!!,
            drawNumber = request.drawNumber,
            tickets = request.tickets.map { it.group to it.number }
        )

        return PensionLotteryTicketsResponse.from(tickets)
    }

    @GetMapping("/tickets")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "내 연금복권 티켓 조회", description = "사용자의 연금복권 티켓 목록을 조회합니다")
    fun getMyTickets(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) drawNumber: Int?
    ): PensionLotteryTicketsResponse {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("사용자를 찾을 수 없습니다")

        val tickets = getLotteryTicketsUseCase.getUserPensionLotteryTickets(user.id!!, drawNumber)
        return PensionLotteryTicketsResponse.from(tickets)
    }

    @GetMapping("/tickets/{ticketId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "연금복권 티켓 상세 조회", description = "특정 연금복권 티켓의 상세 정보를 조회합니다")
    fun getTicket(
        @PathVariable ticketId: Long
    ): PensionLotteryTicketResponse {
        val ticket = getLotteryTicketsUseCase.getPensionLotteryTicket(ticketId)
        return PensionLotteryTicketResponse.from(ticket)
    }

    @GetMapping("/tickets/winning")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "내 당첨 티켓 조회", description = "사용자의 당첨된 연금복권 티켓 목록을 조회합니다")
    fun getMyWinningTickets(
        @AuthenticationPrincipal userDetails: UserDetails
    ): PensionLotteryTicketsResponse {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("사용자를 찾을 수 없습니다")

        val tickets = getLotteryTicketsUseCase.getUserWinningPensionLotteryTickets(user.id!!)
        return PensionLotteryTicketsResponse.from(tickets)
    }

    @PostMapping("/tickets/{ticketId}/check")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "연금복권 당첨 확인", description = "특정 연금복권 티켓의 당첨 여부를 확인합니다")
    fun checkWinning(
        @PathVariable ticketId: Long
    ): PensionLotteryTicketResponse {
        val ticket = checkLotteryWinningUseCase.checkPensionLotteryWinning(ticketId)
        return PensionLotteryTicketResponse.from(ticket)
    }

    @PostMapping("/check-all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "회차별 전체 당첨 확인", description = "특정 회차의 모든 연금복권 티켓 당첨 여부를 확인합니다")
    fun checkAllTickets(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam drawNumber: Int
    ): PensionLotteryTicketsResponse {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("사용자를 찾을 수 없습니다")

        val tickets = checkLotteryWinningUseCase.checkAllPensionLotteryTickets(user.id!!, drawNumber)
        return PensionLotteryTicketsResponse.from(tickets)
    }

    @GetMapping("/draws/{drawNumber}")
    @Operation(summary = "연금복권 추첨 결과 조회", description = "특정 회차의 연금복권 추첨 결과를 조회합니다")
    fun getDrawResult(
        @PathVariable drawNumber: Int
    ): PensionLotteryDrawResultResponse {
        val result = getLotteryDrawResultsUseCase.getPensionLotteryDrawResult(drawNumber)
        return PensionLotteryDrawResultResponse.from(result)
    }

    @GetMapping("/draws/latest")
    @Operation(summary = "최신 연금복권 추첨 결과", description = "가장 최근 연금복권 추첨 결과를 조회합니다")
    fun getLatestDrawResult(): PensionLotteryDrawResultResponse {
        val result = getLotteryDrawResultsUseCase.getLatestPensionLotteryDrawResult()
        return PensionLotteryDrawResultResponse.from(result)
    }

    @GetMapping("/draws")
    @Operation(summary = "연금복권 추첨 결과 목록", description = "연금복권 추첨 결과 목록을 조회합니다 (최신순)")
    fun getDrawResults(
        @RequestParam(defaultValue = "10") limit: Int
    ): List<PensionLotteryDrawResultResponse> {
        val results = getLotteryDrawResultsUseCase.getPensionLotteryDrawResults(limit)
        return results.map { PensionLotteryDrawResultResponse.from(it) }
    }
}
