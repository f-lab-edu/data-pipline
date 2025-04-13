package com.game.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.dto.v1.maching.KafkaEvent
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.core.publisher.Mono
import java.net.URI
import java.util.concurrent.ConcurrentHashMap


@Profile("consumer-local | consumer-prod")
@Component
class WebSocketConnectionManager(
    private val webSocketClient: ReactorNettyWebSocketClient,
    private val objectMapper: ObjectMapper
) {
    private val connectionMonos = ConcurrentHashMap<URI, Mono<WebSocketSession>>()

    suspend fun send(uri: URI, event: KafkaEvent) {
        val session = getSession(uri)
        session.send(Mono.just(session.textMessage(serialize(event)))).awaitSingleOrNull()
    }

    private suspend fun getSession(uri: URI): WebSocketSession {
        val cachedMono = connectionMonos.computeIfAbsent(uri) {
            createConnectionMono(uri).cache()
        }
        return cachedMono.awaitSingle()
    }

    private fun createConnectionMono(uri: URI): Mono<WebSocketSession> {
        return Mono.create { sink ->
            webSocketClient.execute(uri) { session ->
                sink.success(session)

                session.receive()
                    .doFinally {
                        connectionMonos.remove(uri)
                    }
                    .then()
            }.doOnError { error ->
                connectionMonos.remove(uri)
                sink.error(error)
            }.subscribe()
        }
    }

    private fun serialize(event: KafkaEvent): String {
        return objectMapper.writeValueAsString(event)
    }
}