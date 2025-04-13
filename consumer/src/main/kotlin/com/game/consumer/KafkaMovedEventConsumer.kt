package com.game.consumer

import com.game.dto.v1.move.PlayerMoved
import com.game.service.v1.SessionManagement
import com.game.util.coroutine.WebSocketSessionContext
import kotlinx.coroutines.channels.Channel
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.net.URI

@Component
class KafkaMovedEventConsumer(
    private val redisSessionManagement: SessionManagement,
    private val webSocketConnectionManager: WebSocketConnectionManager,
) {

    private val coroutineContext = WebSocketSessionContext()
    private val eventChannel = Channel<PlayerMoved>(Channel.UNLIMITED)

    init {
        coroutineContext.launch {
            for (event in eventChannel) {
                consumePlayerMovedEvent(event)
            }
        }
    }

    @KafkaListener(
        topics = ["\${kafka.topic.player-move}"],
        groupId = "\${kafka.group.player-move-group}",
        containerFactory = "playerMovedKafkaListenerContainerFactory"
    )
    fun listen(playerMoved: PlayerMoved) {
        coroutineContext.launch {
            eventChannel.send(playerMoved)
        }
    }

    private suspend fun consumePlayerMovedEvent(playerMoved: PlayerMoved) {
        val userSessions = redisSessionManagement.findBySessionId(playerMoved.receivers)

        val sessionsGroupedByServer = userSessions.groupBy { session ->
            "${session.serverIp}:${session.serverPort}"
        }

        sessionsGroupedByServer.keys.forEach { serverUrl ->
            val uri = URI.create("ws://$serverUrl/internal-websocket")
            webSocketConnectionManager.send(uri, playerMoved)
        }
    }
}
