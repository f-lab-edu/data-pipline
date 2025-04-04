package game.server.config

import ch.qos.logback.classic.spi.ILoggingEvent
import com.fasterxml.jackson.core.JsonGenerator
import game.server.dto.request.ApiRequest
import game.server.dto.response.ApiResponse
import game.server.dto.response.ErrorResponse
import net.logstash.logback.composite.AbstractJsonProvider

class CustomJsonProvider : AbstractJsonProvider<ILoggingEvent>() {

    override fun writeTo(generator: JsonGenerator, event: ILoggingEvent) {
        val message = event.argumentArray?.firstOrNull()

        try {
            when (message) {
                is ApiResponse<*> -> {
                    generator.writeStringField("messageType", message.messageType)
                    generator.writeStringField("type", message.type)
                    generator.writeBooleanField("success", message.success)
                    generator.writeObjectField("data", message.data)
                    if (message is ErrorResponse<*>) generator.writeStringField("message", message.message)
                }
                is ApiRequest<*> -> {
                    generator.writeStringField("messageType", message.messageType)
                    generator.writeStringField("type", message.type)
                    generator.writeObjectField("data", message.data)
                }
                else -> {
                    generator.writeStringField("message", event.formattedMessage)
                }
            }
        } catch (e: Exception) {
            generator.writeStringField("error", e.message)
        }
    }
}