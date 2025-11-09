package yousang.rest_server.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
@ConditionalOnProperty(name = ["spring.kafka.bootstrap-servers"])
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers:localhost:9092}")
    private lateinit var bootstrapServers: String

    companion object {
        // 기존 토픽
        const val USER_EVENTS_TOPIC = "user-events"
        const val AUDIT_EVENTS_TOPIC = "audit-events"
        const val NOTIFICATION_TOPIC = "notifications"

        // 트레이딩 시스템 토픽
        const val NEWS_ARTICLE_COLLECTED = "news.article.collected"
        const val NEWS_SENTIMENT_ANALYZED = "news.sentiment.analyzed"
        const val SOCIAL_POST_COLLECTED = "social.post.collected"

        const val TRADING_MARKET_PRICE = "trading.market.price"
        const val TRADING_MARKET_ORDERBOOK = "trading.market.orderbook"
        const val TRADING_MARKET_TRADE = "trading.market.trade"

        const val TRADING_SIGNAL_GENERATED = "trading.signal.generated"
        const val TRADING_ORDER_CREATED = "trading.order.created"
        const val TRADING_ORDER_FILLED = "trading.order.filled"
        const val TRADING_ORDER_CANCELLED = "trading.order.cancelled"
        const val TRADING_POSITION_OPENED = "trading.position.opened"
        const val TRADING_POSITION_CLOSED = "trading.position.closed"

        const val ANALYSIS_PREDICTION_COMPLETED = "analysis.prediction.completed"
        const val ANALYSIS_CORRELATION_UPDATED = "analysis.correlation.updated"
        const val ANALYSIS_TECHNICAL_UPDATED = "analysis.technical.updated"

        const val ALERT_PRICE_THRESHOLD = "alert.price.threshold"
        const val ALERT_RISK_WARNING = "alert.risk.warning"
        const val ALERT_TRADE_COMPLETED = "alert.trade.completed"
        const val ALERT_NOTIFICATION = "alert.notification"

        const val SYSTEM_ERROR = "system.error"
        const val SYSTEM_METRIC = "system.metric"
    }

    /**
     * Kafka Producer 설정
     */
    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to 3,
            ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to 1,
            ProducerConfig.COMPRESSION_TYPE_CONFIG to "snappy",
            ProducerConfig.BATCH_SIZE_CONFIG to 16384,
            ProducerConfig.LINGER_MS_CONFIG to 10,
            JsonSerializer.ADD_TYPE_INFO_HEADERS to false
        )
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory())
    }

    // ========== 기존 토픽 ==========

    @Bean
    fun userEventsTopic(): NewTopic {
        return TopicBuilder.name(USER_EVENTS_TOPIC)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun auditEventsTopic(): NewTopic {
        return TopicBuilder.name(AUDIT_EVENTS_TOPIC)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun notificationTopic(): NewTopic {
        return TopicBuilder.name(NOTIFICATION_TOPIC)
            .partitions(3)
            .replicas(1)
            .build()
    }

    // ========== 뉴스 토픽 ==========

    @Bean
    fun newsArticleCollectedTopic(): NewTopic {
        return TopicBuilder.name(NEWS_ARTICLE_COLLECTED)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun newsSentimentAnalyzedTopic(): NewTopic {
        return TopicBuilder.name(NEWS_SENTIMENT_ANALYZED)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun socialPostCollectedTopic(): NewTopic {
        return TopicBuilder.name(SOCIAL_POST_COLLECTED)
            .partitions(3)
            .replicas(1)
            .build()
    }

    // ========== 시장 데이터 토픽 ==========

    @Bean
    fun tradingMarketPriceTopic(): NewTopic {
        return TopicBuilder.name(TRADING_MARKET_PRICE)
            .partitions(6)
            .replicas(1)
            .build()
    }

    @Bean
    fun tradingMarketOrderbookTopic(): NewTopic {
        return TopicBuilder.name(TRADING_MARKET_ORDERBOOK)
            .partitions(6)
            .replicas(1)
            .build()
    }

    @Bean
    fun tradingMarketTradeTopic(): NewTopic {
        return TopicBuilder.name(TRADING_MARKET_TRADE)
            .partitions(6)
            .replicas(1)
            .build()
    }

    // ========== 거래 토픽 ==========

    @Bean
    fun tradingSignalGeneratedTopic(): NewTopic {
        return TopicBuilder.name(TRADING_SIGNAL_GENERATED)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun tradingOrderCreatedTopic(): NewTopic {
        return TopicBuilder.name(TRADING_ORDER_CREATED)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun tradingOrderFilledTopic(): NewTopic {
        return TopicBuilder.name(TRADING_ORDER_FILLED)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun tradingOrderCancelledTopic(): NewTopic {
        return TopicBuilder.name(TRADING_ORDER_CANCELLED)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun tradingPositionOpenedTopic(): NewTopic {
        return TopicBuilder.name(TRADING_POSITION_OPENED)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun tradingPositionClosedTopic(): NewTopic {
        return TopicBuilder.name(TRADING_POSITION_CLOSED)
            .partitions(3)
            .replicas(1)
            .build()
    }

    // ========== 분석 토픽 ==========

    @Bean
    fun analysisPredictionCompletedTopic(): NewTopic {
        return TopicBuilder.name(ANALYSIS_PREDICTION_COMPLETED)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun analysisCorrelationUpdatedTopic(): NewTopic {
        return TopicBuilder.name(ANALYSIS_CORRELATION_UPDATED)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun analysisTechnicalUpdatedTopic(): NewTopic {
        return TopicBuilder.name(ANALYSIS_TECHNICAL_UPDATED)
            .partitions(3)
            .replicas(1)
            .build()
    }

    // ========== 알림 토픽 ==========

    @Bean
    fun alertPriceThresholdTopic(): NewTopic {
        return TopicBuilder.name(ALERT_PRICE_THRESHOLD)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun alertRiskWarningTopic(): NewTopic {
        return TopicBuilder.name(ALERT_RISK_WARNING)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun alertTradeCompletedTopic(): NewTopic {
        return TopicBuilder.name(ALERT_TRADE_COMPLETED)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun alertNotificationTopic(): NewTopic {
        return TopicBuilder.name(ALERT_NOTIFICATION)
            .partitions(3)
            .replicas(1)
            .build()
    }

    // ========== 시스템 토픽 ==========

    @Bean
    fun systemErrorTopic(): NewTopic {
        return TopicBuilder.name(SYSTEM_ERROR)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun systemMetricTopic(): NewTopic {
        return TopicBuilder.name(SYSTEM_METRIC)
            .partitions(3)
            .replicas(1)
            .build()
    }
}
