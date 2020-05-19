package ar2.facades

import ar2.db.entities.User

interface UsersFacade {
    fun new(form: User, password: String, issuer: User): User

    fun disable(userId: Int, issuer: User)
    fun disable(username: String, issuer: User)
    fun disable(username: String)
    fun enable(username: String, issuer: User)
}
