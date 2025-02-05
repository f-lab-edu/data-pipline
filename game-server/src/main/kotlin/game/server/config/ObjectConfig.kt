package game.server.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
open class ObjectConfig {

    @Bean
    open fun objectMapper(): ObjectMapper {
        return ObjectMapper().registerKotlinModule()
    }
}