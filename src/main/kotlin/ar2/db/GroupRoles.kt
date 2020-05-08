package ar2.db

import ar2.users.Role
import org.jetbrains.exposed.dao.IntIdTable

object GroupRoles : IntIdTable("group_roles") {
    val groupId = (integer("group_id").references(Groups.id))
    val userId = (integer("user_id").references(Users.id))
    val role = enumerationByName("role", length = 24, klass = Role::class)
}
