package game.server.lobby.dto.v1.response

import game.server.lobby.domain.match.MatchType

sealed class MatchResponseDto(open val matchType: MatchType)

data class Matched(
    val matchId: String,
    val sessionIds: List<String>,
    override val matchType: MatchType
) : MatchResponseDto(matchType)

data class Waiting(
    override val matchType: MatchType
) : MatchResponseDto(matchType)