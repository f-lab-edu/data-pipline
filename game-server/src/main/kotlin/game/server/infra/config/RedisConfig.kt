package game.server.infra.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.config.ObjectConfig
import game.server.lobby.service.UserSession
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@Import(ObjectConfig::class)
open class RedisConfig(
    private val objectMapper: ObjectMapper
) {
    @Bean
    open fun reactiveRedisUserSessionTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, UserSession> {
        val jsonSerializer = Jackson2JsonRedisSerializer(objectMapper, UserSession::class.java)
        val context = RedisSerializationContext.newSerializationContext<String, UserSession>(StringRedisSerializer())
            .value(jsonSerializer)
            .hashValue(jsonSerializer)
            .build()
        return ReactiveRedisTemplate(factory, context)
    }

    @Bean
    open fun reactiveRedisStringTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        val stringSerializer = StringRedisSerializer()
        val context = RedisSerializationContext.newSerializationContext<String, String>(stringSerializer)
            .value(stringSerializer)
            .hashValue(stringSerializer)
            .build()
        return ReactiveRedisTemplate(factory, context)
    }
}