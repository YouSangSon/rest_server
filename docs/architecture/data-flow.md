# 데이터 흐름 및 상호작용

## 🔄 데이터 흐름 개요

이 문서는 REST Server의 데이터 흐름과 각 계층 간의 상호작용을 상세히 설명합니다. 헥사고날 아키텍처에서 데이터가 어떻게 흘러가는지, 각 컴포넌트가 어떤 역할을 하는지 파악할 수 있습니다.

## 📊 전체 데이터 흐름도

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │    │   External      │    │   Message       │
│                 │    │   API Service   │    │   Broker        │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          │ HTTP/HTTPS           │ HTTP/HTTPS           │ Kafka
          │ gRPC                 │ gRPC                 │ WebSocket
          │ WebSocket            │                      │
          ▼                      ▼                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                    인터페이스 계층 (Interface Layer)              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ REST        │  │ gRPC        │  │ WebSocket   │            │
│  │ Controller  │  │ Service     │  │ Handler     │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
│           │              │              │                      │
│           ▼              ▼              ▼                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ Request     │  │ Protocol    │  │ Message     │            │
│  │ Validation  │  │ Buffer      │  │ Router      │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                  애플리케이션 계층 (Application Layer)            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ Use Case    │  │ Application │  │ Command/    │            │
│  │ Service     │  │ Service     │  │ Query       │            │
│  └─────────────┘  └─────────────┘  │ Handler     │            │
│           │              │         └─────────────┘            │
│           ▼              ▼                                    │
│  ┌─────────────┐  ┌─────────────┐                            │
│  │ Business    │  │ Transaction │                            │
│  │ Logic       │  │ Management  │                            │
│  └─────────────┘  └─────────────┘                            │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                     도메인 계층 (Domain Layer)                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ Domain      │  │ Domain      │  │ Repository  │            │
│  │ Entity      │  │ Service     │  │ Interface   │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
│           │              │              │                      │
│           ▼              ▼              ▼                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ Business    │  │ Validation  │  │ Data        │            │
│  │ Rules       │  │ Rules       │  │ Contract    │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                   인프라 계층 (Infrastructure Layer)             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ Repository  │  │ External    │  │ Message     │            │
│  │ Impl        │  │ Service     │  │ Queue       │            │
│  └─────────────┘  │ Client      │  └─────────────┘            │
│           │       └─────────────┘            │                │
│           ▼              ▼                   ▼                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ Database    │  │ HTTP Client │  │ Kafka       │            │
│  │ Connection  │  │ gRPC Client │  │ Producer    │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                    데이터 계층 (Data Layer)                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ PostgreSQL  │  │ Redis       │  │ File        │            │
│  │ Database    │  │ Cache       │  │ Storage     │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└─────────────────────────────────────────────────────────────────┘
```

## 🔄 상세 데이터 흐름

### 1. HTTP REST API 요청 처리

```
1. Client Request
   ↓
2. Spring WebFlux Router
   ↓
3. Controller Method
   ↓
4. Request Validation (Bean Validation)
   ↓
5. Use Case Service
   ↓
6. Domain Service (Business Logic)
   ↓
7. Repository Interface
   ↓
8. Repository Implementation
   ↓
9. Database Query (Exposed ORM)
   ↓
10. Response Mapping (Entity → DTO)
    ↓
11. Controller Response
    ↓
12. Client Response
```

### 2. gRPC 요청 처리

```
1. gRPC Client Request
   ↓
2. gRPC Server (Netty)
   ↓
3. gRPC Service Implementation
   ↓
4. Protocol Buffer Deserialization
   ↓
5. Use Case Service
   ↓
6. Domain Service
   ↓
7. Repository Layer
   ↓
8. Database Operation
   ↓
9. Response Serialization (Protocol Buffer)
    ↓
10. gRPC Response
```

### 3. WebSocket 메시지 처리

```
1. WebSocket Connection
   ↓
2. Message Handler
   ↓
3. Message Validation
   ↓
4. Business Logic Processing
   ↓
5. Database Update
   ↓
6. Broadcast to Connected Clients
   ↓
