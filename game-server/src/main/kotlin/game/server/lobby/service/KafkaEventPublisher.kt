package game.server.lobby.service

import game.server.lobby.dto.v1.response.MatchResultDto
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class KafkaEventPublisher(
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>
) {
    fun publishMatchStart(matchResultDto: MatchResultDto): Mono<Void> {
        return kafkaTemplate.send(
            "match-start-topic",
            matchResultDto.matchId,
            matchResultDto
        ).then()
    }
}