package ar2.services.impl

import ar2.db.entities.User
import ar2.db.entities.assertAdmin
import ar2.db.pagedQuery
import ar2.db.transaction
import ar2.exceptions.IllegalActionException
import ar2.exceptions.user.NoSuchUserException
import ar2.exceptions.user.UserDisabledException
import ar2.exceptions.user.UserExistsException
import ar2.services.SecurityService
import ar2.services.UsersService
import ar2.web.PageRequest
import java.time.LocalDateTime
import org.hibernate.SessionFactory
import org.koin.core.KoinComponent
import org.koin.core.inject

class UsersServiceImpl(private val securityService: SecurityService) : UsersService, KoinComponent {

    private val factory: SessionFactory by inject()

    override fun new(form: User, password: String, issuer: User?): User {
        issuer?.assertAdmin()
        // firstly try to find existing user
        try {
            val user = find(form.username)
            throw UserExistsException(user)
        } catch (ignored: NoSuchUserException) {}
        // and then create new user
        val user = form.copy(
            id = null,
            passwordHash = securityService.encode(password),
            createdOn = LocalDateTime.now(),
            lastLogin = null,
            disabled = false
        )
        transaction { it.save(user) }
        return user
    }

    override fun newOrEnable(request: User, password: String): User {
        return try {
            new(request, password)
        } catch (exists: UserExistsException) {
            enable(exists.user, null)
            exists.user
        }
    }

    override fun find(pr: PageRequest): List<User> = factory.openSession().use {
        it.pagedQuery(pr, "from User where disabled = false", User::class.java)
    }

    override fun find(username: String): User {
        factory.openSession().use {
            val user = it.createQuery("from User where username = :username")
                .setParameter("username", username)
                .uniqueResult() as User?
            return user ?: throw NoSuchUserException()
        }
    }

    override fun find(id: Int): User = factory.openSession().use {
        it.find(User::class.java, id) ?: throw NoSuchUserException()
    }

    override fun update(id: Int, form: User, password: String, issuer: User): User {
        // if issuer is not admin or updates not the same user, then throw an exception
        if (!issuer.isAdmin && issuer.id != id)
            throw IllegalActionException("Not enough privileges to change other users.", "NO_PRIVILEGES")
        val user = find(id).apply {
            name = form.name
            email = form.name
            passwordHash = securityService.encode(password)
        }
        if (user.disabled) throw UserDisabledException()
        forceUpdate(user)
        return user
    }

    override fun update(user: User) = forceUpdate(user)

    private fun forceUpdate(user: User) = transaction {
        it.update(user)
    }

    override fun disable(user: User, issuer: User?) {
        issuer?.let {
            it.assertAdmin()
            if (it.id == user.id) throw IllegalActionException("You cannot disable yourself.", "CANNOT_DISABLE_YOURSELF")
        }
        user.disabled = true
        forceUpdate(user)
    }

    override fun enable(user: User, issuer: User?) {
        issuer?.assertAdmin()
        user.disabled = false
        forceUpdate(user)
    }

    override fun changePassword(username: String, password: String) {
        transaction {
            it.createQuery("update User set passwordHash = :hash where username = :username")
                .setParameter("username", username)
                .setParameter("hash", securityService.encode(password))
                .executeUpdate()
        }
    }
}
