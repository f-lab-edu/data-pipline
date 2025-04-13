package game.server.game.handler

import com.fasterxml.jackson.core.type.TypeReference
import com.game.dto.v1.move.PlayerMoved
import com.game.service.v1.matching.MatchEventPublisher
import game.server.game.domain.player.Player
import game.server.game.service.RequestHandler
import game.server.game.domain.vo.Position
import game.server.game.domain.vo.Direction
import game.server.game.domain.vo.Direction.*
import game.server.game.dto.v1.request.ApiRequest
import game.server.game.dto.v1.request.PlayerMoveRequestData
import game.server.game.dto.v1.response.ApiResponse
import game.server.game.dto.v1.response.ErrorResponse
import game.server.game.dto.v1.response.MoveResponseData
import game.server.game.dto.v1.response.Response
import game.server.game.session.PlayerManager
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession

const val CANVAS_WIDTH = 800
const val CANVAS_HEIGHT = 600

@Component("MOVE")
class PlayerMoveHandler(
    private val playerManager: PlayerManager,
    private val movePublisher: MatchEventPublisher,
) : RequestHandler<PlayerMoveRequestData, MoveResponseData> {

    override suspend fun handle(
        request: ApiRequest<PlayerMoveRequestData>,
        socket: WebSocketSession
    ): ApiResponse<MoveResponseData> {
        val player = playerManager.getPlayer(socket)
            ?: return ErrorResponse(type = "move", message =  "Player not found")

        val newPosition = calculateNewPosition(request.data.currentPosition, request.data.direction, request.data.speed)

        return if (player.isMoveAllowed(newPosition.x, newPosition.y)) {
            player.position = newPosition
            publishMovement(player, newPosition)
            Response(type = "move", data = MoveResponseData(newPosition))
        } else {
            ErrorResponse(type = "move", message = "Move is not allowed")
        }
    }

    private fun calculateNewPosition(
        currentPosition: Position,
        direction: Direction,
        speed: Int
    ): Position = when (direction) {
        UP -> currentPosition.copy(y = currentPosition.y - speed)
        DOWN -> currentPosition.copy(y = currentPosition.y + speed)
        LEFT -> currentPosition.copy(x = currentPosition.x - speed)
        RIGHT -> currentPosition.copy(x = currentPosition.x + speed)
    }

    private suspend fun publishMovement(player: Player, newPosition: Position) {
        val receivers = playerManager.getReceivers(player.matchId)
            .map { it.sessionId }
            .filter { it != player.sessionId }

        val moveEvent = PlayerMoved(
            playerId = player.sessionId,
            matchId = player.matchId,
            newPositionX = newPosition.x,
            newPositionY = newPosition.y,
            receivers = receivers
        )
        movePublisher.publishPlayerMovement(moveEvent)
    }
}