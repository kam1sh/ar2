package ar2.services

import ar2.db.entities.User
import ar2.web.PageRequest

interface UsersService {
    /**
     * Creates new user from request and password.
     * @throws UserExistsException if user already exists
     */
    fun new(form: User, password: String, issuer: User? = null): User

    /**
     * Creates new user or enables back existing user
     * with same username.
     */
    fun newOrEnable(request: User, password: String): User

    /**
     * Lists all enabled users.
     */
    fun find(pr: PageRequest, issuer: User): List<User>

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
     * @throws UserDisabledExce when user disabled.
     */
    fun update(id: Int, form: User, password: String, issuer: User): User

    /**
     * Saves user information.
     */
    fun update(user: User)

    /**
     * Disables user.
     */
    fun disable(user: User, issuer: User?)

    /**
     * Enables back user by its username.
     */
    fun enable(user: User, issuer: User?)

    fun changePassword(username: String, password: String)
}
