package ar2.web.views

import ar2.Config
import ar2.db.entities.Session
import ar2.db.entities.User
import ar2.exceptions.WebError
import ar2.services.SecurityService
import ar2.services.SessionsService
import ar2.services.UsersService
import ar2.web.*
import java.lang.NumberFormatException
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom
import org.http4k.base64Encode
import org.http4k.core.*
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.koin.core.KoinComponent
import org.koin.core.inject

class UserViews(
    private val service: UsersService,
    private val securityService: SecurityService
) : KoinComponent {

    private val cfg: Config by inject()
    private val sessionsService: SessionsService by inject()

    fun views() = routes(
            "/" bind Method.GET to ::listUsers,
            "/" bind Method.POST to ::newUser,
            "/current" bind Method.GET to ::currentUser,
            "/id/{id}" bind Method.GET to ::findUserById,
            "/id/{id}" bind Method.DELETE to ::removeUserById,
            "/username/{name}" bind Method.GET to ::findUserByUsername,
            "/username/{name}" bind Method.DELETE to ::removeUserByName
        )

    private fun findUserByUsername(request: Request): Response {
        request.checkApiAcceptHeader()
        val username = request.path("name")!!
        val user = service.find(username).orNotFound()
        return userResponseLens(user, Response(Status.OK))
    }

    private fun findUserById(request: Request): Response {
        request.checkApiAcceptHeader()
        val id = request.path("id")!!.toIntOrNull() ?: throw WebError(Status.BAD_REQUEST, "Invalid ID.")
        val user = service.find(id).orNotFound()
        return userResponseLens(user, Response(Status.OK))
    }

    data class AuthRequest(val username: String, val password: String)
    data class AuthResponse(val message: String)
    val authLens = Body.auto<AuthRequest>().toLens()
    val authResponseLens = Body.auto<AuthResponse>().toLens()

    fun authenticate(request: Request): Response {
        request.checkApiAcceptHeader()
        request.checkApiCTHeader()
        val form = authLens(request)
        if (securityService.authenticate(Credentials(form.username, form.password)) == null)
            throw WebError(Status.BAD_REQUEST,"Invalid username or password.", "INVALID_USERNAME_OR_PASSWORD")
        val user = service.find(form.username)
        user.lastLogin = LocalDateTime.now()
        service.update(user)
        val byteArr = ByteArray(10)
        ThreadLocalRandom.current().nextBytes(byteArr)
        val cookieValue = String(byteArr).base64Encode()
        val expires = LocalDateTime.now().plusDays(cfg.security.sessionLifetimeDays.toLong())
        sessionsService.new(Session(cookieValue, user, expires))
        val cookie = Cookie(cfg.security.cookieName, cookieValue, expires = expires)
        return authResponseLens(
            AuthResponse("Successfully authenticated as $user."),
            Response(Status.OK).cookie(cookie)
        )
    }

    data class NewUserRequest(val user: User, val password: String)
    val userLens = Body.auto<NewUserRequest>().toLens()

    private fun newUser(request: Request): Response {
        request.checkApiAcceptHeader()
        request.checkApiCTHeader()
        request.currentUser!!.assertAdmin()
        val form = userLens(request)
        form.user.passwordHash = securityService.encode(form.password)
        val user = try {
            service.new(form.user, form.password)
        } catch (e: Exception) {
            return Response(Status.CONFLICT).body("This user already exists.")
        }
        return Response(Status.CREATED).header("Location", "/users/id/${user.id}")
    }

    val userResponseLens = Body.auto<User>().toLens()
    private fun currentUser(request: Request): Response {
        val body = request.currentUser!!
        return userResponseLens(body, Response(Status.OK))
    }

    val listUsersLens = Body.auto<List<User>>().toLens()
    private fun listUsers(request: Request): Response {
        request.checkApiAcceptHeader()
        val pr = request.toPageRequest()
        val users = service.list(pr)
        return listUsersLens(users, Response(Status.OK))
    }

    private fun removeUserById(request: Request): Response {
        request.currentUser!!.assertAdmin()
        val id = try {
            request.path("id")!!.toInt()
        } catch (e: NumberFormatException) {
            return Response(Status.BAD_REQUEST)
        }
        val user = service.find(id).orNotFound()
        if (request.currentUser!!.id == user.id) throw WebError(Status.BAD_REQUEST, "You cannot remove yourself =/", "CANNOT_REMOVE_YOURSELF")
        service.remove(id)
        return Response(Status.NO_CONTENT)
    }

    private fun removeUserByName(request: Request): Response {
        request.currentUser!!.assertAdmin()
        val name = request.path("name")!!
        val user = service.find(name)
        if (request.currentUser!!.id == user.id) throw WebError(Status.BAD_REQUEST, "You cannot remove yourself =/", "CANNOT_REMOVE_YOURSELF")
        service.remove(user.id!!)
        return Response(Status.NO_CONTENT)
    }
}

fun User?.orNotFound(): User = this ?: throw WebError(
    Status.NOT_FOUND,
    "User not found."
)

fun User.assertAdmin(msg: String? = null) {
    if (!isAdmin) throw WebError(
        Status.FORBIDDEN,
        msg ?: "You don't have permission to do this."
    )
}
