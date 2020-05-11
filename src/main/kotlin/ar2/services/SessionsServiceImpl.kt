package ar2.services

import ar2.db.entities.Session
import ar2.db.entities.User
import java.time.LocalDateTime
import org.hibernate.SessionFactory
import org.koin.core.KoinComponent
import org.koin.core.inject

class SessionsServiceImpl : SessionsService, KoinComponent {

    private val sessions: SessionFactory by inject()

    override fun new(session: Session) {
        sessions.openSession().use {
            val tr = it.beginTransaction()
            it.save(session)
            tr.commit()
        }
    }

    override fun findUser(cookieValue: String): User {
        return sessions.openSession().use {
            it.get(Session::class.java, cookieValue).user
        }
    }

    override fun pruneOld(dt: LocalDateTime) {
        sessions.openSession().use {
            val tr = it.beginTransaction()
            it.createQuery("delete Session where expires <= :dt")
                .setParameter("dt", dt)
                .executeUpdate()
            tr.commit()
        }
    }
}
