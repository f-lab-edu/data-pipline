package game.infra.consumer

import com.game.dto.v1.move.PlayerMoved
import com.game.service.v1.SessionManagement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.net.URI

@Profile("consumer-local | consumer-prod")
@Component
class KafkaMovedEventConsumer(
    private val redisSessionManagement: SessionManagement,
    private val webSocketConnectionManager: WebSocketConnectionManager,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = ["\${kafka.topic.player-move}"],
        groupId = "\${kafka.group.player-move-group}",
        containerFactory = "kafkaEventListenerContainerFactory"
    )
    fun listen(playerMoved: PlayerMoved) {
        CoroutineScope(Dispatchers.IO).launch {
            consumePlayerMovedEvent(playerMoved)
        }
    }

    private suspend fun consumePlayerMovedEvent(playerMoved: PlayerMoved) {
        val userSessions = redisSessionManagement.findBySessionId(playerMoved.receivers)

        println("================playerMoved==================: $playerMoved")
        logger.info("Received playerMoved event: $playerMoved")
        val sessionsGroupedByServer = userSessions.groupBy { session ->
            "${session.serverIp}:${session.serverPort}"
        }

        sessionsGroupedByServer.keys.forEach { serverUrl ->
            val uri = URI.create("ws://$serverUrl/internal-websocket")
            webSocketConnectionManager.send(uri, playerMoved)
        }
    }
}
