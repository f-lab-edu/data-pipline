package game.server.game.dto.request

import game.server.game.domain.vo.Position
import game.server.game.domain.vo.Direction

data class PlayerMoveRequestData(
    val currentPosition: Position,
    val direction: Direction,
    val speed: Int
)
