package game.server.game.domain.player

import game.server.game.domain.vo.Position
import game.server.game.handler.CANVAS_HEIGHT
import game.server.game.handler.CANVAS_WIDTH
import org.springframework.web.reactive.socket.WebSocketSession

class Player(
    val sessionId: String,
    val matchId: String,
    val socket: WebSocketSession,
    val health: Int = 50,
    val damage: Int = 10,
    val speed: Int = 5
) {
    var position = Position(400, 300)

    fun isMoveAllowed(x: Int, y: Int) =
        x in 0 until CANVAS_WIDTH && y in 0 until CANVAS_HEIGHT
}
