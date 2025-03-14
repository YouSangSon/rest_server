package yousang.rest.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import yousang.rest.adapter.MockLotteryAdapter
import yousang.rest.domain.lotto.AnnuityLottoRepository
import yousang.rest.domain.lotto.LottoRepository

@Configuration
@Profile("test")
class TestConfig {
    
    @Bean
    @Primary
    fun lottoRepository(): LottoRepository {
        return MockLotteryAdapter()
    }
    
    @Bean
    @Primary
    fun annuityLottoRepository(): AnnuityLottoRepository {
        return MockLotteryAdapter()
    }
} 