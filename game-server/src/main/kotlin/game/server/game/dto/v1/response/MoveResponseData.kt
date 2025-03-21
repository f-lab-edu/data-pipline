package game.server.game.dto.v1.response

import game.server.game.domain.vo.Position

data class MoveResponseData(
    val newPosition: Position
)
