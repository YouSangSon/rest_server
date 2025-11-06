package yousang.rest_server.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.TopicBuilder

@Configuration
@EnableKafka
@ConditionalOnProperty(name = ["spring.kafka.bootstrap-servers"])
class KafkaConfig {

    companion object {
        const val USER_EVENTS_TOPIC = "user-events"
        const val AUDIT_EVENTS_TOPIC = "audit-events"
        const val NOTIFICATION_TOPIC = "notifications"
    }

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
}
