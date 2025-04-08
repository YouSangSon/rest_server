package yousang.rest.config

import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

/**
 * 데이터 관련 설정 (DB, Redis, ORM)
 * - Exposed ORM 설정
 * - 데이터베이스 초기화
 * - Redis 연결 설정
 */
@Configuration
@EnableTransactionManagement
class DataConfig {
    
    private val logger = LoggerFactory.getLogger(DataConfig::class.java)
    
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
        return Database.connect(dataSource)
    }
    
    /**
     * Redis 연결 팩토리
     */
//    @Bean
//    fun redisConnectionFactory(): RedisConnectionFactory {
//        val host = System.getProperty("REDIS_HOST") ?: "localhost"
//        val port = System.getProperty("REDIS_PORT")?.toIntOrNull() ?: 6379
//
//        return LettuceConnectionFactory(host, port)
//    }
//
    /**
     * Redis 템플릿 설정
     */
//    @Bean
//    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
//        val template = RedisTemplate<String, Any>()
//        template.connectionFactory = connectionFactory
//        template.keySerializer = StringRedisSerializer()
//
//        return template
//    }
    
    /**
     * 애플리케이션 시작 시 데이터베이스 초기화
     */
    @Bean
    fun databaseInitializer(): ApplicationRunner {
        return ApplicationRunner {
            try {
                logger.info("데이터베이스 초기화 시작")
                
                // 여기에 데이터베이스 초기화 로직 구현
                // 예: 초기 데이터 삽입, 스키마 확인 등
                
                logger.info("데이터베이스 초기화 완료")
            } catch (e: Exception) {
                logger.error("데이터베이스 초기화 중 오류 발생", e)
                throw e
            }
        }
    }
} 