package ar2.services

import ar2.db.entities.Session
import ar2.db.entities.User
import java.time.LocalDateTime

interface SessionsService {
    fun new(session: Session)
    fun findUser(cookieValue: String): User?
    fun pruneOld(dt: LocalDateTime)
}
