package ar2.services

import ar2.Config
import ar2.db.entities.Session
import ar2.db.entities.User
import ar2.db.transaction
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom
import org.hibernate.SessionFactory
import org.http4k.base64Encode
import org.koin.core.KoinComponent
import org.koin.core.inject

class SessionsServiceImpl : SessionsService, KoinComponent {

    private val sessions: SessionFactory by inject()
    private val cfg: Config by inject()

    override fun new(user: User): Session {
        user.lastLogin = LocalDateTime.now()
        val byteArr = ByteArray(10)
        ThreadLocalRandom.current().nextBytes(byteArr)
        val cookieValue = String(byteArr).base64Encode()
        val expires = LocalDateTime.now().plusDays(cfg.security.sessionLifetimeDays.toLong())
        val session = Session(cookieValue, user, expires)
        new(session)
        return session
    }

    override fun new(session: Session) {
        transaction {
            it.save(session)
        }
    }

    override fun findUser(cookieValue: String): User? {
        val user = sessions.openSession().use {
            it.get(Session::class.java, cookieValue)?.user
        }
        if (user?.disabled == true) return null
        return user
    }

    override fun pruneOld(dt: LocalDateTime) {
        transaction {
            it.createQuery("delete Session where expires <= :dt")
                .setParameter("dt", dt)
                .executeUpdate()
        }
    }
}
