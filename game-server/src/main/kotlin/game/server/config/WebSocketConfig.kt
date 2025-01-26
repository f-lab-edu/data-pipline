package game.server.config

import game.server.websocket.GameWebSocketHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
open class WebSocketConfig : WebFluxConfigurer {

    @Bean
    open fun webSocketHandlerMapping(gameWebSocketHandler: GameWebSocketHandler): SimpleUrlHandlerMapping {
        return SimpleUrlHandlerMapping()
            .apply {
                urlMap = mapOf("/ws/game" to gameWebSocketHandler)
                order = 0
            }
    }

    @Bean
    open fun webSocketHandlerAdapter() = WebSocketHandlerAdapter()
}