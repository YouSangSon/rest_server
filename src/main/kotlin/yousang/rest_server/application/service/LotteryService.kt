package yousang.rest_server.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.application.ports.`in`.*
import yousang.rest_server.application.ports.out.*
import yousang.rest_server.domain.exception.BadRequestException
import yousang.rest_server.domain.exception.NotFoundException
import yousang.rest_server.domain.model.*

/**
 * 복권 서비스
 * 로또 6/45와 연금복권의 모든 비즈니스 로직을 처리
 */
@Service
@Transactional
class LotteryService(
    private val lottoTicketRepository: LottoTicketRepositoryPort,
    private val pensionLotteryTicketRepository: PensionLotteryTicketRepositoryPort,
    private val lottoDrawResultRepository: LottoDrawResultRepositoryPort,
    private val pensionLotteryDrawResultRepository: PensionLotteryDrawResultRepositoryPort
) : GenerateLottoNumbersUseCase,
    GeneratePensionLotteryNumbersUseCase,
    CheckLotteryWinningUseCase,
    GetLotteryTicketsUseCase,
    GetLotteryDrawResultsUseCase {

    // ========== 로또 번호 생성 ==========

    override fun generateAutoNumbers(userId: Long, drawNumber: Int, count: Int): List<LottoTicket> {
        validateCount(count)

        val tickets = (1..count).map {
            LottoTicket.generateRandom(userId, drawNumber)
        }

        return lottoTicketRepository.saveAll(tickets)
    }

    override fun generateManualNumbers(userId: Long, drawNumber: Int, numbersList: List<List<Int>>): List<LottoTicket> {
        validateCount(numbersList.size)

        val tickets = numbersList.map { numbers ->
            LottoTicket.createManual(userId, drawNumber, numbers)
        }

        return lottoTicketRepository.saveAll(tickets)
    }

    // ========== 연금복권 번호 생성 ==========

    override fun generateAutoNumbers(userId: Long, drawNumber: Int, count: Int): List<PensionLotteryTicket> {
        validateCount(count)

        val tickets = (1..count).map {
            PensionLotteryTicket.generateRandom(userId, drawNumber)
        }

        return pensionLotteryTicketRepository.saveAll(tickets)
    }

    override fun generateManualNumbers(
        userId: Long,
        drawNumber: Int,
        tickets: List<Pair<Int, Int>>
    ): List<PensionLotteryTicket> {
        validateCount(tickets.size)

        val pensionTickets = tickets.map { (group, number) ->
            PensionLotteryTicket.createManual(userId, drawNumber, group, number)
        }

        return pensionLotteryTicketRepository.saveAll(pensionTickets)
    }

    // ========== 당첨 확인 ==========

    override fun checkLottoWinning(ticketId: Long): LottoTicket {
        val ticket = lottoTicketRepository.findById(ticketId)
            ?: throw NotFoundException("로또 티켓을 찾을 수 없습니다: $ticketId")

        val drawResult = lottoDrawResultRepository.findByDrawNumber(ticket.drawNumber)
            ?: throw NotFoundException("${ticket.drawNumber}회차 추첨 결과를 찾을 수 없습니다")

        val rank = drawResult.calculateRank(ticket)
        val amount = rank?.let { drawResult.getEstimatedPrizeAmount(it) }

        val updatedTicket = ticket.withWinningInfo(rank, amount)
        return lottoTicketRepository.save(updatedTicket)
    }

    override fun checkPensionLotteryWinning(ticketId: Long): PensionLotteryTicket {
        val ticket = pensionLotteryTicketRepository.findById(ticketId)
            ?: throw NotFoundException("연금복권 티켓을 찾을 수 없습니다: $ticketId")

        val drawResult = pensionLotteryDrawResultRepository.findByDrawNumber(ticket.drawNumber)
            ?: throw NotFoundException("${ticket.drawNumber}회차 추첨 결과를 찾을 수 없습니다")

        val rank = drawResult.calculateRank(ticket)
        val amount = rank?.let { drawResult.getPrizeAmount(it) }
        val monthlyPension = rank?.let { drawResult.getMonthlyPension(it) }

        val updatedTicket = ticket.withWinningInfo(rank, amount, monthlyPension)
        return pensionLotteryTicketRepository.save(updatedTicket)
    }

    override fun checkAllLottoTickets(userId: Long, drawNumber: Int): List<LottoTicket> {
        val tickets = lottoTicketRepository.findByUserIdAndDrawNumber(userId, drawNumber)

        if (tickets.isEmpty()) {
            return emptyList()
        }

        val drawResult = lottoDrawResultRepository.findByDrawNumber(drawNumber)
            ?: throw NotFoundException("${drawNumber}회차 추첨 결과를 찾을 수 없습니다")

        val updatedTickets = tickets.map { ticket ->
            val rank = drawResult.calculateRank(ticket)
            val amount = rank?.let { drawResult.getEstimatedPrizeAmount(it) }
            ticket.withWinningInfo(rank, amount)
        }

        return lottoTicketRepository.saveAll(updatedTickets)
    }

    override fun checkAllPensionLotteryTickets(userId: Long, drawNumber: Int): List<PensionLotteryTicket> {
        val tickets = pensionLotteryTicketRepository.findByUserIdAndDrawNumber(userId, drawNumber)

        if (tickets.isEmpty()) {
            return emptyList()
        }

        val drawResult = pensionLotteryDrawResultRepository.findByDrawNumber(drawNumber)
            ?: throw NotFoundException("${drawNumber}회차 추첨 결과를 찾을 수 없습니다")

        val updatedTickets = tickets.map { ticket ->
            val rank = drawResult.calculateRank(ticket)
            val amount = rank?.let { drawResult.getPrizeAmount(it) }
            val monthlyPension = rank?.let { drawResult.getMonthlyPension(it) }
            ticket.withWinningInfo(rank, amount, monthlyPension)
        }

        return pensionLotteryTicketRepository.saveAll(updatedTickets)
    }

    // ========== 티켓 조회 ==========

    override fun getLottoTicket(ticketId: Long): LottoTicket {
        return lottoTicketRepository.findById(ticketId)
            ?: throw NotFoundException("로또 티켓을 찾을 수 없습니다: $ticketId")
    }

    override fun getPensionLotteryTicket(ticketId: Long): PensionLotteryTicket {
        return pensionLotteryTicketRepository.findById(ticketId)
            ?: throw NotFoundException("연금복권 티켓을 찾을 수 없습니다: $ticketId")
    }

    override fun getUserLottoTickets(userId: Long, drawNumber: Int?): List<LottoTicket> {
        return if (drawNumber != null) {
            lottoTicketRepository.findByUserIdAndDrawNumber(userId, drawNumber)
        } else {
            lottoTicketRepository.findByUserId(userId)
        }
    }

    override fun getUserPensionLotteryTickets(userId: Long, drawNumber: Int?): List<PensionLotteryTicket> {
        return if (drawNumber != null) {
            pensionLotteryTicketRepository.findByUserIdAndDrawNumber(userId, drawNumber)
        } else {
            pensionLotteryTicketRepository.findByUserId(userId)
        }
    }

    override fun getUserWinningLottoTickets(userId: Long): List<LottoTicket> {
        return lottoTicketRepository.findWinningTicketsByUserId(userId)
    }

    override fun getUserWinningPensionLotteryTickets(userId: Long): List<PensionLotteryTicket> {
        return pensionLotteryTicketRepository.findWinningTicketsByUserId(userId)
    }

    // ========== 추첨 결과 조회 ==========

    override fun getLottoDrawResult(drawNumber: Int): LottoDrawResult {
        return lottoDrawResultRepository.findByDrawNumber(drawNumber)
            ?: throw NotFoundException("${drawNumber}회차 로또 추첨 결과를 찾을 수 없습니다")
    }

    override fun getPensionLotteryDrawResult(drawNumber: Int): PensionLotteryDrawResult {
        return pensionLotteryDrawResultRepository.findByDrawNumber(drawNumber)
            ?: throw NotFoundException("${drawNumber}회차 연금복권 추첨 결과를 찾을 수 없습니다")
    }

    override fun getLatestLottoDrawResult(): LottoDrawResult {
        return lottoDrawResultRepository.findLatest()
            ?: throw NotFoundException("로또 추첨 결과가 없습니다")
    }

    override fun getLatestPensionLotteryDrawResult(): PensionLotteryDrawResult {
        return pensionLotteryDrawResultRepository.findLatest()
            ?: throw NotFoundException("연금복권 추첨 결과가 없습니다")
    }

    override fun getLottoDrawResults(limit: Int): List<LottoDrawResult> {
        return lottoDrawResultRepository.findRecent(limit)
    }

    override fun getPensionLotteryDrawResults(limit: Int): List<PensionLotteryDrawResult> {
        return pensionLotteryDrawResultRepository.findRecent(limit)
    }

    // ========== 유틸리티 메서드 ==========

    private fun validateCount(count: Int) {
        if (count < 1) {
            throw BadRequestException("생성할 게임 수는 1개 이상이어야 합니다")
        }
        if (count > 100) {
            throw BadRequestException("한 번에 생성할 수 있는 게임 수는 최대 100개입니다")
        }
    }
}
