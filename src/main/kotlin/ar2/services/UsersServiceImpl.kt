package ar2.services

import ar2.db.entities.User
import ar2.db.pagedQuery
import ar2.db.transaction
import ar2.exceptions.NoSuchUserException
import ar2.web.PageRequest
import java.time.LocalDateTime
import org.hibernate.SessionFactory
import org.koin.core.KoinComponent
import org.koin.core.inject

class UsersServiceImpl(private val securityService: SecurityService) : UsersService, KoinComponent {

    private val factory: SessionFactory by inject()

    override fun new(request: User, password: String): User {
        try {
            find(request.username)
            throw UserExists(request.username)
        } catch (ignored: NoSuchUserException) {}
        request.passwordHash = securityService.encode(password)
        request.createdOn = LocalDateTime.now()
        transaction { it.save(request) }
        return request
    }

    override fun list(pr: PageRequest): List<User> = factory.openSession().use {
        it.pagedQuery(pr, "from User", User::class.java)
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

    override fun remove(id: Int) {
        transaction {
            it.createQuery("delete User where id = :id")
                .setParameter("id", id)
                .executeUpdate()
        }
    }

    override fun remove(username: String) {
        transaction {
            it.createQuery("delete User where username = :username")
                .setParameter("username", username)
                .executeUpdate()
        }
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
