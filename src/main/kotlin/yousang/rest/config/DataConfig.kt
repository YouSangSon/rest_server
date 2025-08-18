package yousang.rest.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.time.Duration
import javax.sql.DataSource

/**
 * 데이터 관련 설정 (DB, Redis, ORM)
 * - Exposed ORM 설정
 * - PostgreSQL HikariCP 연결 풀 설정
 * - 데이터베이스 초기화
 * - Redis 연결 설정
 */
@Configuration
@EnableTransactionManagement
class DataConfig {
    
    private val logger = LoggerFactory.getLogger(DataConfig::class.java)
    
    /**
     * PostgreSQL HikariCP 데이터소스 설정
     */
    @Bean
    fun dataSource(env: Environment): DataSource {
        val config = HikariConfig().apply {
            jdbcUrl = env.getProperty("spring.datasource.url") ?: "jdbc:postgresql://localhost:5432/rest_dev"
            username = env.getProperty("spring.datasource.username") ?: "postgres"
            password = env.getProperty("spring.datasource.password") ?: "postgres"
            driverClassName = env.getProperty("spring.datasource.driver-class-name") ?: "org.postgresql.Driver"
            
            // 연결 풀 설정
            maximumPoolSize = env.getProperty("spring.datasource.hikari.maximum-pool-size", Int::class.java) ?: 20
            minimumIdle = env.getProperty("spring.datasource.hikari.minimum-idle", Int::class.java) ?: 5
            idleTimeout = env.getProperty("spring.datasource.hikari.idle-timeout", Long::class.java) ?: 30000L
            connectionTimeout = env.getProperty("spring.datasource.hikari.connection-timeout", Long::class.java) ?: 10000L
            maxLifetime = env.getProperty("spring.datasource.hikari.max-lifetime", Long::class.java) ?: 2000000L
            
            // PostgreSQL 최적화 설정
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            addDataSourceProperty("useServerPrepStmts", "true")
            addDataSourceProperty("useLocalSessionState", "true")
            addDataSourceProperty("rewriteBatchedStatements", "true")
            addDataSourceProperty("cacheResultSetMetadata", "true")
            addDataSourceProperty("cacheServerConfiguration", "true")
            addDataSourceProperty("elideSetAutoCommits", "true")
            addDataSourceProperty("maintainTimeStats", "false")
            
            // 연결 테스트 설정
            connectionTestQuery = "SELECT 1"
            validationTimeout = 5000L
            
            // 로깅 설정
            poolName = "PostgreSQLHikariCP"
        }
        
        logger.info("PostgreSQL HikariCP 데이터소스 설정 완료")
        logger.info("JDBC URL: ${config.jdbcUrl}")
        logger.info("최대 연결 풀 크기: ${config.maximumPoolSize}")
        logger.info("최소 유휴 연결: ${config.minimumIdle}")
        
        return HikariDataSource(config)
    }
    
    /**
     * Exposed 트랜잭션 매니저 설정
     */
    @Bean
    fun transactionManager(dataSource: DataSource): SpringTransactionManager {
        return SpringTransactionManager(dataSource)
    }
    
    /**
     * Exposed Database 객체 설정
     */
    @Bean
    fun database(dataSource: DataSource): Database {
        val database = Database.connect(dataSource)
        logger.info("Exposed Database 연결 완료")
        return database
    }
    
    /**
     * Redis 연결 팩토리 (현재 비활성화)
     */
    // @Bean
    // fun redisConnectionFactory(envConfig: EnvConfig): RedisConnectionFactory {
    //     val redisStandaloneConfig = RedisStandaloneConfiguration().apply {
    //         hostName = envConfig.redis.host
    //         port = envConfig.redis.port
            
    //         if (envConfig.redis.password != null) {
    //             setPassword(envConfig.redis.password)
    //         }
            
    //         database = envConfig.redis.database
    //     }
        
    //     val clientConfig = LettuceClientConfiguration.builder()
    //         .commandTimeout(Duration.ofSeconds(5))
            
    //     if (envConfig.redis.useSSL) {
    //         clientConfig.useSsl()
    //     }
        
    //     return LettuceConnectionFactory(redisStandaloneConfig, clientConfig.build())
    // }
    
    /**
     * Redis 템플릿 설정 (현재 비활성화)
     */
    // @Bean
    // fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
    //     val template = RedisTemplate<String, Any>()
    //     template.connectionFactory = connectionFactory
    //     template.keySerializer = StringRedisSerializer()
        
    //     return template
    // }
    
    /**
     * 애플리케이션 시작 시 데이터베이스 초기화
     */
    @Bean
    fun databaseInitializer(): ApplicationRunner {
        return ApplicationRunner {
            try {
                logger.info("🚀 데이터베이스 초기화 시작")
                
                // 데이터베이스 연결 테스트
                val dataSource = dataSource(org.springframework.context.ApplicationContextProvider.getApplicationContext().environment)
                val connection = dataSource.connection
                
                if (connection.isValid(5)) {
                    logger.info("✅ PostgreSQL 데이터베이스 연결 성공")
                    
                    // 데이터베이스 정보 출력
                    val metaData = connection.metaData
                    logger.info("📊 데이터베이스 정보:")
                    logger.info("   - 제품명: ${metaData.databaseProductName}")
                    logger.info("   - 버전: ${metaData.databaseProductVersion}")
                    logger.info("   - URL: ${metaData.url}")
                    logger.info("   - 사용자: ${metaData.userName}")
                    
                    // 테이블 존재 여부 확인
                    val tables = mutableListOf<String>()
                    val resultSet = metaData.getTables(null, null, "%", arrayOf("TABLE"))
                    while (resultSet.next()) {
                        tables.add(resultSet.getString("TABLE_NAME"))
                    }
                    
                    logger.info("📋 존재하는 테이블: ${tables.joinToString(", ")}")
                    
                    connection.close()
                } else {
                    logger.error("❌ PostgreSQL 데이터베이스 연결 실패")
                }
                
                logger.info("✅ 데이터베이스 초기화 완료")
            } catch (e: Exception) {
                logger.error("❌ 데이터베이스 초기화 중 오류 발생", e)
                // 개발 환경에서는 오류를 던지고, 운영 환경에서는 로그만 남김
                if (System.getProperty("spring.profiles.active") == "dev") {
                    throw e
                }
            }
        }
    }
} 