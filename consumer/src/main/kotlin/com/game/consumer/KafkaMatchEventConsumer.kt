package com.game.consumer

import com.game.dto.v1.maching.Matched
import com.game.dto.v1.move.PlayerMoved
import com.game.service.v1.SessionManagement
import com.game.util.coroutine.WebSocketSessionContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.net.URI

@Component
class KafkaMatchedEventConsumer(
    private val redisSessionManagement: SessionManagement,
    private val webSocketConnectionManager: WebSocketConnectionManager
) {

    private val coroutineContext = WebSocketSessionContext()
    private val eventChannel = Channel<Matched>(Channel.UNLIMITED)

    init {
        coroutineContext.launch {
            for (event in eventChannel) {
                consumeMatchedEvent(event)
            }
        }
    }

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
            webSocketConnectionManager.send(uri, matched)
        }
    }
}