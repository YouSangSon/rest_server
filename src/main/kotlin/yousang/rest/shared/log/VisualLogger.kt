package yousang.rest.shared.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 가시성이 높은 로깅을 위한 유틸리티 클래스
 */
object VisualLogger {
    private val logger: Logger = LoggerFactory.getLogger(VisualLogger::class.java)
    
    // 구분선 문자
    private const val HORIZONTAL_LINE = "═"
    private const val VERTICAL_LINE = "║"
    private const val TOP_LEFT = "╔"
    private const val TOP_RIGHT = "╗"
    private const val BOTTOM_LEFT = "╚"
    private const val BOTTOM_RIGHT = "╝"
    
    // 구분선 길이
    private const val DEFAULT_WIDTH = 80
    
    /**
     * 시작 구분선 출력
     */
    fun startSection(title: String, width: Int = DEFAULT_WIDTH) {
        val actualTitle = if (title.isNotEmpty()) " $title " else ""
        val titleLength = actualTitle.length
        val lineLength = (width - titleLength) / 2
        val leftLine = HORIZONTAL_LINE.repeat(lineLength.coerceAtLeast(0))
        val rightLine = HORIZONTAL_LINE.repeat((width - titleLength - lineLength).coerceAtLeast(0))
        
        logger.info("")
        logger.info("$TOP_LEFT$leftLine$actualTitle$rightLine$TOP_RIGHT")
    }
    
    /**
     * 종료 구분선 출력
     */
    fun endSection(width: Int = DEFAULT_WIDTH) {
        logger.info("$BOTTOM_LEFT${HORIZONTAL_LINE.repeat(width - 2)}$BOTTOM_RIGHT")
        logger.info("")
    }
    
    /**
     * 구분선 내부 텍스트 출력
     */
    fun sectionInfo(message: String, width: Int = DEFAULT_WIDTH) {
        val padding = width - message.length - 4
        val paddedMessage = if (padding > 0) {
            "$VERTICAL_LINE ${message.padEnd(width - 4)} $VERTICAL_LINE"
        } else {
            // 메시지가 너무 길면 여러 줄로 분할
            val lines = splitMessageIntoLines(message, width - 4)
            lines.joinToString("\n") { "$VERTICAL_LINE ${it.padEnd(width - 4)} $VERTICAL_LINE" }
        }
        
        paddedMessage.lines().forEach { logger.info(it) }
    }
    
    /**
     * 메시지를 여러 줄로 분할
     */
    private fun splitMessageIntoLines(message: String, maxWidth: Int): List<String> {
        val result = mutableListOf<String>()
        var remaining = message
        
        while (remaining.isNotEmpty()) {
            val length = minOf(remaining.length, maxWidth)
            val splitIndex = if (length < remaining.length) {
                val lastSpaceIndex = remaining.substring(0, length).lastIndexOf(' ')
                if (lastSpaceIndex > 0) lastSpaceIndex + 1 else length
            } else {
                length
            }
            
            result.add(remaining.substring(0, splitIndex))
            remaining = remaining.substring(splitIndex).trimStart()
        }
        
        return result
    }
    
    /**
     * 섹션 내에 정보 출력
     */
    fun logInSection(title: String, messages: List<String>, width: Int = DEFAULT_WIDTH) {
        startSection(title, width)
        messages.forEach { sectionInfo(it, width) }
        endSection(width)
    }
    
    /**
     * 에러 섹션 출력
     */
    fun logErrorSection(title: String, errorMessage: String, throwable: Throwable? = null, width: Int = DEFAULT_WIDTH) {
        startSection("ERROR: $title", width)
        sectionInfo(errorMessage, width)
        
        throwable?.let {
            sectionInfo("Exception: ${it.javaClass.simpleName}", width)
            val stackTrace = it.stackTraceToString().lines().take(10)
            stackTrace.forEach { line -> sectionInfo(line, width) }
        }
        
        endSection(width)
    }
    
    /**
     * 진행 상황 출력
     */
    fun logProgress(title: String, current: Int, total: Int, width: Int = DEFAULT_WIDTH) {
        val percentage = (current.toDouble() / total * 100).toInt()
        val barWidth = width - title.length - 10
        val completedWidth = ((barWidth * percentage) / 100).coerceAtLeast(0)
        val remainingWidth = (barWidth - completedWidth).coerceAtLeast(0)
        
        val progressBar = "[${"=".repeat(completedWidth)}${" ".repeat(remainingWidth)}] $percentage%"
        logger.info("$title $progressBar")
    }
} 