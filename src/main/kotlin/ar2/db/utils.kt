package ar2.db

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.koin.core.KoinComponent
import org.koin.core.inject

object SessionFactoryHolder : KoinComponent {
    val factory: SessionFactory by inject()
}

fun <T> transaction(statement: (Session) -> T): T {
    SessionFactoryHolder.factory.openSession().use {
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