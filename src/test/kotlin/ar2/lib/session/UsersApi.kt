package ar2.lib.session

import ar2.db.User
import com.fasterxml.jackson.core.type.TypeReference
import org.http4k.core.Method

class UsersApi(val session: Session) {
    fun iter(): Iterator<User> {
        return Paginator(::list)
    }

    fun list(offset: Int = 0, limit: Int = 10): List<User> {
        val req = session.prepareRequest(Method.GET, "/users")
            .query("offset", offset.toString())
            .query("limit", limit.toString())
        return session.request(req).deserialize(object : TypeReference<List<User>>() {})
    }

    fun find(username: String): User? {
        return iter().asSequence().find { it.username == username }
    }
}
