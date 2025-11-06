package yousang.rest_server.domain.model

/**
 * 복권 유형
 */
enum class LotteryType {
    /**
     * 로또 6/45
     * 1~45 숫자 중 6개를 선택하는 복권
     */
    LOTTO_645,

    /**
     * 연금복권
     * 조(1~5)와 6자리 번호로 구성된 복권
     */
    PENSION_LOTTERY
}
