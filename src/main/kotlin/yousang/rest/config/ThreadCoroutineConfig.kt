package yousang.rest.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

/**
 * 스레드 및 코루틴 관련 구성
 * - 코루틴 디스패처 설정
 * - 스레드 풀 설정
 * - 가상 스레드 설정 (Java 21)
 */
@Configuration
class ThreadCoroutineConfig {

    /**
     * 가상 스레드 기반 코루틴 디스패처
     * Java 21의 가상 스레드를 활용하여 IO 작업에 최적화된 디스패처 제공
     */
    @Bean
    fun virtualThreadCoroutineDispatcher(): CoroutineContext {
        return Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
    }
    
    /**
     * IO 작업에 최적화된 코루틴 디스패처
     * 오래된 하드웨어에서도 효율적으로 동작하도록 설계
     */
    @Bean
    fun optimizedIoDispatcher(): CoroutineContext {
        // IO 작업은 많은 대기 시간이 있으므로 코어 수의 몇 배 더 많은 스레드 사용
        val availableProcessors = Runtime.getRuntime().availableProcessors()
        val optimalThreads = (availableProcessors * 4).coerceAtLeast(8)
        
        return ThreadPoolExecutor(
            optimalThreads / 2, // 코어 풀 크기
            optimalThreads,      // 최대 풀 크기
            60L, TimeUnit.SECONDS, // 유휴 스레드 제거 시간
            LinkedBlockingQueue(1000), // 작업 큐
            ThreadPoolExecutor.CallerRunsPolicy() // 풀이 포화되면 호출 스레드에서 실행
        ).asCoroutineDispatcher()
    }
    
    /**
     * CPU 집약적 작업에 최적화된 코루틴 디스패처
     * 오래된 하드웨어에서도 CPU 자원을 효율적으로 활용
     */
    @Bean
    fun optimizedCpuDispatcher(): CoroutineContext {
        // CPU 집약적 작업은 코어 수에 맞춰 스레드 생성
        val availableProcessors = Runtime.getRuntime().availableProcessors()
        
        return ThreadPoolExecutor(
            availableProcessors,    // 코어 풀 크기
            availableProcessors,    // 최대 풀 크기 
            60L, TimeUnit.SECONDS,  // 유휴 스레드 제거 시간
            LinkedBlockingQueue(100), // 작업 큐
            ThreadPoolExecutor.CallerRunsPolicy() // 풀이 포화되면 호출 스레드에서 실행
        ).asCoroutineDispatcher()
    }
    
    /**
     * 비동기 작업을 위한 스레드 풀 태스크 실행기
     */
    @Bean
    fun asyncTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        val availableProcessors = Runtime.getRuntime().availableProcessors()
        
        executor.corePoolSize = availableProcessors
        executor.maxPoolSize = availableProcessors * 2
        executor.queueCapacity = 500
        executor.setThreadNamePrefix("async-")
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        
        // 대기 중인 작업이 모두 완료되면 종료하도록 설정
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(60)
        
        return executor
    }
} 