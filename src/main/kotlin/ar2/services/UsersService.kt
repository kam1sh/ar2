package ar2.services

import ar2.db.entities.User
import java.lang.Exception

class UserExists(val username: String) : Exception()

interface UsersService {
    fun findByUsername(username: String): User?
    fun save(user: User)
    fun newUser(request: User, password: String): User
    fun changePassword(username: String, password: String)
}
