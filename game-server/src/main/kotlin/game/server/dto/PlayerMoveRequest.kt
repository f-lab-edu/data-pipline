package game.server.dto

data class PlayerMoveRequest(
    val currentPosition: Position,
    val direction: String,
    val speed: Int
) : Request


data class Position(
    val x: Int,
    val y: Int
)