7. WebSocket Response
```

## 📝 데이터 변환 흐름

### 1. Request → Entity 변환

```kotlin
// 1. DTO에서 Entity로 변환
@PostMapping("/lotto")
suspend fun createLotto(@RequestBody dto: LottoDto): ResponseEntity<ApiResponse<LottoDto>> {
    return try {
        // DTO → Entity 변환
        val entity = LottoEntity.new {
            drwNo = dto.drwNo
            drwNoDate = dto.drwNoDate.toKotlinLocalDate()
            // ... 기타 필드들
        }
        
        // 비즈니스 로직 처리
        val savedEntity = lottoService.createLotto(entity)
        
        // Entity → DTO 변환하여 응답
        ResponseEntity.ok(ApiResponse(
            statusCode = HttpStatus.OK.value(),
            message = "로또 정보가 성공적으로 생성되었습니다.",
            data = savedEntity.toDto()
        ))
    } catch (e: Exception) {
        // 예외 처리
        throw e
    }
}
```

### 2. Entity → DTO 변환

```kotlin
// Entity에서 DTO로 변환
fun LottoEntity.toDto(): LottoDto {
    return LottoDto(
        id = id.value,
        drwNo = drwNo,
        drwNoDate = drwNoDate.toJavaLocalDate(),
        drwtNo1 = drwtNo1,
        drwtNo2 = drwtNo2,
        drwtNo3 = drwtNo3,
        drwtNo4 = drwtNo4,
        drwtNo5 = drwtNo5,
        drwtNo6 = drwtNo6,
        bnusNo = bnusNo,
        firstPrzwnerCo = firstPrzwnerCo,
        firstAccumamnt = firstAccumamnt,
        firstWinamnt = firstWinamnt,
        totSellamnt = totSellamnt,
        returnValue = ""
    )
}
```

## 🔐 데이터 보안 흐름

### 1. 인증 및 권한 확인

```
1. Request with JWT Token
   ↓
2. Spring Security Filter Chain
   ↓
3. JWT Token Validation
   ↓
4. User Authentication
   ↓
5. Role/Authority Check
   ↓
6. Controller Method Execution
   ↓
7. Business Logic Processing
```

### 2. 데이터 암호화

```
1. Sensitive Data Input
   ↓
2. Encryption Service
   ↓
3. Encrypted Data Storage
   ↓
4. Data Retrieval
   ↓
5. Decryption Service
   ↓
6. Decrypted Data Response
```

## 📊 데이터베이스 상호작용

### 1. 읽기 작업 (Read Operations)

```kotlin
// Repository Interface
interface LottoRepository {
    suspend fun findById(id: Long): LottoEntity?
    suspend fun findByDrwNo(drwNo: Int): LottoEntity?
    suspend fun findAll(): List<LottoEntity>
}

// Repository Implementation
class LottoRepositoryImpl : LottoRepository {
    override suspend fun findById(id: Long): LottoEntity? {
        return transaction {
            LottoEntity.findById(id)
        }
    }
    
    override suspend fun findByDrwNo(drwNo: Int): LottoEntity? {
        return transaction {
            LottoEntity.find { LottoTable.drwNo eq drwNo }.firstOrNull()
        }
    }
}
```

### 2. 쓰기 작업 (Write Operations)

```kotlin
// Service Layer
@Service
class LottoServiceImpl(private val lottoRepository: LottoRepository) : LottoService {
    
    @Transactional
    override suspend fun createLotto(lotto: LottoEntity): LottoEntity {
        // 비즈니스 로직 검증
        validateLottoData(lotto)
        
        // 데이터베이스 저장
        return lottoRepository.save(lotto)
    }
    
