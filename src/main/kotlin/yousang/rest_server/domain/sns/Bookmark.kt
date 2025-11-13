package yousang.rest_server.domain.sns

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 북마크 도메인 모델
 * 사용자가 저장한 게시물이나 투자 게시물
 */
data class Bookmark(
    val bookmarkId: Long,
    val userId: Long,
    val contentType: BookmarkContentType,
    val contentId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * 북마크 콘텐츠 유형
 */
enum class BookmarkContentType {
    POST,               // 일반 게시물
    INVESTMENT_POST     // 투자 게시물
}

/**
 * 워치리스트 항목 도메인 모델
 * 사용자가 모니터링하는 자산 및 가격 알림
 */
data class WatchlistItem(
    val watchlistId: Long,
    val userId: Long,
    val assetType: AssetType,
    val symbol: String,
    val alertConditions: List<AlertCondition> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(symbol.isNotBlank()) { "Symbol cannot be blank" }
    }

    /**
     * 알림 조건 추가
     */
    fun addAlertCondition(condition: AlertCondition): WatchlistItem {
        return copy(
            alertConditions = alertConditions + condition,
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * 알림 조건 제거
     */
    fun removeAlertCondition(conditionId: String): WatchlistItem {
        return copy(
            alertConditions = alertConditions.filterNot { it.id == conditionId },
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * 활성화/비활성화
     */
    fun setActive(active: Boolean): WatchlistItem {
        return copy(isActive = active, updatedAt = LocalDateTime.now())
    }

    /**
     * 가격 알림 확인
     */
    fun checkAlerts(currentPrice: BigDecimal): List<AlertCondition> {
        return alertConditions.filter { it.isSatisfied(currentPrice) }
    }
}

/**
 * 알림 조건
 */
data class AlertCondition(
    val id: String,
    val type: AlertConditionType,
    val value: BigDecimal
) {
    /**
     * 조건 충족 여부 확인
     */
    fun isSatisfied(currentPrice: BigDecimal): Boolean {
        return when (type) {
            AlertConditionType.ABOVE -> currentPrice >= value
            AlertConditionType.BELOW -> currentPrice <= value
            AlertConditionType.CHANGE_PERCENT -> {
                // 변동률 체크는 이전 가격 정보가 필요하므로 Service 레이어에서 처리
                false
            }
        }
    }
}

/**
 * 알림 조건 유형
 */
enum class AlertConditionType {
    ABOVE,              // 가격이 특정 값 이상
    BELOW,              // 가격이 특정 값 이하
    CHANGE_PERCENT      // 변동률이 특정 % 이상/이하
}

/**
 * 알림 도메인 모델
 */
data class Notification(
    val notificationId: Long,
    val userId: Long,
    val notificationType: NotificationType,
    val sourceUserId: Long? = null,
    val relatedContentType: String? = null,
    val relatedContentId: Long? = null,
    val title: String,
    val message: String,
    val dataPayload: Map<String, Any> = emptyMap(),
    val isRead: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(message.isNotBlank()) { "Message cannot be blank" }
    }

    /**
     * 읽음 표시
     */
    fun markAsRead(): Notification = copy(isRead = true)
}

/**
 * 알림 유형
 */
enum class NotificationType {
    LIKE,               // 좋아요
    COMMENT,            // 댓글
    FOLLOW,             // 팔로우
    PRICE_ALERT,        // 가격 알림
    PORTFOLIO_UPDATE,   // 포트폴리오 업데이트
    MESSAGE,            // 메시지
    SYSTEM              // 시스템 알림
}
