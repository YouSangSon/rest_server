package yousang.rest.shared.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * 애플리케이션 시작 시 환경 설정 정보를 로깅하는 설정 클래스
 */
@Configuration
class ApplicationStartupConfig @Autowired constructor(
    private val envConfigExampleService: EnvConfigExampleService
) : ApplicationListener<ApplicationReadyEvent> {

    /**
     * 애플리케이션이 시작되면 환경 설정 정보를 출력
     */
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        envConfigExampleService.printEnvironmentInfo()
        envConfigExampleService.executeEnvironmentSpecificLogic()
    }
} 