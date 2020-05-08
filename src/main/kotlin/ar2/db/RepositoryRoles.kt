package ar2.db

import ar2.users.Role
import org.jetbrains.exposed.dao.IntIdTable

object RepositoryRoles : IntIdTable("repository_roles") {
    val repoId = (integer("repo_id").references(Repositories.id))
    val userId = (integer("user_id").references(Users.id))
    val role = enumerationByName("role", length = 24, klass = Role::class)
}
