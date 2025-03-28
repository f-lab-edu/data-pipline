package game.infra.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.config.ObjectConfig
import com.game.dto.v1.maching.Matched
import com.game.service.v1.SessionManagement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.core.publisher.Mono
import java.net.URI
import java.util.concurrent.ConcurrentHashMap


@Profile("consumer-local | consumer-prod")
@Import(ObjectConfig::class)
@Component
class KafkaMatchedEventConsumer(
    private val webSocketClient: ReactorNettyWebSocketClient,
    private val redisSessionManagement: SessionManagement,
    private val objectMapper: ObjectMapper,
) {

    private val connectionMonos = ConcurrentHashMap<URI, Mono<WebSocketSession>>()

    @KafkaListener(
        topics = ["\${kafka.topic.match-start}"],
        groupId = "\${kafka.group.match-start-group}",
        containerFactory = "matchedKafkaListenerContainerFactory"
        )
    fun listen(matched: Matched) {
        CoroutineScope(Dispatchers.IO).launch {
            consumeMatchedEvent(matched)
        }
    }

    private suspend fun consumeMatchedEvent(matched: Matched) {
        val userSessions = redisSessionManagement.findBySessionId(matched.sessionIds)

        val sessionsGroupedByServer = userSessions.groupBy { session ->
            "${session.serverIp}:${session.serverPort}"
        }
        sessionsGroupedByServer.keys.forEach { serverUrl ->
            val uri = URI.create("ws://$serverUrl/internal-websocket")
            send(uri, matched)
        }
    }

    private suspend fun send(uri: URI, matched: Matched) {
        val session = getSession(uri)
        session.send(Mono.just(session.textMessage(serialize(matched)))).awaitSingleOrNull()
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

    private fun serialize(matched: Matched): String {
        return objectMapper.writeValueAsString(matched)
    }
}