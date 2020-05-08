package ar2.services

import ar2.users.BaseUser
import ar2.users.User
import java.lang.Exception
import org.jetbrains.exposed.sql.ResultRow

class UserExists(val username: String) : Exception()

interface UsersService {
    fun findByUsername(username: String): User?
    fun findByUsernameRaw(username: String): ResultRow?
    fun newUser(request: BaseUser, password: String): User
    fun changePassword(username: String, password: String)
}
