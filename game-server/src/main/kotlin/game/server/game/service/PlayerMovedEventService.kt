package game.server.game.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.dto.v1.move.PlayerMoved
import game.server.game.session.PlayerManager
import game.server.game.session.WebSocketSessionManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class PlayerMovedEventService(
    private val sessionManager: WebSocketSessionManager,
    private val objectMapper: ObjectMapper,
    private val playerManager: PlayerManager
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun processPlayerMovedEvent(event: PlayerMoved) = coroutineScope {
        logger.info("=========================playerMovedEventService $event")
        val message = objectMapper.writeValueAsString(event)

        event.receivers.mapNotNull { sessionKey ->
            sessionManager.getSession(sessionKey)
        }.map { webSocketSession ->
            launch {
                webSocketSession.send(
                    Mono.just(webSocketSession.textMessage(message))
                ).awaitSingleOrNull()
            }
        }.joinAll()

        playerManager.updatePlayerPosition(
            matchId = event.matchId,
            sessionId = event.playerId,
            positionX = event.newPositionX,
            positionY = event.newPositionY
        )
    }
}