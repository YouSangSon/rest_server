package yousang.rest_server.application.ports.out.lotto

/**
 * Outbound port for persisting and retrieving Lotto tickets.
 * Implementations can use Exposed/JDBC/etc. while keeping core independent.
 */
interface LottoRepositoryPort {
    fun save(numbers: List<Int>): LottoRecord
    fun findById(id: Long): LottoRecord?
    fun findAll(): List<LottoRecord>
}

/**
 * Persistence record representation of a saved lotto ticket.
 */
data class LottoRecord(
    val id: Long,
    val numbers: List<Int>
)
