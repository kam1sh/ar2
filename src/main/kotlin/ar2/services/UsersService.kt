package ar2.services

import ar2.db.entities.User
import ar2.web.PageRequest
import java.lang.Exception

class UserExists(val username: String) : Exception()

interface UsersService {
    fun new(request: User, password: String): User
    fun list(pr: PageRequest): List<User>
    fun find(username: String): User
    fun find(id: Int): User
    fun update(user: User)
    fun remove(id: Int)
    fun remove(username: String)
    fun changePassword(username: String, password: String)
}
