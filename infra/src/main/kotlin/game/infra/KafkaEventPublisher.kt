package game.infra

import com.game.dto.v1.maching.Matched
import com.game.dto.v1.move.PlayerMoved
import com.game.service.v1.matching.MatchEventPublisher
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Repository

@Repository
open class KafkaEventPublisher(
    @Value("\${kafka.topic.match-start}") private val matchStartTopic: String,
    @Value("\${kafka.topic.player-move}") private val playerMoveTopic: String,
    private val kafkaTemplateMatched: ReactiveKafkaProducerTemplate<String, Matched>,
    private val kafkaTemplatePlayerMoved: ReactiveKafkaProducerTemplate<String, PlayerMoved>,
) : MatchEventPublisher {

    override suspend fun publishMatchStart(matched: Matched) {
        kafkaTemplateMatched.send(matchStartTopic, matched.matchId, matched).awaitSingle()
    }

    override suspend fun publishPlayerMovement(playerMoved: PlayerMoved) {
        kafkaTemplatePlayerMoved.send(playerMoveTopic, playerMoved.matchId, playerMoved).awaitSingle()
    }

}