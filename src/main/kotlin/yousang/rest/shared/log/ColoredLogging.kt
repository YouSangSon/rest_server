package yousang.rest.shared.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 색상 코드를 적용한 로깅 기능
 */
object ColoredLogging {
    // ANSI 색상 코드
    private const val ANSI_RESET = "\u001B[0m"
    private const val ANSI_BLACK = "\u001B[30m"
    private const val ANSI_RED = "\u001B[31m"
    private const val ANSI_GREEN = "\u001B[32m"
    private const val ANSI_YELLOW = "\u001B[33m"
    private const val ANSI_BLUE = "\u001B[34m"
    private const val ANSI_PURPLE = "\u001B[35m"
    private const val ANSI_CYAN = "\u001B[36m"
    private const val ANSI_WHITE = "\u001B[37m"
    
    // 배경색 코드
    private const val ANSI_BLACK_BACKGROUND = "\u001B[40m"
    private const val ANSI_RED_BACKGROUND = "\u001B[41m"
    private const val ANSI_GREEN_BACKGROUND = "\u001B[42m"
    private const val ANSI_YELLOW_BACKGROUND = "\u001B[43m"
    private const val ANSI_BLUE_BACKGROUND = "\u001B[44m"
    private const val ANSI_PURPLE_BACKGROUND = "\u001B[45m"
    private const val ANSI_CYAN_BACKGROUND = "\u001B[46m"
    private const val ANSI_WHITE_BACKGROUND = "\u001B[47m"

    // 스타일 코드
    private const val ANSI_BOLD = "\u001B[1m"
    private const val ANSI_UNDERLINE = "\u001B[4m"
    
    /**
     * 색상이 적용된 문자열 생성
     */
    fun red(text: String): String = "$ANSI_RED$text$ANSI_RESET"
    fun green(text: String): String = "$ANSI_GREEN$text$ANSI_RESET"
    fun yellow(text: String): String = "$ANSI_YELLOW$text$ANSI_RESET"
    fun blue(text: String): String = "$ANSI_BLUE$text$ANSI_RESET"
    fun purple(text: String): String = "$ANSI_PURPLE$text$ANSI_RESET"
    fun cyan(text: String): String = "$ANSI_CYAN$text$ANSI_RESET"
    fun white(text: String): String = "$ANSI_WHITE$text$ANSI_RESET"
    fun black(text: String): String = "$ANSI_BLACK$text$ANSI_RESET"
    
    /**
     * 배경색이 적용된 문자열 생성
     */
    fun redBackground(text: String): String = "$ANSI_RED_BACKGROUND$text$ANSI_RESET"
    fun greenBackground(text: String): String = "$ANSI_GREEN_BACKGROUND$text$ANSI_RESET"
    fun yellowBackground(text: String): String = "$ANSI_YELLOW_BACKGROUND$text$ANSI_RESET"
    fun blueBackground(text: String): String = "$ANSI_BLUE_BACKGROUND$text$ANSI_RESET"
    fun purpleBackground(text: String): String = "$ANSI_PURPLE_BACKGROUND$text$ANSI_RESET"
    fun cyanBackground(text: String): String = "$ANSI_CYAN_BACKGROUND$text$ANSI_RESET"
    
    /**
     * 스타일이 적용된 문자열 생성
     */
    fun bold(text: String): String = "$ANSI_BOLD$text$ANSI_RESET"
    fun underline(text: String): String = "$ANSI_UNDERLINE$text$ANSI_RESET"
    
    /**
     * 색상과 스타일을 조합한 문자열 생성
     */
    fun highlight(text: String): String = "$ANSI_BOLD$ANSI_YELLOW$text$ANSI_RESET"
    fun warning(text: String): String = "$ANSI_YELLOW$ANSI_BOLD$text$ANSI_RESET"
    fun error(text: String): String = "$ANSI_RED$ANSI_BOLD$text$ANSI_RESET"
    fun success(text: String): String = "$ANSI_GREEN$ANSI_BOLD$text$ANSI_RESET"
    fun info(text: String): String = "$ANSI_BLUE$text$ANSI_RESET"
    fun debug(text: String): String = "$ANSI_PURPLE$text$ANSI_RESET"
    fun trace(text: String): String = "$ANSI_CYAN$text$ANSI_RESET"
} 