package yousang.rest.shared

import java.time.LocalDate

object Constants {
    const val BASE_PATH: String = ""
    const val UTC: String = "UTC"
    const val SEOUL: String = "Asia/Seoul"


    const val LOTTO_URL = "https://www.dhlottery.co.kr"
    const val ANNUITY_LOTTO_URL = "https://dhlottery.co.kr/gameResult.do?method=win720"
    val LOTTO_FIRST_DATE: LocalDate = LocalDate.of(2002, 12, 7)
    val ANNUITY_LOTTO_FIRST_DATE: LocalDate = LocalDate.of(2020, 5, 7)
}