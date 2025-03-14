package yousang.rest.adapter.`in`.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import yousang.rest.adapter.`in`.web.dto.LottoResponseDto
import yousang.rest.domain.port.`in`.LottoQueryUseCase

@RestController
@RequestMapping("/api/lotto")
class LottoController(
    private val lottoQueryUseCase: LottoQueryUseCase
) {
    
    @GetMapping("/latest")
    fun getLatestLotto(): ResponseEntity<LottoResponseDto> {
        val lotto = lottoQueryUseCase.getLatestLotto()
        return ResponseEntity.ok(LottoResponseDto.fromDomain(lotto))
    }
    
    @GetMapping("/{round}")
    fun getLottoByRound(@PathVariable round: Int): ResponseEntity<LottoResponseDto> {
        val lotto = lottoQueryUseCase.getLottoByRound(round)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(LottoResponseDto.fromDomain(lotto))
    }
    
    @GetMapping
    fun getLottoByRoundRange(
        @RequestParam(defaultValue = "1") fromRound: Int,
        @RequestParam(required = false) toRound: Int?
    ): ResponseEntity<List<LottoResponseDto>> {
        val effectiveToRound = toRound ?: lottoQueryUseCase.getLatestLotto().round
        
        val query = LottoQueryUseCase.LottoRangeQuery(
            fromRound = fromRound,
            toRound = effectiveToRound
        )
        
        val lottos = lottoQueryUseCase.getLottoByRoundRange(query)
        return ResponseEntity.ok(lottos.map { LottoResponseDto.fromDomain(it) })
    }
    
    @GetMapping("/all")
    fun getAllLottoRounds(): ResponseEntity<List<LottoResponseDto>> {
        val lottos = lottoQueryUseCase.getAllLottoRounds()
        return ResponseEntity.ok(lottos.map { LottoResponseDto.fromDomain(it) })
    }
} 