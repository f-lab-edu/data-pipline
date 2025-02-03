package game.server

import game.server.domain.Position
import game.server.handler.CANVAS_HEIGHT
import game.server.handler.CANVAS_WIDTH
import org.springframework.stereotype.Component


@Component
class Player(
    val health: Int = 50,
    val damage: Int = 10,
    val speed: Int = 5
) {
    var position = Position(400, 300)

    fun isMoveAllowed(x: Int, y: Int) =
        x in 0 until CANVAS_WIDTH && y in 0 until CANVAS_HEIGHT
}