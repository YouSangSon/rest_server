package yousang.rest_server.application.ports.out.db

/**
 * Outbound port for retrieving DB information via custom SQL.
 * This allows infrastructure adapters (e.g., JDBC) to implement
 * queries in plain SQL while keeping the core independent.
 */
interface DbInfoPort {
    fun fetchCurrentTime(): String
}
