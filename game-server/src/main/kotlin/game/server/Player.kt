package game.server

import game.server.dto.Position
import org.springframework.stereotype.Component


@Component
class Player(
    val health: Int = 50,
    val damage: Int = 10,
    val speed: Int = 5
) {
    var position = Position(400, 300)
}