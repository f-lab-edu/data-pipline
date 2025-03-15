package game.server.lobby.service.v1.matching

import game.server.lobby.dto.v1.response.Matched

interface MatchEventPublisher {
    suspend fun publishMatchStart(matched: Matched)
}
