package game.server.game.handler

import com.fasterxml.jackson.core.type.TypeReference
import game.server.game.domain.player.Player
import game.server.core.service.RequestHandler
import game.server.game.domain.vo.Position
import game.server.game.domain.vo.Direction
import game.server.game.domain.vo.Direction.*
import game.server.game.dto.request.PlayerMoveRequestData
import game.server.core.dto.Request
import game.server.core.dto.ApiResponse
import game.server.core.dto.ErrorResponse
import game.server.game.dto.response.MoveResponseData
import game.server.core.dto.Response
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