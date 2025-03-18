package com.game.config

import io.netty.channel.ChannelOption
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.netty.http.client.HttpClient


@Configuration
open class WebSocketClientConfig {

    @Bean
    open fun reactorNettyWebSocketClient(): ReactorNettyWebSocketClient {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        return ReactorNettyWebSocketClient(httpClient)
    }
}