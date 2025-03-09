package game.server.core.config

import game.server.core.controller.WebSocketController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
@Profile("local", "test-game")
open class WebSocketConfig : WebFluxConfigurer {

    @Bean
    open fun webSocketHandlerMapping(gameRequestRouter: WebSocketController): SimpleUrlHandlerMapping {
        return SimpleUrlHandlerMapping()
            .apply {
                urlMap = mapOf("/ws/game" to gameRequestRouter)
                order = 0
            }
    }

    @Bean
    open fun webSocketHandlerAdapter() = WebSocketHandlerAdapter()
}