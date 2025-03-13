package game.server.lobby.dto.v1.response

import game.server.lobby.domain.match.MatchType

data class MatchResponseDto(
    val status: MatchStatus = MatchStatus.MATCHED,
    val matchId: String?,
    val sessionIds: List<String>,
    val matchType: MatchType
)

enum class MatchStatus {
    MATCHED, WAITING
}