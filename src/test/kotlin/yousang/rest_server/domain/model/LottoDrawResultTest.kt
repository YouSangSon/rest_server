package yousang.rest_server.domain.model

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 로또 추첨 결과 도메인 모델 테스트
 */
class LottoDrawResultTest {

    @Test
    fun `1등 당첨 확인 - 6개 모두 일치`() {
        // given
        val drawResult = LottoDrawResult(
            drawNumber = 1000,
            winningNumbers = listOf(1, 2, 3, 4, 5, 6),
            bonusNumber = 7,
            drawDate = LocalDateTime.now(),
            firstPrizeAmount = 2_000_000_000L
        )
        val ticket = LottoTicket.createManual(1L, 1000, listOf(1, 2, 3, 4, 5, 6))

        // when
        val rank = drawResult.calculateRank(ticket)

        // then
        assertEquals(1, rank)
    }

    @Test
    fun `2등 당첨 확인 - 5개 + 보너스 일치`() {
        // given
        val drawResult = LottoDrawResult(
            drawNumber = 1000,
            winningNumbers = listOf(1, 2, 3, 4, 5, 6),
            bonusNumber = 7,
            drawDate = LocalDateTime.now()
        )
        val ticket = LottoTicket.createManual(1L, 1000, listOf(1, 2, 3, 4, 5, 7)) // 7은 보너스

        // when
        val rank = drawResult.calculateRank(ticket)

        // then
        assertEquals(2, rank)
    }

    @Test
    fun `3등 당첨 확인 - 5개 일치`() {
        // given
        val drawResult = LottoDrawResult(
            drawNumber = 1000,
            winningNumbers = listOf(1, 2, 3, 4, 5, 6),
            bonusNumber = 7,
            drawDate = LocalDateTime.now()
        )
        val ticket = LottoTicket.createManual(1L, 1000, listOf(1, 2, 3, 4, 5, 10))

        // when
        val rank = drawResult.calculateRank(ticket)

        // then
        assertEquals(3, rank)
    }

    @Test
    fun `4등 당첨 확인 - 4개 일치`() {
        // given
        val drawResult = LottoDrawResult(
            drawNumber = 1000,
            winningNumbers = listOf(1, 2, 3, 4, 5, 6),
            bonusNumber = 7,
            drawDate = LocalDateTime.now()
        )
        val ticket = LottoTicket.createManual(1L, 1000, listOf(1, 2, 3, 4, 10, 11))

        // when
        val rank = drawResult.calculateRank(ticket)

        // then
        assertEquals(4, rank)
    }

    @Test
    fun `5등 당첨 확인 - 3개 일치`() {
        // given
        val drawResult = LottoDrawResult(
            drawNumber = 1000,
            winningNumbers = listOf(1, 2, 3, 4, 5, 6),
            bonusNumber = 7,
            drawDate = LocalDateTime.now()
        )
        val ticket = LottoTicket.createManual(1L, 1000, listOf(1, 2, 3, 10, 11, 12))

        // when
        val rank = drawResult.calculateRank(ticket)

        // then
        assertEquals(5, rank)
    }

    @Test
    fun `미당첨 확인 - 2개 이하 일치`() {
        // given
        val drawResult = LottoDrawResult(
            drawNumber = 1000,
            winningNumbers = listOf(1, 2, 3, 4, 5, 6),
            bonusNumber = 7,
            drawDate = LocalDateTime.now()
        )
        val ticket = LottoTicket.createManual(1L, 1000, listOf(1, 2, 10, 11, 12, 13))

        // when
        val rank = drawResult.calculateRank(ticket)

        // then
        assertNull(rank)
    }

    @Test
    fun `등수별 예상 상금 계산`() {
        // given
        val drawResult = LottoDrawResult(
            drawNumber = 1000,
            winningNumbers = listOf(1, 2, 3, 4, 5, 6),
            bonusNumber = 7,
            drawDate = LocalDateTime.now(),
            firstPrizeAmount = 2_000_000_000L
        )

        // when & then
        assertEquals(2_000_000_000L, drawResult.getEstimatedPrizeAmount(1))
        assertEquals(200_000_000L, drawResult.getEstimatedPrizeAmount(2))
        assertEquals(1_500_000L, drawResult.getEstimatedPrizeAmount(3))
        assertEquals(50_000L, drawResult.getEstimatedPrizeAmount(4))
        assertEquals(5_000L, drawResult.getEstimatedPrizeAmount(5))
    }
}
