package yousang.rest_server.adapter.out.persistence.exposed.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import yousang.rest_server.application.ports.out.db.DbInfoPort

@Repository
@Profile("postgres")
class ExposedDbInfoAdapter(
    private val database: Database
) : DbInfoPort {
    override fun fetchCurrentTime(): String {
        return transaction(database) {
            // Run raw SQL to format timestamp with timezone offset
            val sql = "select to_char(current_timestamp, 'YYYY-MM-DD\"T\"HH24:MI:SS.USOF')"
            val result = org.jetbrains.exposed.sql.transactions.TransactionManager.current().exec(sql) { rs ->
                if (rs.next()) rs.getString(1) else ""
            }
            result ?: ""
        }
    }
}
