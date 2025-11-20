package yousang.rest_server.adapter.out.persistence.sns.entity

import jakarta.persistence.*
import yousang.rest_server.domain.sns.Conversation
import java.time.LocalDateTime

@Entity
@Table(
    name = "sns_conversations",
    indexes = [
        Index(name = "idx_participant1_id", columnList = "participant1_id"),
        Index(name = "idx_participant2_id", columnList = "participant2_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_participants",
            columnNames = ["participant1_id", "participant2_id"]
        )
    ]
)
class ConversationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    val conversationId: Long = 0,

    @Column(name = "participant1_id", nullable = false)
    val participant1Id: Long,

    @Column(name = "participant2_id", nullable = false)
    val participant2Id: Long,

    @Column(name = "last_message_at")
    val lastMessageAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Conversation = Conversation(
        conversationId = conversationId,
        participant1Id = participant1Id,
        participant2Id = participant2Id,
        lastMessageAt = lastMessageAt,
        createdAt = createdAt
    )

    companion object {
        fun from(domain: Conversation): ConversationEntity = ConversationEntity(
            conversationId = domain.conversationId,
            participant1Id = domain.participant1Id,
            participant2Id = domain.participant2Id,
            lastMessageAt = domain.lastMessageAt,
            createdAt = domain.createdAt
        )
    }
}
