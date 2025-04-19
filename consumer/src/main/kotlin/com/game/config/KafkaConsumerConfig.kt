package com.game.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.dto.v1.maching.Matched
import com.game.dto.v1.move.PlayerMoved
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer

private const val ALL_PARTITION_FETCH_BYTES_CONFIG = 5 * 1024 * 1024 // 5MB
private const val EACH_PARTITION_FETCH_BYTES_CONFIG = 1 * 1024 * 1024 // 1MB

@Configuration
@EnableKafka
open class KafkaConsumerConfig(
    private val objectMapper: ObjectMapper,
    @Value("\${kafka.ip}") private val kafkaIp: String,
    @Value("\${kafka.port}") private val kafkaPort: String,
) {

    private fun commonConsumerProps(): Map<String, Any> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "$kafkaIp:$kafkaPort",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to JsonDeserializer::class.java.name
        )
    }

    private fun consumerPropsForTopic(customProps: Map<String, Any> = emptyMap()): Map<String, Any> {
        val defaultProps = commonConsumerProps()
        return defaultProps + customProps
    }

    private fun <T> createConsumerFactory(type: Class<T>, customProps: Map<String, Any> = emptyMap()): ConsumerFactory<String, T> {
        val jsonDeserializer = JsonDeserializer(type, objectMapper).apply {
            addTrustedPackages("*")
            setUseTypeHeaders(false)
        }

        val props = consumerPropsForTopic(customProps)
        return DefaultKafkaConsumerFactory(props, StringDeserializer(), jsonDeserializer)
    }

    private fun <T> createKafkaListenerContainerFactory(
        type: Class<T>,
        concurrency: Int,
        pollTimeout: Long,
        ackMode: ContainerProperties.AckMode = ContainerProperties.AckMode.BATCH,
        customProps: Map<String, Any> = emptyMap()
    ): ConcurrentKafkaListenerContainerFactory<String, T> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, T>()
        factory.consumerFactory = createConsumerFactory(type, customProps)
        factory.setConcurrency(concurrency)
        factory.containerProperties.pollTimeout = pollTimeout
        factory.containerProperties.ackMode = ackMode

        return factory
    }

    @Bean
    open fun matchedKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Matched> {
        return createKafkaListenerContainerFactory(
            type = Matched::class.java,
            concurrency = 1,
            pollTimeout = 1000L
        )
    }

    @Bean
    open fun playerMovedKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, PlayerMoved> {
        val customProps = mapOf(
            ConsumerConfig.FETCH_MIN_BYTES_CONFIG to 1,
            ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG to 1,
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 10,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG to EACH_PARTITION_FETCH_BYTES_CONFIG,
            ConsumerConfig.FETCH_MAX_BYTES_CONFIG to ALL_PARTITION_FETCH_BYTES_CONFIG,
        )
        return createKafkaListenerContainerFactory(
            type = PlayerMoved::class.java,
            concurrency = 4,
            pollTimeout = 200L,
            ackMode = ContainerProperties.AckMode.MANUAL,
            customProps = customProps
        )
    }
}