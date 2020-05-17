package ar2.services

import ar2.db.entities.User
import ar2.db.entities.assertAdmin
import ar2.db.pagedQuery
import ar2.db.transaction
import ar2.exceptions.IllegalActionException
import ar2.exceptions.NoSuchUserException
import ar2.web.PageRequest
import java.time.LocalDateTime
import org.hibernate.SessionFactory
import org.koin.core.KoinComponent
import org.koin.core.inject

class UsersServiceImpl(private val securityService: SecurityService) : UsersService, KoinComponent {

    private val factory: SessionFactory by inject()

    override fun new(request: User, password: String, issuer: User): User {
        issuer.assertAdmin()
        return new(request, password)
    }

    override fun new(request: User, password: String): User {
        // firstly try to find and restore user
        try {
            val user = find(request.username)
            if (!user.disabled) throw IllegalActionException("User exists.", "USER_EXISTS")
            enable(user)
            return user
        } catch (ignored: NoSuchUserException) {}
        // and then create new user
        val user = request.copy(
            id = null,
            passwordHash = securityService.encode(password),
            createdOn = LocalDateTime.now(),
            lastLogin = null,
            disabled = false
        )
        transaction { it.save(user) }
        return user
    }


    override fun list(pr: PageRequest): List<User> = factory.openSession().use {
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

    override fun update(user: User) = transaction {
        it.update(user)
    }

    override fun disable(user: User) {
        user.disabled = true
        transaction { it.update(user) }
    }

    override fun enable(user: User) {
        user.disabled = false
        transaction { it.update(user) }
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
