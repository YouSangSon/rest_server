package yousang.rest.adapter.`in`.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import yousang.rest.adapter.`in`.web.dto.PensionLotteryResponseDto
import yousang.rest.domain.port.`in`.PensionLotteryQueryUseCase

@RestController
@RequestMapping("/api/pension-lottery")
class PensionLotteryController(
    private val pensionLotteryQueryUseCase: PensionLotteryQueryUseCase
) {
    
    @GetMapping("/latest")
    fun getLatestPensionLottery(): ResponseEntity<PensionLotteryResponseDto> {
        try {
            val pensionLottery = pensionLotteryQueryUseCase.getLatestPensionLottery()
            return ResponseEntity.ok(PensionLotteryResponseDto.fromDomain(pensionLottery))
        } catch (e: NotImplementedError) {
            return ResponseEntity.status(501).build() // Not Implemented
        }
    }
    
    @GetMapping("/{round}")
    fun getPensionLotteryByRound(@PathVariable round: Int): ResponseEntity<PensionLotteryResponseDto> {
        try {
            val pensionLottery = pensionLotteryQueryUseCase.getPensionLotteryByRound(round)
                ?: return ResponseEntity.notFound().build()
            
            return ResponseEntity.ok(PensionLotteryResponseDto.fromDomain(pensionLottery))
        } catch (e: NotImplementedError) {
            return ResponseEntity.status(501).build() // Not Implemented
        }
    }
    
    @GetMapping
    fun getPensionLotteryByRoundRange(
        @RequestParam(defaultValue = "1") fromRound: Int,
        @RequestParam(required = false) toRound: Int?
    ): ResponseEntity<List<PensionLotteryResponseDto>> {
        try {
            val effectiveToRound = toRound ?: pensionLotteryQueryUseCase.getLatestPensionLottery().round
            
            val query = PensionLotteryQueryUseCase.PensionLotteryRangeQuery(
                fromRound = fromRound,
                toRound = effectiveToRound
            )
            
            val pensionLotteries = pensionLotteryQueryUseCase.getPensionLotteryByRoundRange(query)
            return ResponseEntity.ok(pensionLotteries.map { PensionLotteryResponseDto.fromDomain(it) })
        } catch (e: NotImplementedError) {
            return ResponseEntity.status(501).build() // Not Implemented
        }
    }
    
    @GetMapping("/all")
    fun getAllPensionLotteryRounds(): ResponseEntity<List<PensionLotteryResponseDto>> {
        try {
            val pensionLotteries = pensionLotteryQueryUseCase.getAllPensionLotteryRounds()
            return ResponseEntity.ok(pensionLotteries.map { PensionLotteryResponseDto.fromDomain(it) })
        } catch (e: NotImplementedError) {
            return ResponseEntity.status(501).build() // Not Implemented
        }
    }
} 