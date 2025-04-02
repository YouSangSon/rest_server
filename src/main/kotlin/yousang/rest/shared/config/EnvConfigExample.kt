package yousang.rest.shared.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

/**
 * EnvConfig 사용 예시를 보여주는 서비스 클래스
 * 이 클래스는 예시용이므로 실제 애플리케이션에선 필요에 따라 수정하거나 제거하세요.
 */
@Service
class EnvConfigExampleService @Autowired constructor(
    private val envConfig: EnvConfig
) {
    private val logger = LoggerFactory.getLogger(EnvConfigExampleService::class.java)
    
    /**
     * 환경 설정 정보를 로깅하여 출력하는 예시 메서드
     */
    fun printEnvironmentInfo() {
        logger.info("===== 환경 설정 정보 =====")
        logger.info("Active Profile: ${envConfig.getActiveProfile()}")
        logger.info("Development Mode: ${envConfig.isDev()}")
        
        // 데이터베이스 설정 출력
        logger.info("----- 데이터베이스 설정 -----")
        logger.info("DB URL: ${envConfig.db.url}")
        logger.info("DB Username: ${envConfig.db.username}")
        logger.info("DB Pool Size: ${envConfig.db.poolSize}")
        logger.info("DB Connection Timeout: ${envConfig.db.connTimeout}ms")
        
        // 서버 설정 출력
        logger.info("----- 서버 설정 -----")
        logger.info("Server Port: ${envConfig.server.port}")
        logger.info("API Base Path: ${envConfig.server.apiBasePath}")
        logger.info("CORS Allowed Origins: ${envConfig.server.corsAllowedOrigins}")
        logger.info("Compression Enabled: ${envConfig.server.compressionEnabled}")
        
        // 기타 설정 출력
        logger.info("----- 기타 설정 -----")
        logger.info("Log Level: ${envConfig.get("LOG_LEVEL")}")
        logger.info("JPA Show SQL: ${envConfig.getBoolean("JPA_SHOW_SQL")}")
        logger.info("Hibernate Format SQL: ${envConfig.getBoolean("HIBERNATE_FORMAT_SQL")}")
        
        logger.info("==========================")
    }
    
    /**
     * 현재 환경에 따라 다른 로직을 실행하는 예시 메서드
     */
    fun executeEnvironmentSpecificLogic() {
        if (envConfig.isDev()) {
            logger.info("개발 환경에서 실행 중 - 추가 디버깅 정보 활성화")
            // 개발 환경 특화 로직
        } else if (envConfig.isProd()) {
            logger.info("운영 환경에서 실행 중 - 성능 최적화 모드 활성화")
            // 운영 환경 특화 로직
        } else {
            logger.info("알 수 없는 환경에서 실행 중: ${envConfig.getActiveProfile()}")
            // 기본 로직
        }
    }
} 