package yousang.rest.config

import io.github.cdimascio.dotenv.Dotenv
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import jakarta.annotation.PostConstruct
import java.io.File

@Configuration
class EnvConfig(
    private val environment: Environment,
    @Value("\${spring.profiles.active:dev}") private val activeProfile: String
) {
    private val logger = LoggerFactory.getLogger(EnvConfig::class.java)
    
    // env 폴더 경로
    private val envDirPath = "env"

    @PostConstruct
    fun init() {
        try {
            // env 디렉토리가 존재하는지 확인
            val envDir = File(envDirPath)
            if (!envDir.exists() || !envDir.isDirectory) {
                logger.warn("env 디렉토리를 찾을 수 없습니다: $envDirPath")
                return
            }
            
            // 프로파일에 해당하는 env 파일 로드
            val dotenv = when (activeProfile) {
                "prod" -> Dotenv.configure().directory(envDirPath).filename("prod.env").ignoreIfMissing().load()
                "dev" -> Dotenv.configure().directory(envDirPath).filename("dev.env").ignoreIfMissing().load()
                else -> Dotenv.configure().directory(envDirPath).filename("${activeProfile}.env").ignoreIfMissing().load()
            }

            logger.info("환경 설정 로드 완료: 프로필 '${activeProfile}'")
            logger.info("애플리케이션이 다음 프로필로 실행 중: ${environment.activeProfiles.joinToString(", ")}")
            
            // .env 파일이 로드되었는지 확인
            if (dotenv.entries().isEmpty()) {
                logger.warn("환경 변수 파일(${activeProfile}.env)을 찾을 수 없거나 비어 있습니다. application.yml 기본값을 사용합니다.")
            } else {
                logger.info("환경 변수 파일(${activeProfile}.env)이 성공적으로 로드되었습니다.")
            }
        } catch (e: Exception) {
            logger.error("환경 변수 로드 중 오류 발생: ${e.message}")
        }
    }
} 