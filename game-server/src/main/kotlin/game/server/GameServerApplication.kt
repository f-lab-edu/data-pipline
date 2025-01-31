package game.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
open class GameServerApplication

fun main(args: Array<String>) {
    runApplication<GameServerApplication>(*args)
}