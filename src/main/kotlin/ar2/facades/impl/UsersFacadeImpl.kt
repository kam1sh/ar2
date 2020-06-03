package ar2.facades.impl

import ar2.db.entities.User
import ar2.facades.UsersFacade
import ar2.services.GroupsService
import ar2.services.UsersService
import ar2.web.PageRequest
import org.koin.core.KoinComponent

class UsersFacadeImpl(
    private val service: UsersService,
    private val groupsService: GroupsService
) : UsersFacade, KoinComponent {

    override fun new(form: User, password: String, issuer: User?): User {
        val user = service.new(form, password, issuer)
        groupsService.new(user.username, user)
        return user
    }

    override fun find(username: String) = service.find(username)
    override fun find(id: Int) = service.find(id)
    override fun find(pr: PageRequest, issuer: User) = service.find(pr, issuer)

    override fun update(id: Int, form: User, password: String, issuer: User) {
        service.update(id, form, password, issuer)
    }

    override fun disable(userId: Int, issuer: User) {
        val user = service.find(userId)
        service.disable(user, issuer)
    }

    override fun disable(username: String, issuer: User) {
        val user = service.find(username)
        service.disable(user, issuer)
    }

    override fun enable(username: String, issuer: User) {
        val user = service.find(username)
        service.enable(user, issuer)
    }
}
