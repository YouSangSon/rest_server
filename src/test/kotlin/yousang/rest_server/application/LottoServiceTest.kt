package yousang.rest_server.application

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import yousang.rest_server.application.ports.`in`.lotto.LottoDto
import yousang.rest_server.application.ports.out.lotto.LottoRecord
import yousang.rest_server.application.ports.out.lotto.LottoRepositoryPort
import yousang.rest_server.application.service.lotto.LottoService

class LottoServiceTest {

    private class FakeRepo : LottoRepositoryPort {
        var saved: LottoRecord? = null
        private var seq = 0L
        private val store = mutableMapOf<Long, LottoRecord>()

        override fun save(numbers: List<Int>): LottoRecord {
            seq += 1
            val rec = LottoRecord(seq, numbers)
            saved = rec
            store[seq] = rec
            return rec
        }

        override fun findById(id: Long): LottoRecord? = store[id]

        override fun findAll(): List<LottoRecord> = store.values.sortedBy { it.id }
    }

    @Test
    fun `generateAndSave returns 6 unique sorted numbers between 1 and 45`() {
        val repo = FakeRepo()
        val service = LottoService(repo)

        val dto: LottoDto = service.generateAndSave()

        assertEquals(6, dto.numbers.size, "Should generate 6 numbers")
        assertEquals(dto.numbers.toSet().size, dto.numbers.size, "Numbers should be unique")
        assertTrue(dto.numbers.all { it in 1..45 }, "Numbers should be in 1..45")
        assertEquals(dto.numbers.sorted(), dto.numbers, "Numbers should be sorted ascending")
        // saved in repository
        val saved = repo.saved
        requireNotNull(saved)
        assertEquals(saved.numbers, dto.numbers)
        assertTrue(dto.id > 0)
    }

    @Test
    fun `getById returns saved ticket`() {
        val repo = FakeRepo()
        val service = LottoService(repo)

        val created = service.generateAndSave()
        val fetched = service.getById(created.id)

        requireNotNull(fetched)
        assertEquals(created.id, fetched.id)
        assertEquals(created.numbers, fetched.numbers)
    }
}
