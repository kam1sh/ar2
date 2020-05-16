package ar2.services

import ar2.db.entities.Group
import ar2.db.entities.User
import ar2.users.Role
import ar2.web.PageRequest

interface GroupsService {
    fun new(name: String, owner: User)
    fun find(name: String): Group
    fun list(pr: PageRequest): List<Group>
    fun remove(name: String)

    fun setOwner(name: String, owner: User)
    fun addUserRole(groupName: String, user: User, role: Role)
    fun findUserRole(groupName: String, user: User): Role?
}
