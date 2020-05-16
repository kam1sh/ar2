package ar2.services

import ar2.db.entities.Project
import ar2.db.entities.User
import ar2.users.Role

interface AccessService {
    fun giveGroupRole(user: User, group: String, role: Role)
    fun giveRepositoryRole(user: User, repo: Project, role: Role)
    fun hasRole(user: User, repo: Project, role: Role): Boolean
}
