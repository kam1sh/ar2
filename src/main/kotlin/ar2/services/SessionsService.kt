package ar2.services

import ar2.db.Session
import ar2.db.User
import java.time.LocalDateTime

interface SessionsService {
    fun new(session: Session)
    fun findUser(cookieValue: String): User
    fun pruneOld(dt: LocalDateTime)
}