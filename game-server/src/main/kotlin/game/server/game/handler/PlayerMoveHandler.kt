package game.server.game.handler

import com.fasterxml.jackson.core.type.TypeReference
import com.game.dto.v1.move.PlayerMoved
import com.game.service.v1.matching.MatchEventPublisher
import game.server.game.domain.player.Player
import game.server.game.service.RequestHandler
import game.server.game.domain.vo.Position
import game.server.game.domain.vo.Direction
import game.server.game.domain.vo.Direction.*
import game.server.game.dto.v1.request.PlayerMoveRequestData
import game.server.game.dto.v1.request.Request
import game.server.game.dto.v1.response.ApiResponse
import game.server.game.dto.v1.response.ErrorResponse
import game.server.game.dto.v1.response.MoveResponseData
import game.server.game.dto.v1.response.Response
import game.server.game.service.RequestService
import game.server.game.session.PlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession

const val CANVAS_WIDTH = 800
const val CANVAS_HEIGHT = 600

@Component("move")
class PlayerMoveHandler(
    private val playerManager: PlayerManager,
    private val movePublisher: MatchEventPublisher,
) : RequestHandler<PlayerMoveRequestData, MoveResponseData> {

    override val requestTypeReference: TypeReference<Request<PlayerMoveRequestData>> =
        object : TypeReference<Request<PlayerMoveRequestData>>() {}

    override fun handle(request: Request<PlayerMoveRequestData>, socket: WebSocketSession): ApiResponse<MoveResponseData> {
        val player = playerManager.getPlayer(socket)
            ?: return ErrorResponse(type = "move", message = "Player not found")
        val (currentX, currentY) = request.data.currentPosition
        val (newX, newY) = calculateNewPosition(currentX, currentY, request.data.direction, request.data.speed)
        return if (player.isMoveAllowed(newX, newY)) {
            player.position = Position(newX, newY)

            CoroutineScope(Dispatchers.IO).launch {
                movePublisher.publishPlayerMovement(
                    PlayerMoved(
                        seq = request.data.seq,
                        timestamp = request.data.timestamp,
                        playerId = request.data.senderSessionId,
                        matchId = player.matchId,
                        newPositionX = newX,
                        newPositionY = newY,
                        receivers = playerManager.getReceivers(player.matchId)
                            .map { it.sessionId }.filter { it != player.sessionId }
                    )
                )
            }

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