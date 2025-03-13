package game.server.lobby.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.*

@Configuration
open class RouterConfig {
    @Bean
    open fun loginRouter(): RouterFunction<ServerResponse> {
        return RouterFunctions.route(
            RequestPredicates.GET("/login")
        ) { request: ServerRequest? ->
            ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(
                    BodyInserters.fromResource(
                        ClassPathResource(
                            "static/login.html"
                        )
                    )
                )
        }
    }
}
