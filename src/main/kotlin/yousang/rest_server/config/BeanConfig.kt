package yousang.rest_server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import yousang.rest_server.application.ports.`in`.greeting.GetGreetingUseCase
import yousang.rest_server.application.service.greeting.GetGreetingService
import yousang.rest_server.application.ports.`in`.db.GetDbTimeUseCase
import yousang.rest_server.application.service.db.GetDbTimeService
import yousang.rest_server.application.ports.out.db.DbInfoPort
import yousang.rest_server.application.ports.`in`.lotto.GenerateLottoUseCase
import yousang.rest_server.application.ports.`in`.lotto.GetLottoUseCase
import yousang.rest_server.application.service.lotto.LottoService
import yousang.rest_server.application.ports.out.lotto.LottoRepositoryPort
import yousang.rest_server.application.ports.`in`.lottoOfficial.OfficialLottoUseCase
import yousang.rest_server.application.service.lottoOfficial.OfficialLottoService

@Configuration
class BeanConfig {
    @Bean
    fun getGreetingUseCase(): GetGreetingUseCase = GetGreetingService()

    @Bean
    @Profile("postgres")
    fun getDbTimeUseCase(dbInfoPort: DbInfoPort): GetDbTimeUseCase = GetDbTimeService(dbInfoPort)

    // Lotto beans are only active when using the postgres profile (requires DB)
    @Bean
    @Profile("postgres")
    fun generateLottoUseCase(lottoRepositoryPort: LottoRepositoryPort): GenerateLottoUseCase =
        LottoService(lottoRepositoryPort)

    @Bean
    @Profile("postgres")
    fun getLottoUseCase(lottoRepositoryPort: LottoRepositoryPort): GetLottoUseCase =
        LottoService(lottoRepositoryPort)

    // Official lotto (reference-guide style) use case is profile-agnostic (no DB needed)
    @Bean
    fun officialLottoUseCase(): OfficialLottoUseCase = OfficialLottoService()
}
