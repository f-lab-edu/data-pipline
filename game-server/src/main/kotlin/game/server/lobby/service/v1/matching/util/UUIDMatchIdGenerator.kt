package game.server.lobby.service.v1.matching.util

import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UUIDMatchIdGenerator : MatchIdGenerator {
    override fun generate(): String = UUID.randomUUID().toString()
}