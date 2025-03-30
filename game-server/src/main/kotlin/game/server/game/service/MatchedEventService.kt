package game.server.game.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.dto.v1.maching.Matched
import game.server.game.domain.player.Player
import game.server.game.session.PlayerManager
import game.server.game.session.WebSocketSessionManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class MatchedEventService(
    private val sessionManager: WebSocketSessionManager,
    private val objectMapper: ObjectMapper,
    private val playerManager: PlayerManager
) {
    suspend fun processMatchedEvent(matched: Matched) = coroutineScope {
        val message = objectMapper.writeValueAsString(matched)

        println("""
            ${matched.sessionIds.mapNotNull { sessionManager.getSession(it) }}
        """.trimIndent())
        matched.sessionIds.mapNotNull { sessionKey ->
            sessionManager.getSession(sessionKey)
                ?.also { webSocketSession ->
                    val player = Player(
                        sessionId = sessionKey,
                        matchId = matched.matchId,
                        socket = webSocketSession
                    )
                    playerManager.addPlayer(matched.matchId, player)
                }
        }.map { webSocketSession ->
            launch {
                webSocketSession.send(
                    Mono.just(webSocketSession.textMessage(message))
                ).awaitSingleOrNull()
            }
        }.joinAll()
    }
}