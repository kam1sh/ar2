package ar2.services

import ar2.db.Group
import org.hibernate.SessionFactory
import org.koin.core.KoinComponent
import org.koin.core.inject

class GroupsServiceImpl : GroupsService, KoinComponent {

    private val sessions: SessionFactory by inject()

    @Suppress("UNCHECKED_CAST")
    override fun listGroups(limit: Int, offset: Int): List<Group> {
        return sessions.openSession().use {
            it.createQuery("from Group")
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list() as List<Group>
        }
    }

    override fun newGroup(name: String) {
        TODO("Not yet implemented")
    }
}