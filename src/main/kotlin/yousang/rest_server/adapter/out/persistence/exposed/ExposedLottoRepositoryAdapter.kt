package yousang.rest_server.adapter.out.persistence.exposed.lotto

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import yousang.rest_server.application.ports.out.lotto.LottoRecord
import yousang.rest_server.application.ports.out.lotto.LottoRepositoryPort

/**
 * Exposed-based implementation of LottoRepositoryPort using a simple numbers-as-string storage.
 */
@Repository
@Profile("postgres")
class ExposedLottoRepositoryAdapter(
    private val database: Database
) : LottoRepositoryPort {

    object LottoTickets : Table("lotto_tickets") {
        val id = long("id").autoIncrement()
        val numbers = varchar("numbers", length = 100)
        override val primaryKey = PrimaryKey(id)
    }

    override fun save(numbers: List<Int>): LottoRecord = transaction(database) {
        createMissingTablesAndColumns(LottoTickets)
        val numbersStr = numbers.joinToString(",")
        val insertedId = LottoTickets.insert {
            it[LottoTickets.numbers] = numbersStr
        }[LottoTickets.id]
        LottoRecord(insertedId, numbers)
    }

    override fun findById(id: Long): LottoRecord? = transaction(database) {
        createMissingTablesAndColumns(LottoTickets)
        LottoTickets
            .selectAll()
            .where { LottoTickets.id eq id }
            .limit(1)
            .firstOrNull()
            ?.let { row ->
                val nums = row[LottoTickets.numbers].split(',').filter { it.isNotBlank() }.map { it.toInt() }
                LottoRecord(row[LottoTickets.id], nums)
            }
    }

    override fun findAll(): List<LottoRecord> = transaction(database) {
        createMissingTablesAndColumns(LottoTickets)
        LottoTickets
            .selectAll()
            .orderBy(LottoTickets.id, SortOrder.DESC)
            .map { row ->
                val nums = row[LottoTickets.numbers].split(',').filter { it.isNotBlank() }.map { it.toInt() }
                LottoRecord(row[LottoTickets.id], nums)
            }
    }
}
