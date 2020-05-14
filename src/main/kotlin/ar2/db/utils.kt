package ar2.db

import ar2.web.PageRequest
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.koin.core.KoinComponent
import org.koin.core.inject

object SessionFactoryHolder : KoinComponent {
    val factory: SessionFactory by inject()
    val session
        get() = factory.openSession()
}

fun <T> transaction(statement: (Session) -> T): T {
    SessionFactoryHolder.session.use {
        val tr = it.beginTransaction()
        try {
            val result: T = statement(it)
            tr.commit()
            return result
        } catch (e: Exception) {
            tr.rollback()
            throw e
        }
    }
}

fun <T> Session.pagedQuery(pr: PageRequest, query: String, cls: Class<T>): List<T> =
    createQuery(query, cls)
        .setFirstResult(pr.offset)
        .setMaxResults(pr.limit)
        .list()
