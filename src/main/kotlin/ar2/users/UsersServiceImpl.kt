package ar2.users

import ar2.security.SecurityService
import ar2.web.ContextHolder
import org.http4k.core.Request
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.slf4j.LoggerFactory

class UsersServiceImpl(val securityService: SecurityService): UsersService, KoinComponent {
    val log = LoggerFactory.getLogger(UsersServiceImpl::class.java)

    override fun getCurrentUser(request: Request): User? {
        return ContextHolder(request).currentUser
    }

    override fun findByUsername(username: String): User? = findByUsernameRaw(username)?.toUser()
    override fun findByUsernameRaw(username: String): ResultRow? = transaction { Users.select {Users.username eq username}.singleOrNull() }

    override fun newUser(username: String, email: String, password: String, name: String, admin: Boolean) {
        transaction {
            if (findByUsername(username) != null) {
                throw UserExists(username)
            }
            Users.insert {
                it[Users.email] = email
                it[Users.username] = username
                it[passwordHash] = securityService.encode(password)
                it[Users.name] = name
                it[Users.isAdmin] = admin
                it[Users.createdOn] = DateTime.now()
            }
        }
    }

    override fun changePassword(username: String, password: String) {
        transaction {
            Users.update({Users.username eq username}) {
                it[passwordHash] = securityService.encode(password)
            }
        }
    }

}

fun ResultRow.toUser() = User(
        id = this[Users.id],
        username = this[Users.username],
        email = this[Users.email],
        name = this[Users.name],
        admin = this[Users.isAdmin]
)
