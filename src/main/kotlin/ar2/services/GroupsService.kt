package ar2.services

import ar2.db.entities.Group

interface GroupsService {
    fun listGroups(limit: Int, offset: Int): List<Group>
    fun newGroup(name: String)
}
