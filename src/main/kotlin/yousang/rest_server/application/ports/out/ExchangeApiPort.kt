package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 거래소 API Port (Outbound Port)
 *
 * 거래소 API와의 통신을 위한 포트.
 * Binance, Upbit 어댑터가 이 포트를 구현합니다.
 */
interface ExchangeApiPort {
    /**
     * 실시간 시세를 조회합니다.
     *
     * @param symbol 거래 쌍
     * @return 시장 데이터
     */
    fun getMarketData(symbol: String): MarketData

    /**
     * 여러 심볼의 시세를 일괄 조회합니다.
     *
     * @param symbols 거래 쌍 목록
     * @return 시장 데이터 목록
     */
    fun getMarketDataBatch(symbols: List<String>): List<MarketData>

    /**
     * 캔들스틱 데이터를 조회합니다.
     *
     * @param symbol 거래 쌍
     * @param interval 캔들 인터벌
     * @param limit 개수 (기본값: 100)
     * @return 캔들 데이터 목록
     */
    fun getCandles(
        symbol: String,
        interval: CandleInterval,
        limit: Int = 100
    ): List<Candle>

    /**
     * 거래 가능한 심볼 목록을 조회합니다.
     *
     * @return 거래 쌍 목록
     */
    fun getTradingPairs(): List<TradingPair>

    /**
     * 시장가 주문을 실행합니다.
     *
     * @param symbol 거래 쌍
     * @param side 매수/매도
     * @param quantity 수량
     * @param clientOrderId 클라이언트 주문 ID
     * @return 주문 결과
     */
    fun placeMarketOrder(
        symbol: String,
        side: OrderSide,
        quantity: BigDecimal,
        clientOrderId: String? = null
    ): Order

    /**
     * 지정가 주문을 실행합니다.
     *
     * @param symbol 거래 쌍
     * @param side 매수/매도
     * @param price 가격
     * @param quantity 수량
     * @param timeInForce 주문 유효 기간
     * @param clientOrderId 클라이언트 주문 ID
     * @return 주문 결과
     */
    fun placeLimitOrder(
        symbol: String,
        side: OrderSide,
        price: BigDecimal,
        quantity: BigDecimal,
        timeInForce: TimeInForce = TimeInForce.GTC,
        clientOrderId: String? = null
    ): Order

    /**
     * 손절 주문을 실행합니다.
     *
     * @param symbol 거래 쌍
     * @param side 매수/매도
     * @param stopPrice 손절가
     * @param quantity 수량
     * @param clientOrderId 클라이언트 주문 ID
     * @return 주문 결과
     */
    fun placeStopLossOrder(
        symbol: String,
        side: OrderSide,
        stopPrice: BigDecimal,
        quantity: BigDecimal,
        clientOrderId: String? = null
    ): Order

    /**
     * 주문을 취소합니다.
     *
     * @param symbol 거래 쌍
     * @param orderId 주문 ID
     * @return 취소된 주문
     */
    fun cancelOrder(symbol: String, orderId: String): Order

    /**
     * 주문 상태를 조회합니다.
     *
     * @param symbol 거래 쌍
     * @param orderId 주문 ID
     * @return 주문 정보
     */
    fun getOrder(symbol: String, orderId: String): Order

    /**
     * 활성 주문 목록을 조회합니다.
     *
     * @param symbol 거래 쌍 (null이면 전체)
     * @return 활성 주문 목록
     */
    fun getOpenOrders(symbol: String? = null): List<Order>

    /**
     * 잔고를 조회합니다.
     *
     * @param asset 자산 (예: "BTC", "USDT")
     * @return 보유 수량
     */
    fun getBalance(asset: String): BigDecimal

    /**
     * 전체 잔고를 조회합니다.
     *
     * @return 자산별 보유 수량 (Map<자산, 수량>)
     */
    fun getAllBalances(): Map<String, BigDecimal>

    /**
     * 거래소 이름을 반환합니다.
     *
     * @return 거래소 이름 (예: "binance", "upbit")
     */
    fun getExchangeName(): String

    /**
     * WebSocket으로 실시간 시세를 구독합니다.
     *
     * @param symbols 거래 쌍 목록
     * @param callback 시세 업데이트 콜백
     */
    fun subscribeMarketData(symbols: List<String>, callback: (MarketData) -> Unit)

    /**
     * WebSocket 구독을 해제합니다.
     *
     * @param symbols 거래 쌍 목록
     */
    fun unsubscribeMarketData(symbols: List<String>)
}
