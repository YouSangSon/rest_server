package yousang.rest_server.adapter.out.persistence.jdbc.db

import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import yousang.rest_server.application.ports.out.db.DbInfoPort

@Repository
@Profile("jdbc-postgres")
class JdbcDbInfoAdapter(
    private val jdbcTemplate: JdbcTemplate
) : DbInfoPort {
    override fun fetchCurrentTime(): String {
        val sql = "select to_char(current_timestamp, 'YYYY-MM-DD\"T\"HH24:MI:SS.USOF')"
        return jdbcTemplate.queryForObject(sql, String::class.java) ?: ""
    }
}
