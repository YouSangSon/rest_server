package yousang.rest.config

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import yousang.rest.domain.lotto.AnnuityLottoTable
import yousang.rest.domain.lotto.LottoTable
import yousang.rest.shared.log.log

@Component
class DatabaseInitializer : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        transaction {
            try {
                log.info("Initializing database tables...")
                // Create Lotto tables
                SchemaUtils.create(
                    LottoTable,
                    AnnuityLottoTable
                )
                log.info("Database tables created successfully")
            } catch (e: Exception) {
                log.error("Error creating database tables: ${e.message}", e)
                throw e
            }
        }
    }
} 