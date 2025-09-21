package yousang.rest_server.application.service.lotto

import yousang.rest_server.application.ports.`in`.lotto.GenerateLottoUseCase
import yousang.rest_server.application.ports.`in`.lotto.GetLottoUseCase
import yousang.rest_server.application.ports.`in`.lotto.LottoDto
import yousang.rest_server.application.ports.out.lotto.LottoRepositoryPort
import kotlin.random.Random

/**
 * Application service for Lotto use cases.
 * Pure Kotlin; no framework annotations.
 */
class LottoService(
    private val lottoRepositoryPort: LottoRepositoryPort
) : GenerateLottoUseCase, GetLottoUseCase {

    override fun generateAndSave(): LottoDto {
        val numbers = generateNumbers()
        val saved = lottoRepositoryPort.save(numbers)
        return LottoDto(saved.id, saved.numbers)
    }

    override fun getById(id: Long): LottoDto? {
        return lottoRepositoryPort.findById(id)?.let { LottoDto(it.id, it.numbers) }
    }

    override fun listAll(): List<LottoDto> {
        return lottoRepositoryPort.findAll().map { LottoDto(it.id, it.numbers) }
    }

    private fun generateNumbers(): List<Int> {
        val pool = (1..45).toMutableList()
        val picked = mutableListOf<Int>()
        repeat(6) {
            val idx = Random.nextInt(pool.size)
            picked += pool.removeAt(idx)
        }
        return picked.sorted()
    }
}
