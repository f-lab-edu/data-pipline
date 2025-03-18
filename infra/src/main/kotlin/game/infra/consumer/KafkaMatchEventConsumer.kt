package game.infra.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.config.ObjectConfig
import com.game.dto.v1.UserSession
import com.game.dto.v1.maching.Matched
import com.game.service.v1.SessionManagement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.core.publisher.Mono
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

@Profile("consumer-local")
@Component
@Import(ObjectConfig::class)
class KafkaMatchedEventConsumer(
    private val objectMapper: ObjectMapper,
    private val redisSessionManagement: SessionManagement,
    private val webSocketClient: ReactorNettyWebSocketClient
) {

    private val gameServerConnections = ConcurrentHashMap<String, ReactorNettyWebSocketClient>()

    @KafkaListener(
        topics = ["\${kafka.topic.match-start}"],
        groupId = "\${kafka.group.match-start-group}",
        containerFactory = "kafkaListenerContainerFactory"
        )
    fun listen(matched: Matched) {
        CoroutineScope(Dispatchers.IO).launch {
            consumeMatchedEvent(matched)
        }
    }

    private suspend fun consumeMatchedEvent(matched: Matched) {
        val userSessions = redisSessionManagement.findBySessionId(matched.sessionIds)

        userSessions.forEach { session ->
            val uri = serverEndpoint(session)
            sendViaWebSocket(uri, matched.also { it.sessionIds = listOf(session.sessionId) })
        }
    }

    private fun serverEndpoint(session: UserSession): URI {
        return URI.create("ws://${session.serverIp}:${session.serverPort}/internal-websocket")
    }

    private suspend fun sendViaWebSocket(uri: URI, event: Matched) {
        val client = gameServerConnections.computeIfAbsent(uri.toString()) {
            webSocketClient
        }

        client.execute(uri) { session ->
            session.send(Mono.just(session.textMessage(serialize(event))))
                .then()
        }.awaitSingleOrNull()
    }

    private fun serialize(matched: Matched): String {
        return objectMapper.writeValueAsString(matched)
    }
}