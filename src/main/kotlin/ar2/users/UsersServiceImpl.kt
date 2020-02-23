package ar2.users

import ar2.db.Users
import ar2.security.SecurityService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.KoinComponent
import org.slf4j.LoggerFactory

class UsersServiceImpl(val securityService: SecurityService) : UsersService, KoinComponent {
    val log = LoggerFactory.getLogger(UsersServiceImpl::class.java)

    override fun findByUsername(username: String): User? = Users.findByUsername(username)
    override fun findByUsernameRaw(username: String): ResultRow? = transaction { Users.select { Users.username eq username }.singleOrNull() }

    override fun newUser(request: BaseUser, password: String): User {
        if (findByUsername(request.username) != null) {
            throw UserExists(request.username)
        }
        return Users.new(request, passwordHash = securityService.encode(password))
    }

    override fun changePassword(username: String, password: String) {
        transaction {
            Users.update({ Users.username eq username }) {
                it[passwordHash] = securityService.encode(password)
            }
        }
    }
}
