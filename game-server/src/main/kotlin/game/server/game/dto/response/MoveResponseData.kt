package game.server.game.dto.response

import game.server.game.domain.vo.Position

data class MoveResponseData(
    val newPosition: Position
)
