package ar2.services

import ar2.db.entities.Session
import ar2.db.entities.User
import java.time.LocalDateTime
import org.http4k.core.Request
import org.koin.core.KoinComponent
import org.koin.core.get

interface SessionsService {

    fun new(user: User): Session
    fun new(session: Session)

    fun attachUser(request: Request)
    fun findUser(request: Request): User

    fun findUser(cookieValue: String): User?
    fun pruneOld(dt: LocalDateTime)
}

fun KoinComponent.extractUser(request: Request) = get<SessionsService>().findUser(request)
