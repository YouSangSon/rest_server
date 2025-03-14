package yousang.rest.shared.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import yousang.rest.shared.log.log
import kotlin.coroutines.CoroutineContext

/**
 * 코루틴 유틸리티 컴포넌트
 * 최적화된 디스패처를 활용해 효율적인 비동기 처리를 지원합니다.
 */
@Component
class CoroutineContextProvider(
    @Qualifier("virtualThreadCoroutineDispatcher") 
    val virtualThreadDispatcher: CoroutineContext,
    
    @Qualifier("optimizedIoDispatcher") 
    val ioDispatcher: CoroutineContext,
    
    @Qualifier("optimizedCpuDispatcher") 
    val cpuDispatcher: CoroutineContext
)

// 싱글톤 인스턴스로 코루틴 컨텍스트 제공자 접근
private lateinit var contextProvider: CoroutineContextProvider

@Autowired
fun setContextProvider(provider: CoroutineContextProvider) {
    contextProvider = provider
}

/**
 * 데이터베이스 작업을 코루틴 컨텍스트에서 실행하기 위한 유틸리티 함수
 * 이 함수는 트랜잭션 내에서 비동기 작업을 수행합니다.
 * 
 * @param block 실행할 비동기 코드 블록
 * @return 블록의 실행 결과
 */
suspend fun <T> dbQuery(block: suspend () -> T): T =
    try {
        if (::contextProvider.isInitialized) {
            newSuspendedTransaction(contextProvider.virtualThreadDispatcher) { 
                block() 
            }
        } else {
            // 폴백: 초기화되지 않은 경우 기본 디스패처 사용
            newSuspendedTransaction(Dispatchers.IO) { 
                block() 
            }
        }
    } catch (e: Exception) {
        log.error("Database query error: ${e.message}", e)
        throw e
    }

/**
 * IO 작업을 코루틴 컨텍스트에서 실행하기 위한 유틸리티 함수
 * 
 * @param block 실행할 IO 작업 블록
 * @return 블록의 실행 결과
 */
suspend fun <T> ioContext(block: suspend () -> T): T =
    try {
        if (::contextProvider.isInitialized) {
            withContext(contextProvider.ioDispatcher) {
                block()
            }
        } else {
            // 폴백: 초기화되지 않은 경우 기본 디스패처 사용
            withContext(Dispatchers.IO) {
                block()
            }
        }
    } catch (e: Exception) {
        log.error("IO operation error: ${e.message}", e)
        throw e
    }

/**
 * CPU 집약적 작업을 코루틴 컨텍스트에서 실행하기 위한 유틸리티 함수
 * 
 * @param block 실행할 CPU 작업 블록
 * @return 블록의 실행 결과
 */
suspend fun <T> cpuContext(block: suspend () -> T): T =
    try {
        if (::contextProvider.isInitialized) {
            withContext(contextProvider.cpuDispatcher) {
                block()
            }
        } else {
            // 폴백: 초기화되지 않은 경우 기본 디스패처 사용
            withContext(Dispatchers.Default) {
                block()
            }
        }
    } catch (e: Exception) {
        log.error("CPU operation error: ${e.message}", e)
        throw e
    }

/**
 * 여러 비동기 작업을 병렬로 실행하는 유틸리티 함수
 * 
 * @param block 여러 async 작업을 포함하는 코루틴 스코프 블록
 * @return 병렬 실행 결과
 */
suspend fun <T> parallel(block: suspend () -> T): T =
    coroutineScope {
        try {
            block()
        } catch (e: Exception) {
            log.error("Error in parallel execution: ${e.message}", e)
            throw e
        }
    }

/**
 * 두 개의 비동기 작업을 병렬로 실행하고 결과를 쌍으로 반환
 * 
 * @param first 첫 번째 비동기 작업
 * @param second 두 번째 비동기 작업
 * @return 두 작업의 결과 쌍
 */
suspend fun <A, B> parallelPair(
    first: suspend () -> A,
    second: suspend () -> B
): Pair<A, B> = coroutineScope {
    val deferredFirst = async { first() }
    val deferredSecond = async { second() }
    Pair(deferredFirst.await(), deferredSecond.await())
} 