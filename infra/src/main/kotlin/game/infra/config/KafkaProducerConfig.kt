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


private const val BATCH_SIZE = 512 * 1024 // 512KB
private const val BUFFER_MEMORY = 1 * 1024 * 1024 * 1024 // 1GB

@Configuration
open class KafkaProducerConfig(
    private val objectMapper: ObjectMapper,
    @Value("\${kafka.ip}") private val kafkaIp: String,
    @Value("\${kafka.port}") private val kafkaPort: String,
) {

    private fun commonProducerProps(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "$kafkaIp:$kafkaPort",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        )
    }

    private fun producerPropsForTopic(customProps: Map<String, Any> = emptyMap()): Map<String, Any> {
        val defaultProps = commonProducerProps()
        return defaultProps + customProps
    }

    private fun <T> createTemplate(customProps: Map<String, Any>): ReactiveKafkaProducerTemplate<String, T> {
        val jsonSerializer = JsonSerializer<T>(objectMapper)
        val senderOptions = SenderOptions.create<String, T>(customProps)
            .withValueSerializer(jsonSerializer)
        return ReactiveKafkaProducerTemplate(senderOptions)
    }

    @Bean
    open fun reactiveKafkaProducerTemplateForMatched(): ReactiveKafkaProducerTemplate<String, Matched> {
        val matchedProps = producerPropsForTopic()
        return createTemplate(matchedProps)
    }

    @Bean
    open fun reactiveKafkaProducerTemplateForPlayerMoved(): ReactiveKafkaProducerTemplate<String, PlayerMoved> {
        val playerMovedProps = producerPropsForTopic(
            mapOf(
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to false,
                ProducerConfig.ACKS_CONFIG to "0",
                ProducerConfig.RETRIES_CONFIG to 0,
                ProducerConfig.BATCH_SIZE_CONFIG to BATCH_SIZE,
                ProducerConfig.LINGER_MS_CONFIG to 5,
                ProducerConfig.BUFFER_MEMORY_CONFIG to BUFFER_MEMORY,
            )
        )
        return createTemplate(playerMovedProps)
    }
}