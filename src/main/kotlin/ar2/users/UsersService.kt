package ar2.users

import java.lang.Exception

interface UsersService {
    enum class UserRole {
        GUEST, DEVELOPER, MAINTAINER
    }

    fun newUser(username: String, email: String, password: String, name: String, admin: Boolean = false)
    class UserExists(val username: String): Exception()
    fun changePassword(username: String, password: String)
}