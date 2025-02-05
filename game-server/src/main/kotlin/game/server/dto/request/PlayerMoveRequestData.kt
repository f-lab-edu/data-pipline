package game.server.dto.request

import game.server.domain.Position
import game.server.dto.Direction

data class PlayerMoveRequestData(
    val currentPosition: Position,
    val direction: Direction,
    val speed: Int
)
