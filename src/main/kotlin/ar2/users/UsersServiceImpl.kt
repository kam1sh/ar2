package ar2.users

import ar2.security.SecurityService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory

class UsersServiceImpl(val securityService: SecurityService): UsersService, KoinComponent {
    val log = LoggerFactory.getLogger(UsersServiceImpl::class.java)

    val db: Database by inject()

    override fun newUser(username: String, email: String, password: String, name: String, admin: Boolean) {
        transaction {
            if (User.select {User.username eq username}.singleOrNull() != null) {
                throw UsersService.UserExists(username)
            }
            User.insert {
                it[User.email] = email
                it[User.username] = username
                it[passwordHash] = securityService.encode(password)
                it[User.name] = name
                it[User.isAdmin] = admin
                it[User.createdOn] = DateTime.now()
            }
        }
    }
}