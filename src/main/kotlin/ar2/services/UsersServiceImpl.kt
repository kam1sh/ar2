package ar2.services

import ar2.db.entities.User
import java.time.LocalDateTime
import org.hibernate.SessionFactory
import org.koin.core.KoinComponent
import org.koin.core.inject

class UsersServiceImpl(private val securityService: SecurityService) : UsersService, KoinComponent {

    private val factory: SessionFactory by inject()

    override fun new(request: User, password: String): User {
        if (find(request.username) != null) {
            throw UserExists(request.username)
        }
        request.passwordHash = securityService.encode(password)
        request.createdOn = LocalDateTime.now()
        factory.openSession().use {
            val tr = it.beginTransaction()
            it.save(request)
            tr.commit()
        }
        return request
    }

    override fun list(offset: Int, limit: Int): List<User> = factory.openSession().use {
        it.createQuery("from User", User::class.java)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .list()
    }

    override fun find(username: String): User? {
        factory.openSession().use {
            val user = it.createQuery("from User where username = :username")
                .setParameter("username", username)
                .uniqueResult()
            return user as User?
        }
    }

    override fun find(id: Int): User = factory.openSession().use {
        it.find(User::class.java, id)
    }

    override fun update(user: User) {
        factory.openSession().use {
            val tr = it.beginTransaction()
            it.update(user)
            tr.commit()
        }
    }

    override fun remove(id: Int) = factory.openSession().use {
        val tr = it.beginTransaction()
        it.createQuery("delete User where id = :id")
            .setParameter("id", id)
            .executeUpdate()
        tr.commit()
    }

    override fun remove(username: String) = factory.openSession().use {
        val tr = it.beginTransaction()
        it.createQuery("delete User where username = :username")
            .setParameter("username", username)
            .executeUpdate()
        tr.commit()
    }

    override fun changePassword(username: String, password: String) {
        factory.openSession().use {
            val user = find(username)
            user?.passwordHash = securityService.encode(password)
            val tr = it.beginTransaction()
            it.update(user)
            tr.commit()
        }
    }
}
