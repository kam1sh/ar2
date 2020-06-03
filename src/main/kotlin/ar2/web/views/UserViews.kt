package ar2.web.views

import ar2.db.entities.User
import ar2.exceptions.WebError
import ar2.exceptions.user.NoSuchUserException
import ar2.facades.UsersFacade
import ar2.services.extractUser
import ar2.web.*
import org.http4k.core.*
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.koin.core.KoinComponent

class UserViews(
    private val facade: UsersFacade
) : KoinComponent {

    fun views() = routes(
            "/" bind Method.GET to ::listUsers,
            "/" bind Method.POST to ::newUser,
            "/current" bind Method.GET to ::currentUser,
            "/id/{id}" bind Method.GET to ::findUserById,
            "/id/{id}" bind Method.PUT to ::updateUserById,
            "/id/{id}/disable" bind Method.POST to ::disableUserById,
            "/username/{name}" bind Method.GET to ::findUserByUsername,
            "/username/{name}/disable" bind Method.POST to ::disableUserByName
        )

    val userLens = Body.auto<User>().toLens()
    val listUsersLens = Body.auto<List<User>>().toLens()

    private fun findUserByUsername(request: Request): Response {
        request.checkApiAcceptHeader()
        val username = request.path("name")!!
        val user = facade.find(username).orNotFound()
        return userLens(user, Response(Status.OK))
    }

    private fun findUserById(request: Request): Response {
        request.checkApiAcceptHeader()
        val id = request.pathIntOrThrow("id")
        val user = facade.find(id).orNotFound()
        return userLens(user, Response(Status.OK))
    }

    data class NewUserRequest(val user: User, val password: String)
    val newUserLens = Body.auto<NewUserRequest>().toLens()

    private fun newUser(request: Request): Response {
        request.checkApiAcceptHeader()
        request.checkApiCTHeader()
        val form = newUserLens(request)
        val user = facade.new(form.user, form.password, extractUser(request))
        return Response(Status.CREATED).header("Location", "/users/id/${user.id}")
    }

    private fun currentUser(request: Request): Response {
        val body = extractUser(request)
        return userLens(body, Response(Status.OK))
    }

    private fun listUsers(request: Request): Response {
        request.checkApiAcceptHeader()
        val pr = request.toPageRequest()
        val users = facade.find(pr, extractUser(request))
        return listUsersLens(users, Response(Status.OK))
    }

    private fun disableUserById(request: Request): Response {
        val id = request.pathIntOrThrow("id")
        facade.disable(id, extractUser(request))
        return Response(Status.NO_CONTENT)
    }

    private fun disableUserByName(request: Request): Response {
        val name = request.path("name")!!
        facade.disable(name, extractUser(request))
        return Response(Status.NO_CONTENT)
    }

    private fun updateUserById(request: Request): Response {
        val id = request.pathIntOrThrow("id")
        val form = newUserLens(request)
        facade.update(id, form.user, form.password, extractUser(request))
        return Response(Status.NO_CONTENT)
    }
}

fun User?.orNotFound(): User = this ?: throw NoSuchUserException()

fun Request.pathIntOrThrow(name: String): Int {
    return path(name)!!.toIntOrNull() ?: throw WebError(Status.BAD_REQUEST, "Invalid ID.", "INVALID_ID")
}
