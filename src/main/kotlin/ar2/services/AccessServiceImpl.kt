package ar2.services

import ar2.db.Repository
import ar2.db.User
import ar2.users.Role

class AccessServiceImpl : AccessService {
    override fun giveGroupRole(user: User, group: String, role: Role) {
        TODO("Not yet implemented")
    }

    override fun giveRepositoryRole(user: User, repo: Repository, role: Role) {
        TODO("Not yet implemented")
    }

    override fun hasRole(user: User, repo: Repository, role: Role): Boolean {
        TODO("Not yet implemented")
/*
        val lst: List<Role> = listOf(Role.DEVELOPER, Role.MAINTAINER)
        return lst.maxBy { it.code }!!.code >= role.code
*/
    }
}
