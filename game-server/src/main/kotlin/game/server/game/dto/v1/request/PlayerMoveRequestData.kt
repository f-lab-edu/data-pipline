package game.server.game.dto.v1.request

import game.server.game.domain.vo.Position
import game.server.game.domain.vo.Direction

data class PlayerMoveRequestData(
    val seq: Int,
    val currentPosition: Position,
    val direction: Direction,
    val speed: Int
)
