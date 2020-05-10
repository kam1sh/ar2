package ar2.services

import ar2.db.User
import java.time.LocalDateTime
import org.hibernate.SessionFactory
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory

class UsersServiceImpl(val securityService: SecurityService) : UsersService, KoinComponent {
    private val log = LoggerFactory.getLogger(UsersServiceImpl::class.java)

    private val factory: SessionFactory by inject()

    override fun findByUsername(username: String): User? {
        factory.openSession().use {
            val user = it.createQuery("from User where username = :username")
                .setParameter("username", username)
                .uniqueResult()
            return user as User?
        }
    }

    override fun newUser(request: User, password: String): User {
        if (findByUsername(request.username) != null) {
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

    override fun changePassword(username: String, password: String) {
        factory.openSession().use {
            val user = findByUsername(username)
            user?.passwordHash = securityService.encode(password)
            val tr = it.beginTransaction()
            it.update(user)
            tr.commit()
        }
    }
}
