package yousang.rest_server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource
import org.jetbrains.exposed.sql.Database

/**
 * Initialize JetBrains Exposed with Spring's DataSource when 'postgres' profile is active.
 */
@Configuration
@Profile("postgres")
class ExposedConfig {
    @Bean
    fun exposedDatabase(dataSource: DataSource): Database = Database.connect(dataSource)
}
