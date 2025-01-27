package game.server.dto


data class EnemySpawnResponse(
    val enemyId: String,
    val status: EnemyStatus,
    val position: Position
)

data class EnemyStatus(
    val name: String,
    val health: Int,
    val damage: Int,
    val defense: Int,
    val width: Int,
    val height: Int
)