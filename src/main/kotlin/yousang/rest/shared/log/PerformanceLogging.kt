package yousang.rest.shared.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer
import kotlin.system.measureNanoTime

/**
 * 성능 모니터링을 위한 로깅 유틸리티
 */
object PerformanceLogging {
    private val logger: Logger = LoggerFactory.getLogger(PerformanceLogging::class.java)
    private val metrics = ConcurrentHashMap<String, PerformanceMetric>()
    
    // 초기화 시 주기적으로 메트릭 출력하는 타이머 설정
    init {
        timer(name = "performance-metrics-logger", daemon = true, period = 60_000L) {
            logAllMetrics()
        }
    }
    
    /**
     * 성능 지표 클래스
     */
    data class PerformanceMetric(
        val name: String,
        @Volatile var count: Long = 0,
        @Volatile var totalTimeNs: Long = 0,
        @Volatile var minTimeNs: Long = Long.MAX_VALUE,
        @Volatile var maxTimeNs: Long = 0
    ) {
        fun update(timeNs: Long) {
            count++
            totalTimeNs += timeNs
            minTimeNs = minTimeNs.coerceAtMost(timeNs)
            maxTimeNs = maxTimeNs.coerceAtLeast(timeNs)
        }
        
        val avgTimeMs: Double
            get() = if (count > 0) totalTimeNs.toDouble() / count / 1_000_000 else 0.0
        
        val minTimeMs: Double
            get() = minTimeNs.toDouble() / 1_000_000
            
        val maxTimeMs: Double
            get() = maxTimeNs.toDouble() / 1_000_000
    }
    
    /**
     * 지정된 작업의 실행 시간을 측정하고 기록
     */
    inline fun <T> measure(metricName: String, block: () -> T): T {
        var result: T
        val time = measureNanoTime {
            result = block()
        }
        recordMetric(metricName, time)
        return result
    }
    
    /**
     * 실행 시간 메트릭 기록
     */
    fun recordMetric(metricName: String, timeNs: Long) {
        metrics.computeIfAbsent(metricName) { PerformanceMetric(it) }.update(timeNs)
    }
    
    /**
     * 모든 메트릭 로깅
     */
    fun logAllMetrics() {
        if (metrics.isEmpty()) return
        
        logger.info("=== 성능 메트릭 요약 ===")
        metrics.values.forEach { metric ->
            logger.info("${metric.name}: 호출 ${metric.count}회, 평균: ${metric.avgTimeMs}ms, " +
                    "최소: ${metric.minTimeMs}ms, 최대: ${metric.maxTimeMs}ms")
        }
    }
    
    /**
     * 특정 메트릭 로깅
     */
    fun logMetric(metricName: String) {
        metrics[metricName]?.let { metric ->
            logger.info("${metric.name}: 호출 ${metric.count}회, 평균: ${metric.avgTimeMs}ms, " +
                    "최소: ${metric.minTimeMs}ms, 최대: ${metric.maxTimeMs}ms")
        } ?: logger.info("메트릭 없음: $metricName")
    }
    
    /**
     * 모든 메트릭 초기화
     */
    fun resetAllMetrics() {
        metrics.clear()
    }
    
    /**
     * 특정 메트릭 초기화
     */
    fun resetMetric(metricName: String) {
        metrics.remove(metricName)
    }
} 