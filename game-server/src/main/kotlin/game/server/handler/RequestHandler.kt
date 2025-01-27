package game.server.handler

import game.server.dto.Request

interface RequestHandler<T : Request> {
    fun handle(request: T): String
}