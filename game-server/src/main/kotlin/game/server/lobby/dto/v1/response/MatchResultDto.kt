package game.server.lobby.dto.v1.response

data class MatchResultDto(
    val matchId: String,
    val sessionIds: List<String>,
    val matchType: String
)
