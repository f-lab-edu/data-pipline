package game.server.dto.response

import game.server.domain.Position

data class MoveResponseData(
    val newPosition: Position
)
