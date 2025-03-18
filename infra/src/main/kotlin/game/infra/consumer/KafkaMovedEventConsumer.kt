package game.infra.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.config.ObjectConfig
import com.game.dto.v1.maching.Matched
import com.game.dto.v1.move.PlayerMoved
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
class KafkaMovedEventConsumer(
    private val objectMapper: ObjectMapper,
    private val redisSessionManagement: SessionManagement,
    private val webSocketClient: ReactorNettyWebSocketClient
) {

    private val gameServerConnections = ConcurrentHashMap<String, ReactorNettyWebSocketClient>()

    @KafkaListener(
        topics = ["\${kafka.topic.player-move}"],
        groupId = "\${kafka.group.player-move-group}",
        containerFactory = "movedKafkaListenerContainerFactory"
    )
    fun listen(playerMoved: PlayerMoved) {
        CoroutineScope(Dispatchers.IO).launch {
            consumePlayerMovedEvent(playerMoved)
        }
    }

    private suspend fun consumePlayerMovedEvent(playerMoved: PlayerMoved) {
        val userSessions = redisSessionManagement.findBySessionId(playerMoved.receivers)

        val sessionsGroupedByServer = userSessions.groupBy { session ->
            "${session.serverIp}:${session.serverPort}"
        }

        sessionsGroupedByServer.forEach { (server, sessions) ->
            val uri = URI.create("ws://$server/internal-websocket")
            val event = playerMoved.copy(receivers = sessions.map { it.sessionId })
            sendViaWebSocket(uri, event)
        }
    }

    private suspend fun sendViaWebSocket(uri: URI, event: PlayerMoved) {
        val client = gameServerConnections.computeIfAbsent(uri.toString()) {
            webSocketClient
        }

        client.execute(uri) { session ->
            session.send(Mono.just(session.textMessage(serialize(event))))
                .then()
        }.awaitSingleOrNull()
    }

    private fun serialize(event: PlayerMoved): String {
        return objectMapper.writeValueAsString(event)
    }
}
