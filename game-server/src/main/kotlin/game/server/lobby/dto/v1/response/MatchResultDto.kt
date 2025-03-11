package game.server.lobby.dto.v1.response

import game.server.lobby.domain.match.MatchType

data class MatchResultDto(
    val matchId: String,
    val sessionIds: List<String>,
    val matchType: MatchType
)
