package game.server.websocket

import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Component
class GameWebSocketHandler : WebSocketHandler {

    override fun handle(session: WebSocketSession): Mono<Void> {
        return session.send(
            session.receive()
                .map { message ->
                    val payload = message.payloadAsText
                    session.textMessage("Echo: $payload")
                })
    }
}