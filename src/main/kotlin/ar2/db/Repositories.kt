package ar2.db

import org.jetbrains.exposed.dao.IntIdTable

object Repositories : IntIdTable("repository") {
    val name = varchar("name", 64)
    val groupId = (integer("group_id").references(Groups.id))
}
