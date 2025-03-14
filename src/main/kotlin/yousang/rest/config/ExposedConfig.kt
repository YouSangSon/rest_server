package yousang.rest.config

import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@ImportAutoConfiguration(ExposedAutoConfiguration::class)
class ExposedConfig {

    @Bean
    fun database(dataSource: DataSource): Database {
        return Database.connect(
            datasource = dataSource, databaseConfig = DatabaseConfig {
                useNestedTransactions = true
            })
    }
} 