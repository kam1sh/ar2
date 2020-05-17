package ar2.services

import ar2.db.entities.User
import ar2.exceptions.WebError
import ar2.web.PageRequest
import org.http4k.core.Status

class UserExists : WebError(Status.CONFLICT, "User exists.", "USER_EXISTS")
class UserDisabled : WebError(Status.BAD_REQUEST, "User exists, but disabled.", "USER EXISTS")

interface UsersService {
    /**
     * Creates new user from request and password.
     * @throws UserExists when user with such username already exists.
     * @throws UserDisabled when user already exists, but disabled.
     */
    fun new(request: User, password: String, issuer: User): User

    /**
     * Creates new user without checking issuer for admin privs.
     * Used by tests.
     */
    fun new(request: User, password: String): User

    /**
     * Lists all enabled users.
     */
    fun list(pr: PageRequest): List<User>

    /**
     * Finds user (even disabled) by username.
     */
    fun find(username: String): User

    /**
     * Finds user (even disabled) by ID.
     */
    fun find(id: Int): User

    /**
     * Saves new user information.
     * @throws UserDisabled when user disabled.
     */
    fun update(user: User)

    /**
     * Disables user.
     */
    fun disable(user: User)

    /**
     * Enables back user by its username.
     */
    fun enable(user: User)

    fun changePassword(username: String, password: String)
}
