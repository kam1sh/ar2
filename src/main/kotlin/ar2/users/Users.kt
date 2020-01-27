package ar2.users

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 64)
    val passwordHash = varchar("password_hash", 128)
    val email = varchar("email",256)
    val name = varchar("name", 256)
    val isAdmin = bool("is_admin")
    val createdOn = datetime("created_on")
    val lastLogin = datetime("last_login")
}