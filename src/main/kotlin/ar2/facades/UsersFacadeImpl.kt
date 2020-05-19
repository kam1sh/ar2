package ar2.facades

import ar2.db.entities.User
import ar2.db.entities.assertAdmin
import ar2.exceptions.IllegalActionException
import ar2.services.UsersService
import org.koin.core.KoinComponent

class UsersFacadeImpl(
    private val service: UsersService
) : UsersFacade, KoinComponent {

    override fun new(form: User, password: String, issuer: User): User {
        issuer.assertAdmin()
        return service.new(form, password)
    }

    override fun disable(userId: Int, issuer: User) {
        issuer.assertAdmin()
        val user = service.find(userId)
        doDisable(user, issuer)
    }

    override fun disable(username: String, issuer: User) {
        issuer.assertAdmin()
        val user = service.find(username)
        doDisable(user, issuer)
    }

    override fun disable(username: String) {
        val user = service.find(username)
        service.disable(user)
    }

    private fun doDisable(user: User, issuer: User) {
        if (issuer.id == user.id) throw IllegalActionException("You cannot disable yourself.", "CANNOT_DISABLE_YOURSELF")
        service.disable(user)
    }

    override fun enable(username: String, issuer: User) {
        issuer.assertAdmin()
        val user = service.find(username)
        service.enable(user)
    }
}
