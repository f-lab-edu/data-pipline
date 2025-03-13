package game.server.lobby.config


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
open class SecurityConfig(
    private val oAuth2SuccessHandler: OAuth2SuccessHandler
) {

    @Bean
    open fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { csrf -> csrf.disable()}
            .authorizeExchange { authorize ->
                authorize.pathMatchers("/login/**", "/oauth2/**").permitAll()
                authorize.anyExchange().authenticated()
            }
            .formLogin { it.loginPage("/login") }
            .oauth2Login { oauth2 ->
                oauth2.authenticationSuccessHandler(oAuth2SuccessHandler)
            }.build()
    }
}