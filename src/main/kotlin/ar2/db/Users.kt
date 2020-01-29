package ar2.db

import ar2.users.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 64)
    val passwordHash = varchar("password_hash", 128)
    val email = varchar("email",256)
    val name = varchar("name", 256)
    val isAdmin = bool("is_admin")
    val createdOn = datetime("created_on")
    val lastLogin = datetime("last_login")

    fun new(username: String, passwordHash: String, email: String, name: String, admin: Boolean) {
        transaction {
            Users.insert {
                it[this.email] = email
                it[this.username] = username
                it[this.passwordHash] = passwordHash
                it[this.name] = name
                it[this.isAdmin] = admin
                it[this.createdOn] = DateTime.now()
            }
        }
    }

    fun findByUsername(username: String): User = transaction {
        select {Users.username eq username}.single().toUser()
    }
}

fun ResultRow.toUser() = User(
        id = this[Users.id],
        username = this[Users.username],
        email = this[Users.email],
        name = this[Users.name],
        admin = this[Users.isAdmin]
)