package ar2.facades

import ar2.db.entities.User
import ar2.web.PageRequest

interface UsersFacade {
    fun new(form: User, password: String, issuer: User): User
    fun find(username: String): User
    fun find(id: Int): User
    fun find(pr: PageRequest): List<User>

    fun update(id: Int, form: User, password: String, issuer: User)

    fun disable(userId: Int, issuer: User)
    fun disable(username: String, issuer: User)
    fun enable(username: String, issuer: User)
}
