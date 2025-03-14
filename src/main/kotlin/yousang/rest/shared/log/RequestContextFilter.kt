package yousang.rest.shared.utils

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.util.UUID

/**
 * 요청 정보를 MDC에 저장하는 필터
 * Microk8s Observability와 함께 사용하기 위한 로그 컨텍스트 제공
 * 디버깅을 위한 요청/응답 로깅 기능 추가
 */
@Component("customRequestContextFilter")
class CustomRequestContextFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 요청/응답 내용 캐싱을 위한 래퍼
        val cachingRequest = ContentCachingRequestWrapper(request)
        val cachingResponse = ContentCachingResponseWrapper(response)
        
        try {
            // 요청 ID 생성 및 설정
            val requestId = UUID.randomUUID().toString()
            MDC.put("requestId", requestId)
            cachingResponse.addHeader("X-Request-ID", requestId)
            
            // 클라이언트 IP 설정
            MDC.put("clientIp", cachingRequest.remoteAddr)
            
            // 요청 URI 설정
            val requestURI = cachingRequest.requestURI
            MDC.put("requestUri", requestURI)
            
            // 사용자 ID 설정 (인증된 경우)
            cachingRequest.userPrincipal?.name?.let { MDC.put("userId", it) }
            
            // 요청 시작 시간 기록
            val startTime = System.currentTimeMillis()
            
            // 요청 시작 로그
            if (isDebugLoggingEnabled(requestURI)) {
                logger.info("Request started: ${cachingRequest.method} ${cachingRequest.requestURI}")
                logRequestDetails(cachingRequest)
            } else {
                logger.info("Request started: ${cachingRequest.method} ${cachingRequest.requestURI}")
            }
            
            // 필터 체인 실행
            filterChain.doFilter(cachingRequest, cachingResponse)
            
            // 응답 복사 (내용이 소비되므로 반드시 필요)
            cachingResponse.copyBodyToResponse()
            
            // 실행 시간 계산 및 기록
            val executionTime = System.currentTimeMillis() - startTime
            MDC.put("executionTime", executionTime.toString())
            
            // 요청 종료 로그
            if (isDebugLoggingEnabled(requestURI)) {
                logger.info("Request completed: ${cachingRequest.method} ${cachingRequest.requestURI} - Status: ${cachingResponse.status} - Time: ${executionTime}ms")
                logResponseDetails(cachingResponse)
            } else {
                logger.info("Request completed: ${cachingRequest.method} ${cachingRequest.requestURI} - Status: ${cachingResponse.status} - Time: ${executionTime}ms")
            }
        } finally {
            // MDC 정리
            MDC.clear()
        }
    }
    
    /**
     * 디버그 로깅이 필요한 URI인지 확인
     */
    private fun isDebugLoggingEnabled(uri: String): Boolean {
        // 디버그 로깅이 필요한 API 패턴 설정
        // 예: /api/v1/debug/로 시작하는 경로만 자세한 로깅
        // 또는 프로파일을 기반으로 설정할 수도 있음
        return logger.isDebugEnabled && (
                uri.startsWith("/api/") || 
                uri.startsWith("/lotto/") ||
                uri.contains("debug")
        )
    }
    
    /**
     * 요청 상세 정보 로깅
     */
    private fun logRequestDetails(request: ContentCachingRequestWrapper) {
        try {
            // 헤더 정보 로깅
            val headers = request.headerNames.asSequence().associateWith { request.getHeader(it) }
            logger.debug("Request headers: $headers")
            
            // 페이로드 로깅 (POST/PUT에만)
            if (request.method in listOf("POST", "PUT", "PATCH")) {
                val contentType = request.contentType ?: ""
                if (contentType.contains("application/json") || contentType.contains("application/xml") || 
                    contentType.contains("text/")) {
                    val content = request.contentAsByteArray.toString(Charsets.UTF_8)
                    if (content.isNotBlank()) {
                        logger.debug("Request payload: $content")
                    }
                } else {
                    logger.debug("Request with binary or unsupported content type: $contentType")
                }
            }
        } catch (e: Exception) {
            logger.warn("Error logging request details", e)
        }
    }
    
    /**
     * 응답 상세 정보 로깅
     */
    private fun logResponseDetails(response: ContentCachingResponseWrapper) {
        try {
            // 헤더 정보 로깅
            val headers = response.headerNames.associateWith { response.getHeaders(it) }
            logger.debug("Response headers: $headers")
            
            // 페이로드 로깅 (성공 응답만)
            val status = response.status
            if (status in 200..299) {
                val contentType = response.contentType ?: ""
                if (contentType.contains("application/json") || contentType.contains("application/xml") || 
                    contentType.contains("text/")) {
                    val content = response.contentAsByteArray.toString(Charsets.UTF_8)
                    if (content.isNotBlank()) {
                        // 긴 응답은 요약해서 로깅
                        if (content.length > 1000) {
                            logger.debug("Response payload: ${content.take(997)}...")
                        } else {
                            logger.debug("Response payload: $content")
                        }
                    }
                } else {
                    logger.debug("Response with binary or unsupported content type: $contentType")
                }
            }
        } catch (e: Exception) {
            logger.warn("Error logging response details", e)
        }
    }
} 