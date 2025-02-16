package game.server.game.handler

import com.fasterxml.jackson.core.type.TypeReference
import game.server.game.domain.player.Player
import game.server.core.service.RequestHandler
import game.server.game.domain.v0.Position
import game.server.dto.Direction
import game.server.dto.Direction.*
import game.server.dto.request.PlayerMoveRequestData
import game.server.dto.request.Request
import game.server.dto.response.ApiResponse
import game.server.dto.response.ErrorResponse
import game.server.dto.response.MoveResponseData
import game.server.dto.response.Response
import org.springframework.stereotype.Component

const val CANVAS_WIDTH = 800
const val CANVAS_HEIGHT = 600

@Component("move")
class PlayerMoveHandler(
    private val player: Player
) : RequestHandler<PlayerMoveRequestData, MoveResponseData> {

    override val requestTypeReference: TypeReference<Request<PlayerMoveRequestData>> =
        object : TypeReference<Request<PlayerMoveRequestData>>() {}

    override fun handle(request: Request<PlayerMoveRequestData>): ApiResponse<MoveResponseData> {
        val (currentX, currentY) = request.data.currentPosition
        val (newX, newY) = calculateNewPosition(currentX, currentY, request.data.direction, request.data.speed)

        val isAllowed = player.isMoveAllowed(newX, newY)
        return if (isAllowed) {
            player.position = Position(newX, newY)
            Response(
                type = "move",
                data = MoveResponseData(Position(newX, newY))
            )
        } else {
            ErrorResponse(
                type = "move",
                message = "Move is not allowed"
            )
        }
    }

    private fun calculateNewPosition(x: Int, y: Int, direction: Direction, speed: Int): Position {
        return when (direction) {
            UP -> Position(x, y - speed)
            DOWN -> Position(x, y + speed)
            LEFT -> Position(x - speed, y)
            RIGHT -> Position(x + speed, y)
        }
    }
}