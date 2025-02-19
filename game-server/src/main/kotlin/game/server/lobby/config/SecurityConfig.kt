package game.server.lobby.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
open class SecurityConfig {

    @Bean
    open fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http
            .csrf { csrf -> csrf.disable()}
            .authorizeExchange { authorize ->
                authorize.pathMatchers("/login/**", "/oauth2/**").permitAll()
                authorize.anyExchange().authenticated()
            }
            .oauth2Login {}
        return http.build()
    }
}