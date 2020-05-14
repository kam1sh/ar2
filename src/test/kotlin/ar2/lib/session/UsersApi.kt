package ar2.lib.session

import ar2.db.entities.User
import ar2.web.views.UserViews
import com.fasterxml.jackson.core.type.TypeReference
import org.http4k.core.Method
import org.http4k.core.Response

class UsersApi(val session: Session) {

    fun new(user: User, password: String): Response =
        session.request(Method.POST, "/api/v1/users/", UserViews.NewUserRequest(
            user, password
        ))

    fun iter(): Iterator<User> = Paginator(::list)

    fun list(offset: Int = 0, limit: Int = 10): List<User> {
        val req = session.prepareRequest(Method.GET, "/api/v1/users")
            .query("offset", offset.toString())
            .query("limit", limit.toString())
        return session.request(req).deserialize(object : TypeReference<List<User>>() {})
    }

    fun find(username: String): User = session.request(Method.GET, "/api/v1/users/username/$username")
        .deserialize(User::class.java)

    fun find(id: Int): User = session.request(Method.GET, "/api/v1/users/id/$id")
        .deserialize(User::class.java)

    fun current(): User = session.request(Method.GET, "/api/v1/users/current")
        .deserialize(User::class.java)
}
