package game.server.handler

import com.fasterxml.jackson.databind.ObjectMapper
import game.server.dto.PlayerMoveRequest
import game.server.dto.Position
import game.server.dto.response.ApiResponse
import game.server.dto.response.Error
import game.server.dto.response.MoveResponseData
import game.server.dto.response.Success
import org.springframework.stereotype.Component

@Component("move")
class PlayerMoveHandler(
    private val objectMapper: ObjectMapper
) : RequestHandler<PlayerMoveRequest> {

    override fun handle(request: PlayerMoveRequest): ApiResponse {
        val (currentX, currentY) = request.currentPosition
        val (newX, newY) = calculateNewPosition(currentX, currentY, request.direction, request.speed)

        val isAllowed = isMoveAllowed(newX, newY)
        return if (isAllowed) {
            Success(
                type = "move",
                data = MoveResponseData(Position(newX, newY))
            )
        } else {
            Error(
                type = "move",
                message = "Move is not allowed"
            )
        }
    }

    private fun calculateNewPosition(x: Int, y: Int, direction: String, speed: Int): Pair<Int, Int> {
        return when (direction) {
            "UP" -> Pair(x, y - speed)
            "DOWN" -> Pair(x, y + speed)
            "LEFT" -> Pair(x - speed, y)
            "RIGHT" -> Pair(x + speed, y)
            else -> Pair(x, y)
        }
    }

    private fun isMoveAllowed(x: Int, y: Int): Boolean {
        val mapWidth = 800
        val mapHeight = 600
        return x in 0 until mapWidth && y in 0 until mapHeight
    }
}