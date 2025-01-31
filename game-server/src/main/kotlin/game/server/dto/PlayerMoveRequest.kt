package game.server.dto

import game.server.domain.Position

data class PlayerMoveRequest(
    val currentPosition: Position,
    val direction: String,
    val speed: Int
) : Request


