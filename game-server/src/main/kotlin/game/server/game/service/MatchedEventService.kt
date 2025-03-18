package game.server.game.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.dto.v1.maching.Matched
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
) {
    suspend fun processMatchedEvent(matched: Matched) = coroutineScope {
        val message = objectMapper.writeValueAsString(matched)

        matched.sessionIds.mapNotNull { sessionKey ->
            sessionManager.getSession(sessionKey)
        }.map { webSocketSession ->
            launch {
                webSocketSession.send(
                    Mono.just(webSocketSession.textMessage(message))
                ).awaitSingleOrNull()
            }
        }.joinAll()
    }
}