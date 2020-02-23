package ar2.db

import ar2.users.BaseUser
import ar2.users.User
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object Users : IntIdTable("users") {
    val username = varchar("username", 64)
    val passwordHash = varchar("password_hash", 128)
    val email = varchar("email", 256)
    val name = varchar("name", 256)
    val isAdmin = bool("is_admin")
    val createdOn = datetime("created_on")
    val lastLogin = datetime("last_login")

    fun new(user: BaseUser, passwordHash: String): User {
        val resp: InsertStatement<Number> = transaction {
            Users.insert {
                it[this.email] = user.email
                it[this.username] = user.username
                it[this.passwordHash] = passwordHash
                it[this.name] = user.name
                it[this.isAdmin] = user.admin
                it[this.createdOn] = DateTime.now()
            }
        }
        return resp.resultedValues!!.single().toUser()
    }

    fun findByUsername(username: String): User? = transaction {
        select { Users.username eq username }.singleOrNull()?.toUser()
    }

    fun findAll(offset: Int, limit: Int): List<User> = transaction {
        selectAll().limit(limit, offset = offset).map { it.toUser() }
    }
}

fun ResultRow.toUser() = User(
        id = this[Users.id].value,
        obj = BaseUser(
            username = this[Users.username],
            email = this[Users.email],
            name = this[Users.name],
            admin = this[Users.isAdmin]
        )
)
