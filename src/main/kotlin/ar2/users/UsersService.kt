package ar2.users

import java.lang.Exception
import org.jetbrains.exposed.sql.ResultRow

data class BaseUser(val username: String, val email: String, val name: String, val admin: Boolean)
data class User(val id: Int, val username: String, val email: String, val name: String, val admin: Boolean)

enum class UserRole {
    GUEST, DEVELOPER, MAINTAINER
}
class UserExists(val username: String) : Exception()

interface UsersService {
    fun findByUsername(username: String): User?
    fun findByUsernameRaw(username: String): ResultRow?
    fun newUser(username: String, email: String, password: String, name: String, admin: Boolean = false)
    fun changePassword(username: String, password: String)
}
