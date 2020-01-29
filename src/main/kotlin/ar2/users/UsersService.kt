package ar2.users

import org.http4k.core.Request
import org.jetbrains.exposed.sql.ResultRow
import java.lang.Exception

data class User(val id: Int, val username: String, val email: String, val name: String, val admin: Boolean)

enum class UserRole {
    GUEST, DEVELOPER, MAINTAINER
}
class UserExists(val username: String): Exception()

interface UsersService {
    fun findByUsername(username: String): User?
    fun findByUsernameRaw(username: String): ResultRow?
    fun newUser(username: String, email: String, password: String, name: String, admin: Boolean = false)
    fun changePassword(username: String, password: String)

}