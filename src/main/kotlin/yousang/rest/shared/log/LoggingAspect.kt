package yousang.rest.shared.utils

/**
 * @Loggable 어노테이션이 붙은 메소드의 로깅을 담당하는 Aspect
 */
//@Aspect
//@Component
//class LoggingAspect {
//    private val log by LoggerDelegate()
//
//    @Around("@annotation(yousang.rest.shared.utils.Loggable) || @within(yousang.rest.shared.utils.Loggable)")
//    fun logAround(joinPoint: ProceedingJoinPoint): Any? {
//        val signature = joinPoint.signature as MethodSignature
//        val method = signature.method
//        val loggable = method.getAnnotation(Loggable::class.java)
//            ?: joinPoint.target.javaClass.getAnnotation(Loggable::class.java)
//
//        val methodName = signature.declaringType.simpleName + "." + method.name
//        val className = signature.declaringTypeName
//
//        // 로깅 그룹 시작
//        DebugHelper.startLogGroup(log, methodName)
//
//        try {
//            // 메소드 시작 로깅
//            log(loggable.level, "Starting method: $methodName | Time: ${LocalDateTime.now()}")
//
//            // 파라미터 로깅
//            if (loggable.parameters && joinPoint.args.isNotEmpty()) {
//                log(loggable.level, "Parameters:")
//                val parameterNames = signature.parameterNames
//                val args = joinPoint.args
//
//                for (i in args.indices) {
//                    val argName = if (i < parameterNames.size) parameterNames[i] else "arg$i"
//                    logParameter(loggable.level, argName, args[i])
//                }
//            }
//
//            // 실행 시간 측정
//            val startTime = System.currentTimeMillis()
//
//            // 메소드 실행
//            val result = joinPoint.proceed()
//
//            // 실행 시간 계산
//            val executionTime = System.currentTimeMillis() - startTime
//
//            // 실행 시간 로깅
//            if (loggable.executionTime) {
//                // 실행 시간이 임계값을 초과하면 경고 로그
//                if (executionTime > loggable.warnThresholdMillis) {
//                    log(LogLevel.WARN, "Method $methodName execution took $executionTime ms (exceeded threshold of ${loggable.warnThresholdMillis} ms)")
//                } else {
//                    log(loggable.level, "Method $methodName execution time: $executionTime ms")
//                }
//            }
//
//            // 결과 로깅
//            if (loggable.result && method.returnType != Void.TYPE) {
//                log(loggable.level, "Result:")
//                logResult(loggable.level, result)
//            }
//
//            // 메소드 완료 로깅
//            log(loggable.level, "Completed method: $methodName | Time: ${LocalDateTime.now()}")
//
//            return result
//        } catch (e: Exception) {
//            DebugHelper.logException(log, "Exception in method: $methodName", e)
//            throw e
//        } finally {
//            DebugHelper.endLogGroup(log, methodName)
//        }
//    }
//
//    /**
//     * 로그 레벨에 따라 로깅
//     */
//    private fun log(level: LogLevel, message: String) {
//        when (level) {
//            LogLevel.TRACE -> log.trace(message)
//            LogLevel.DEBUG -> log.debug(message)
//            LogLevel.INFO -> log.info(message)
//            LogLevel.WARN -> log.warn(message)
//            LogLevel.ERROR -> log.error(message)
//        }
//    }
//
//    /**
//     * 파라미터 로깅
//     */
//    private fun logParameter(level: LogLevel, name: String, value: Any?) {
//        val safeValue = when {
//            value == null -> "null"
//            value is Array<*> -> Arrays.toString(value)
//            value is Collection<*> -> "Collection(size=${value.size}): $value"
//            value is Map<*, *> -> "Map(size=${value.size}): $value"
//            value.javaClass.name.startsWith("java.") || value.javaClass.name.startsWith("kotlin.") -> value.toString()
//            else -> "Object of type: ${value.javaClass.name}"
//        }
//
//        log(level, "  $name: $safeValue")
//    }
//
//    /**
//     * 결과 로깅
//     */
//    private fun logResult(level: LogLevel, result: Any?) {
//        val safeResult = when {
//            result == null -> "null"
//            result is Array<*> -> Arrays.toString(result)
//            result is Collection<*> -> "Collection(size=${result.size}): $result"
//            result is Map<*, *> -> "Map(size=${result.size}): $result"
//            result.javaClass.name.startsWith("java.") || result.javaClass.name.startsWith("kotlin.") -> result.toString()
//            else -> "Object of type: ${result.javaClass.name}"
//        }
//
//        log(level, "  $safeResult")
//    }
//}