    @Transactional
    override suspend fun updateLotto(id: Long, lotto: LottoEntity): LottoEntity {
        val existingLotto = lottoRepository.findById(id)
            ?: throw EntityNotFoundException("로또 정보를 찾을 수 없습니다: $id")
        
        // 기존 데이터 업데이트
        existingLotto.applyFromDto(lotto.toDto())
        return lottoRepository.save(existingLotto)
    }
}
```

## 🔄 트랜잭션 관리

### 1. 트랜잭션 경계

```kotlin
// Use Case Layer에서 트랜잭션 관리
@UseCase
class LottoUseCase(
    private val lottoService: LottoService,
    private val lottoRepository: LottoRepository
) {
    
    @Transactional
    suspend fun processLottoData(lottoData: List<LottoDto>): List<LottoDto> {
        val results = mutableListOf<LottoDto>()
        
        lottoData.forEach { dto ->
            try {
                val entity = dto.toEntity()
                val savedEntity = lottoService.createLotto(entity)
                results.add(savedEntity.toDto())
            } catch (e: Exception) {
                // 개별 실패는 로깅하고 계속 진행
                logger.error("로또 데이터 처리 실패: ${dto.drwNo}", e)
            }
        }
        
        return results
    }
}
```

### 2. 트랜잭션 격리 수준

```kotlin
// Repository에서 트랜잭션 격리 수준 설정
@Repository
class LottoRepositoryImpl : LottoRepository {
    
    @Transactional(isolation = Isolation.READ_COMMITTED)
    override suspend fun findForUpdate(id: Long): LottoEntity? {
        return transaction {
            LottoEntity.find { LottoTable.id eq id }
                .forUpdate()
                .firstOrNull()
        }
    }
}
```

## 📈 성능 최적화 데이터 흐름

### 1. 캐싱 전략

```
1. Data Request
   ↓
2. Cache Check (Redis)
   ↓
3. Cache Hit? → Return Cached Data
   ↓
4. Cache Miss → Database Query
   ↓
5. Data Processing
   ↓
6. Cache Update
   ↓
7. Response
```

### 2. 배치 처리

```kotlin
// 대량 데이터 배치 처리
@Transactional
suspend fun batchInsertLottoData(lottoDataList: List<LottoDto>) {
    val batchSize = 1000
    lottoDataList.chunked(batchSize).forEach { batch ->
        transaction {
            batch.forEach { dto ->
                val entity = dto.toEntity()
                LottoEntity.new {
                    drwNo = entity.drwNo
                    drwNoDate = entity.drwNoDate
                    // ... 기타 필드들
                }
            }
        }
    }
}
```

## 🚨 에러 처리 데이터 흐름

### 1. 예외 처리 흐름

```
1. Exception Occurred
   ↓
2. Global Exception Handler
   ↓
3. Exception Classification
   ↓
4. Logging (Structured Log)
   ↓
5. Error Response Generation
   ↓
6. Client Error Response
```

### 2. 재시도 메커니즘

```kotlin
// 재시도 가능한 작업에 대한 처리
@Retryable(
    value = [DataAccessException::class],
    maxAttempts = 3,
    backoff = Backoff(delay = 1000)
)
suspend fun retryableDatabaseOperation(): Result<Data> {
    return try {
        // 데이터베이스 작업
        val result = performDatabaseOperation()
        Result.success(result)
    } catch (e: DataAccessException) {
        logger.warn("데이터베이스 작업 실패, 재시도 예정: ${e.message}")
        throw e
    }
}
```

## 🔍 모니터링 및 로깅 데이터 흐름

### 1. 요청 추적

```
1. Request Received
   ↓
2. Correlation ID Generation
   ↓
3. Request Logging
   ↓
4. Performance Metrics Collection
   ↓
5. Business Logic Execution
   ↓
6. Response Logging
   ↓
7. Metrics Aggregation
```

### 2. 성능 모니터링

```kotlin
// AOP를 통한 성능 모니터링
@Aspect
@Component
class PerformanceMonitoringAspect {
    
    @Around("@annotation(Monitored)")
    fun monitorPerformance(joinPoint: ProceedingJoinPoint): Any? {
        val startTime = System.currentTimeMillis()
        val methodName = joinPoint.signature.name
        
        return try {
            val result = joinPoint.proceed()
            val executionTime = System.currentTimeMillis() - startTime
            
            // 성능 메트릭 기록
            recordPerformanceMetric(methodName, executionTime)
            
            result
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            recordErrorMetric(methodName, executionTime, e)
            throw e
        }
    }
}
```

---

**문서 버전**: v1.0.0  
**마지막 업데이트**: 2024-12-19  
**작성자**: Development Team
