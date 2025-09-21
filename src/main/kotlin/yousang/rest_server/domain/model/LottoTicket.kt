package yousang.rest_server.domain.model

/**
 * Domain entity representing a Lotto ticket.
 * Numbers follow Korean Lotto rules: 6 unique numbers between 1 and 45 inclusive.
 */
data class LottoTicket(
    val id: Long? = null,
    val numbers: List<Int>
) {
    init {
        require(numbers.size == 6) { "Lotto ticket must contain exactly 6 numbers" }
        require(numbers.toSet().size == 6) { "Lotto numbers must be unique" }
        require(numbers.all { it in 1..45 }) { "Lotto numbers must be in range 1..45" }
    }

    fun sortedNumbers(): List<Int> = numbers.sorted()
}
