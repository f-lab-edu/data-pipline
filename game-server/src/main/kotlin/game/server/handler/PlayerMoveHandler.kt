package game.server.handler

import com.fasterxml.jackson.databind.ObjectMapper
import game.server.Player
import game.server.dto.PlayerMoveRequest
import game.server.dto.Position
import org.springframework.stereotype.Component

const val CANVAS_WIDTH = 800
const val CANVAS_HEIGHT = 600

@Component("move")
class PlayerMoveHandler(
    private val objectMapper: ObjectMapper,
    private val player: Player
) : RequestHandler<PlayerMoveRequest> {

    override fun handle(request: PlayerMoveRequest): String {
        val (currentX, currentY) = request.currentPosition
        val (newX, newY) = calculateNewPosition(currentX, currentY, request.direction, request.speed)

        val isAllowed = isMoveAllowed(newX, newY)
        return if (isAllowed) {
            player.position = Position(newX, newY)
            objectMapper.writeValueAsString(
                mapOf(
                    "type" to "move",
                    "success" to true,
                    "newPosition" to mapOf("x" to newX, "y" to newY)
                )
            )
        } else {
            objectMapper.writeValueAsString(
                mapOf(
                    "type" to "move",
                    "success" to false,
                    "message" to "Move not allowed"
                )
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
        return x in 0 until CANVAS_WIDTH && y in 0 until CANVAS_HEIGHT
    }
}