package yousang.rest.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import yousang.rest.adapter.out.lottery.DHLotteryAdapter
import yousang.rest.adapter.out.persistence.UserPersistenceAdapter
import yousang.rest.adapter.out.persistence.mapper.UserMapper
import yousang.rest.adapter.out.persistence.repository.UserJpaRepository
import yousang.rest.application.LotteryService
import yousang.rest.application.UserService
import yousang.rest.domain.port.`in`.CreateUserUseCase
import yousang.rest.domain.port.`in`.GetUserUseCase
import yousang.rest.domain.port.`in`.LottoQueryUseCase
import yousang.rest.domain.port.`in`.PensionLotteryQueryUseCase
import yousang.rest.domain.port.out.LotteryDataProvider
import yousang.rest.domain.port.out.UserRepository

@Configuration
class BeanConfiguration {

    @Bean
    fun userRepository(
        userJpaRepository: UserJpaRepository,
        userMapper: UserMapper
    ): UserRepository {
        return UserPersistenceAdapter(userJpaRepository, userMapper)
    }

    @Bean
    fun createUserUseCase(userRepository: UserRepository): CreateUserUseCase {
        return UserService(userRepository)
    }

    @Bean
    fun getUserUseCase(userRepository: UserRepository): GetUserUseCase {
        return UserService(userRepository)
    }
    
    @Bean
    fun lotteryDataProvider(restTemplate: RestTemplate): LotteryDataProvider {
        return DHLotteryAdapter(restTemplate)
    }
    
    @Bean
    fun lottoQueryUseCase(lotteryDataProvider: LotteryDataProvider): LottoQueryUseCase {
        return LotteryService(lotteryDataProvider)
    }
    
    @Bean
    fun pensionLotteryQueryUseCase(lotteryDataProvider: LotteryDataProvider): PensionLotteryQueryUseCase {
        return LotteryService(lotteryDataProvider)
    }
} 