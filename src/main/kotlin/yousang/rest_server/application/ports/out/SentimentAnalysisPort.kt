package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.model.SentimentType

/**
 * 감성 분석 Port (Outbound Port)
 *
 * AI/ML 감성 분석 서비스와의 통신을 위한 포트.
 * Python FastAPI 서비스 (FinBERT)가 이 포트를 구현합니다.
 */
interface SentimentAnalysisPort {
    /**
     * 텍스트의 감성을 분석합니다.
     *
     * @param text 분석할 텍스트
     * @return 감성 분석 결과
     */
    fun analyze(text: String): SentimentResult

    /**
     * 여러 텍스트의 감성을 일괄 분석합니다.
     *
     * @param texts 분석할 텍스트 목록
     * @return 감성 분석 결과 목록
     */
    fun analyzeBatch(texts: List<String>): List<SentimentResult>

    /**
     * 뉴스 제목과 본문을 함께 분석합니다.
     *
     * @param title 제목
     * @param content 본문
     * @return 감성 분석 결과
     */
    fun analyzeArticle(title: String, content: String): SentimentResult
}

/**
 * 감성 분석 결과
 */
data class SentimentResult(
    val score: Double,              // -1.0 ~ +1.0
    val sentimentType: SentimentType,
    val confidence: Double,         // 0.0 ~ 1.0
    val positiveProb: Double = 0.0,
    val neutralProb: Double = 0.0,
    val negativeProb: Double = 0.0
) {
    init {
        require(score in -1.0..1.0) { "감성 점수는 -1.0 ~ +1.0 사이여야 합니다: $score" }
        require(confidence in 0.0..1.0) { "신뢰도는 0.0 ~ 1.0 사이여야 합니다: $confidence" }
        require(positiveProb in 0.0..1.0) { "긍정 확률은 0.0 ~ 1.0 사이여야 합니다" }
        require(neutralProb in 0.0..1.0) { "중립 확률은 0.0 ~ 1.0 사이여야 합니다" }
        require(negativeProb in 0.0..1.0) { "부정 확률은 0.0 ~ 1.0 사이여야 합니다" }
    }
}
