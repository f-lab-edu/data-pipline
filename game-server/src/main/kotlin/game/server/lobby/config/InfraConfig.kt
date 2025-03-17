package game.server.lobby.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories


@Configuration
@EnableR2dbcRepositories(basePackages = ["game.infra"])
@ComponentScan(basePackages = ["game.infra"])
open class InfraConfig