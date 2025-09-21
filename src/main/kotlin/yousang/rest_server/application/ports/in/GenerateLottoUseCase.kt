package yousang.rest_server.application.ports.`in`.lotto

/**
 * Input port for generating a new lotto ticket and saving it.
 */
interface GenerateLottoUseCase {
    fun generateAndSave(): LottoDto
}

/**
 * Input port for retrieving saved lotto tickets.
 */
interface GetLottoUseCase {
    fun getById(id: Long): LottoDto?
    fun listAll(): List<LottoDto>
}

/**
 * DTO representing a lotto ticket in the application layer.
 */
data class LottoDto(
    val id: Long,
    val numbers: List<Int>
)
