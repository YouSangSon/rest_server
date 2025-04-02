package yousang.rest.shared.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

/**
 * 환경 변수에 접근하기 위한 유틸리티 클래스
 * 
 * 사용 예시:
 * ```kotlin
 * @Autowired
 * private lateinit var envConfig: EnvConfig
 * 
 * fun someMethod() {
 *   val dbUrl = envConfig.get("DB_URL")
 *   // ...
 * }
 * ```
 */
@Component
class EnvConfig @Autowired constructor(private val environment: Environment) {
    
    /**
     * 환경 변수 또는 시스템 프로퍼티에서 값을 가져옴
     * 
     * @param key 가져올 환경 변수 키
     * @param defaultValue 값이 없을 경우 기본값 (기본값은 null)
     * @return 환경 변수 값 또는 기본값
     */
    fun get(key: String, defaultValue: String? = null): String? {
        return environment.getProperty(key) 
               ?: System.getenv(key) 
               ?: System.getProperty(key) 
               ?: defaultValue
    }
    
    /**
     * 환경 변수를 불리언으로 변환
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return get(key)?.lowercase()?.let { value ->
            when (value) {
                "true", "yes", "1", "on" -> true
                "false", "no", "0", "off" -> false
                else -> defaultValue
            }
        } ?: defaultValue
    }
    
    /**
     * 환경 변수를 정수로 변환
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return get(key)?.toIntOrNull() ?: defaultValue
    }
    
    /**
     * 환경 변수를 Long으로 변환
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return get(key)?.toLongOrNull() ?: defaultValue
    }
    
    /**
     * 현재 활성화된 프로필 확인
     */
    fun getActiveProfile(): String {
        val profiles = environment.activeProfiles
        return if (profiles.isNotEmpty()) profiles[0] else "dev"
    }
    
    /**
     * 현재 프로필이 개발 환경인지 확인
     */
    fun isDev(): Boolean = getActiveProfile() == "dev"
    
    /**
     * 현재 프로필이 운영 환경인지 확인
     */
    fun isProd(): Boolean = getActiveProfile() == "prod"
    
    /**
     * DB 관련 환경 변수에 쉽게 접근하기 위한 객체
     */
    val db: DbConfig by lazy { DbConfig(this) }
    
    /**
     * 앱 서버 관련 환경 변수에 쉽게 접근하기 위한 객체
     */
    val server: ServerConfig by lazy { ServerConfig(this) }
}

/**
 * DB 관련 환경 변수를 위한 구성 클래스
 */
class DbConfig(private val envConfig: EnvConfig) {
    val url: String? get() = envConfig.get("DB_URL")
    val username: String? get() = envConfig.get("DB_USERNAME")
    val password: String? get() = envConfig.get("DB_PASSWORD")
    val poolSize: Int get() = envConfig.getInt("DB_POOL_SIZE", 20)
    val minIdle: Int get() = envConfig.getInt("DB_MIN_IDLE", 10)
    val connTimeout: Long get() = envConfig.getLong("DB_CONN_TIMEOUT", 20000)
    val idleTimeout: Long get() = envConfig.getLong("DB_IDLE_TIMEOUT", 30000)
}

/**
 * 서버 관련 환경 변수를 위한 구성 클래스
 */
class ServerConfig(private val envConfig: EnvConfig) {
    val port: Int get() = envConfig.getInt("APP_PORT", 8080)
    val apiBasePath: String? get() = envConfig.get("API_BASE_PATH", "/api/v1")
    val corsAllowedOrigins: String? get() = envConfig.get("CORS_ALLOWED_ORIGINS", "*")
    val corsAllowedMethods: String? get() = envConfig.get("CORS_ALLOWED_METHODS", "GET,POST,PUT,DELETE,OPTIONS")
    val compressionEnabled: Boolean get() = envConfig.getBoolean("COMPRESSION_ENABLED", true)
} 