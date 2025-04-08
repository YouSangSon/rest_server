package yousang.rest.config

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
 * 
 * 주요 환경 변수 설정:
 * - DB_URL: 데이터베이스 접속 URL
 * - DB_USERNAME: 데이터베이스 사용자명
 * - DB_PASSWORD: 데이터베이스 비밀번호
 * - DB_POOL_SIZE: 커넥션 풀 크기 (기본값: 20)
 * - DB_MIN_IDLE: 최소 유휴 커넥션 수 (기본값: 10)
 * - DB_CONN_TIMEOUT: 연결 타임아웃 (기본값: 20000ms)
 * - DB_IDLE_TIMEOUT: 유휴 타임아웃 (기본값: 30000ms)
 * - APP_PORT: 애플리케이션 포트 (기본값: 8080)
 * - API_BASE_PATH: API 기본 경로 (기본값: /api/v1)
 * - CORS_ALLOWED_ORIGINS: CORS 허용 오리진 (기본값: *)
 * - CORS_ALLOWED_METHODS: CORS 허용 메서드 (기본값: GET,POST,PUT,DELETE,OPTIONS)
 * - COMPRESSION_ENABLED: 응답 압축 활성화 여부 (기본값: true)
 * - REDIS_HOST: Redis 호스트 (기본값: localhost)
 * - REDIS_PORT: Redis 포트 (기본값: 6379)
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
    
    /**
     * Redis 관련 환경 변수에 쉽게 접근하기 위한 객체
     */
    val redis: RedisConfig by lazy { RedisConfig(this) }
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

/**
 * Redis 관련 환경 변수를 위한 구성 클래스
 */
class RedisConfig(private val envConfig: EnvConfig) {
    val host: String get() = envConfig.get("REDIS_HOST", "localhost") ?: "localhost"
    val port: Int get() = envConfig.getInt("REDIS_PORT", 6379)
    val password: String? get() = envConfig.get("REDIS_PASSWORD")
    val database: Int get() = envConfig.getInt("REDIS_DATABASE", 0)
    val useSSL: Boolean get() = envConfig.getBoolean("REDIS_USE_SSL", false)
} 