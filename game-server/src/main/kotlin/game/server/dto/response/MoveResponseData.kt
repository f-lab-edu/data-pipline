package game.server.dto.response

import game.server.game.domain.v0.Position

data class MoveResponseData(
    val newPosition: Position
)
