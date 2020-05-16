package ar2.services

import ar2.db.entities.Group
import ar2.db.entities.GroupRole
import ar2.db.entities.User
import ar2.db.pagedQuery
import ar2.db.transaction
import ar2.exceptions.GroupRoleExistsException
import ar2.exceptions.NoSuchGroupException
import ar2.users.Role
import ar2.web.PageRequest
import org.hibernate.SessionFactory
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory
class GroupsServiceImpl : GroupsService, KoinComponent {
    private val log = LoggerFactory.getLogger(GroupsServiceImpl::class.java)

    private val sessions: SessionFactory by inject()

    override fun list(pr: PageRequest): List<Group> =
        sessions.openSession().use {
            it.pagedQuery(pr, "from Group", Group::class.java)
        }

    override fun new(name: String, owner: User) {
        val group = Group(name = name, owner = owner)
        transaction { it.save(group) }
    }

    override fun find(name: String): Group =
        sessions.openSession().use {
            it.createQuery("from Group where name = :name", Group::class.java)
                .setParameter("name", name)
                .uniqueResult() ?: throw NoSuchGroupException()
        }

    override fun remove(name: String) {
        transaction {
            it.createQuery("delete Group where name = :name")
                .setParameter("name", name)
                .executeUpdate()
        }
    }

    override fun setOwner(name: String, owner: User) {
        transaction {
            it.createQuery("update Group set owner = :owner where name = :name")
                .setParameter("owner", owner)
                .setParameter("name", name)
        }
    }

    override fun addUserRole(groupName: String, user: User, role: Role) {
        val group = find(groupName)
        val groupRole = GroupRole(
            userId = user.id, groupId = group.id, role = role
        )
        if (groupRole.existsInStorage()) throw GroupRoleExistsException()
        transaction { it.save(groupRole) }
    }

    override fun findUserRole(groupName: String, user: User): Role? {
        val userId = user.id ?: throw IllegalArgumentException("User.id is null.")
        val group = find(groupName)
        return findGroupRole(group.id!!, userId)?.role
    }

    private fun GroupRole.existsInStorage(): Boolean {
        groupId ?: throw IllegalArgumentException("groupId is null.")
        userId ?: throw IllegalArgumentException("userId is null.")
        return findGroupRole(groupId = groupId!!, userId = userId!!) != null
    }

    private fun findGroupRole(groupId: Int, userId: Int): GroupRole? =
        sessions.openSession().use {
            val result = it.createQuery("from GroupRole gr where gr.userId = :userId and gr.groupId = :groupId", GroupRole::class.java)
                .setParameter("userId", userId)
                .setParameter("groupId", groupId)
                .uniqueResult()
            log.debug("query result: {}", result)
            result
        }
}
