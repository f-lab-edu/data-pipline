package game.infra.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.dto.v1.maching.Matched
import com.game.dto.v1.move.PlayerMoved
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import reactor.kafka.sender.SenderOptions

@Configuration
open class KafkaProducerConfig(
    private val objectMapper: ObjectMapper,
    @Value("\${kafka.ip}") private val kafkaIp: String,
    @Value("\${kafka.port}") private val kafkaPort: String,
) {

    @Bean
    open fun reactiveKafkaProducerTemplate(): ReactiveKafkaProducerTemplate<String, Matched> {
        val jsonSerializer = JsonSerializer<Matched>(objectMapper)
        val senderOptions = SenderOptions.create<String, Matched>(producerProps())
            .withValueSerializer(jsonSerializer)
        return ReactiveKafkaProducerTemplate(senderOptions)
    }

    @Bean
    open fun kafkaTemplatePlayerMoved(): ReactiveKafkaProducerTemplate<String, PlayerMoved> {
        val jsonSerializer = JsonSerializer<PlayerMoved>(objectMapper)
        val senderOptions = SenderOptions.create<String, PlayerMoved>(producerProps())
            .withValueSerializer(jsonSerializer)
        return ReactiveKafkaProducerTemplate(senderOptions)
    }

    private fun producerProps(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "$kafkaIp:$kafkaPort",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        )
    }
}
