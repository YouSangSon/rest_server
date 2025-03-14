package yousang.rest.shared.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 로깅 유틸리티 클래스
 */
object LoggingUtils {
    /**
     * ID 값을 마스킹
     * 예: "1234-5678-9012-3456" -> "1234-****-****-3456"
     */
    fun maskId(id: String?): String {
        if (id == null) return "null"
        if (id.length < 8) return id
        
        val firstPart = id.take(4)
        val lastPart = id.takeLast(4)
        return "$firstPart-${"*".repeat(id.length - 8)}-$lastPart"
    }
    
    /**
     * 개인정보를 마스킹
     * 예: "user@example.com" -> "u***@example.com"
     */
    fun maskEmail(email: String?): String {
        if (email == null) return "null"
        if (!email.contains("@")) return email
        
        val parts = email.split("@")
        val username = parts[0]
        val domain = parts[1]
        
        val maskedUsername = when {
            username.length <= 1 -> username
            username.length == 2 -> username.first() + "*"
            else -> username.first() + "*".repeat(username.length - 2) + username.last()
        }
        
        return "$maskedUsername@$domain"
    }
    
    /**
     * 전화번호 마스킹
     * 예: "010-1234-5678" -> "010-****-5678"
     */
    fun maskPhoneNumber(phoneNumber: String?): String {
        if (phoneNumber == null) return "null"
        if (phoneNumber.length < 7) return phoneNumber
        
        val pattern = Regex("""(\d{2,3})-?(\d{3,4})-?(\d{4})""")
        val matchResult = pattern.find(phoneNumber)
        
        return if (matchResult != null) {
            val (prefix, middle, suffix) = matchResult.destructured
            "$prefix-${"*".repeat(middle.length)}-$suffix"
        } else {
            // 패턴에 맞지 않는 경우 기본 마스킹
            phoneNumber.take(3) + "*".repeat(phoneNumber.length - 6) + phoneNumber.takeLast(3)
        }
    }
    
    /**
     * JSON 문자열에서 민감 정보를 마스킹
     */
    fun maskSensitiveJson(json: String): String {
        // 간단한 구현: 정규식으로 치환
        // 실제 프로덕션에서는 JSON 파싱 후 마스킹 처리하는 것이 더 정확함
        val patterns = mapOf(
            Regex(""""password"\s*:\s*"[^"]*"""") to """"password":"********"""",
            Regex(""""token"\s*:\s*"[^"]*"""") to """"token":"********"""",
            Regex(""""creditCardNumber"\s*:\s*"[^"]*"""") to """"creditCardNumber":"********""""
        )
        
        var result = json
        patterns.forEach { (pattern, replacement) ->
            result = result.replace(pattern, replacement)
        }
        
        return result
    }
    
    /**
     * 주어진 로거의 디버그 모드가 활성화되어 있을 때만 람다를 실행
     */
    inline fun ifDebugEnabled(logger: Logger, action: () -> Unit) {
        if (logger.isDebugEnabled) {
            action()
        }
    }
    
    /**
     * 주어진 로거의 트레이스 모드가 활성화되어 있을 때만 람다를 실행
     */
    inline fun ifTraceEnabled(logger: Logger, action: () -> Unit) {
        if (logger.isTraceEnabled) {
            action()
        }
    }
    
    /**
     * 로거 이름으로 로거 가져오기
     */
    fun getLogger(name: String): Logger {
        return LoggerFactory.getLogger(name)
    }
    
    /**
     * 클래스로 로거 가져오기
     */
    fun getLogger(clazz: Class<*>): Logger {
        return LoggerFactory.getLogger(clazz)
    }
} 