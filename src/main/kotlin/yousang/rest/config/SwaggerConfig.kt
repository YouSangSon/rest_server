package yousang.rest.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI().info(
            Info().title("REST API Server").description("Spring Boot REST API for user management and lottery services")
                .version("v1.0.0").contact(
                    Contact().name("YouSang").url("https://github.com/yourusername").email("your.email@example.com")
                ).license(
                    License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")
                )
        ).addServersItem(
            Server().url("/").description("Default Server URL")
        )
    }
}