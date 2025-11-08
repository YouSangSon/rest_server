package yousang.rest_server.adapter.out.persistence.jpa.trading

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 트레이딩 전략 JPA Entity
 */
@Entity
@Table(name = "trading_strategies")
class TradingStrategyJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(name = "strategy_type", nullable = false, length = 50)
    val strategyType: String,

    @Column(nullable = false, columnDefinition = "jsonb")
    var config: String,  // JSON 문자열로 저장

    @Column(name = "is_active")
    var isActive: Boolean = false,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Config를 Map으로 파싱
     */
    fun getConfigAsMap(): Map<String, Any> {
        return try {
            ObjectMapper().readValue(config, Map::class.java) as Map<String, Any>
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Map을 Config JSON으로 설정
     */
    fun setConfigFromMap(configMap: Map<String, Any>) {
        config = ObjectMapper().writeValueAsString(configMap)
        updatedAt = LocalDateTime.now()
    }

    /**
     * 전략을 활성화/비활성화
     */
    fun toggleActive() {
        isActive = !isActive
        updatedAt = LocalDateTime.now()
    }
}

/**
 * 전략 타입 enum
 */
enum class StrategyType {
    SENTIMENT_BASED,    // 감성 기반
    TECHNICAL,          // 기술적 지표 기반
    ML_PREDICTION,      // 머신러닝 예측 기반
    HYBRID,             // 복합 전략
    ARBITRAGE           // 차익거래
}
