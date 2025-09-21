package yousang.rest_server.adapter.`in`.web.lotto

import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import yousang.rest_server.application.ports.`in`.lotto.GenerateLottoUseCase
import yousang.rest_server.application.ports.`in`.lotto.GetLottoUseCase

@RestController
@Profile("postgres")
@RequestMapping("/api/v1/lotto")
class LottoController(
    private val generateLottoUseCase: GenerateLottoUseCase,
    private val getLottoUseCase: GetLottoUseCase
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun generate(): LottoResponse {
        val dto = generateLottoUseCase.generateAndSave()
        return LottoResponse(dto.id, dto.numbers)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): LottoResponse {
        val dto = getLottoUseCase.getById(id) ?: throw LottoNotFoundException(id)
        return LottoResponse(dto.id, dto.numbers)
    }

    @GetMapping
    fun listAll(): List<LottoResponse> = getLottoUseCase.listAll().map { LottoResponse(it.id, it.numbers) }
}

data class LottoResponse(
    val id: Long,
    val numbers: List<Int>
)

@ResponseStatus(HttpStatus.NOT_FOUND)
class LottoNotFoundException(id: Long) : RuntimeException("Lotto ticket not found: $id")
