package ar2.lib.util

import org.hibernate.SessionFactory

/**
 * Removes all entities from database.
 */
fun SessionFactory.cleanAll() {
    openSession().use {
        val tr = it.beginTransaction()
        it.createQuery("delete User").executeUpdate()
        it.createQuery("delete Group").executeUpdate()
        it.createQuery("delete Project").executeUpdate()
        tr.commit()
    }
}
