package ar2.web.views

import ar2.Config
import ar2.db.Session
import ar2.db.User
import ar2.services.SecurityService
import ar2.services.SessionsService
import ar2.services.UsersService
import ar2.web.*
import org.hibernate.SessionFactory
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
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

class UserViews(
    private val usersService: UsersService,
    private val securityService: SecurityService
) : KoinComponent {
    private val log = LoggerFactory.getLogger(UserViews::class.java)

    private val cfg: Config by inject()
    private val sessionsService: SessionsService by inject()
    private val factory: SessionFactory by inject()

    fun views() = routes(
            "/" bind Method.GET to ::listUsers,
            "/" bind Method.POST to ::newUser,
            "/current" bind Method.GET to ::currentUser,
            "/id/{id}" bind Method.DELETE to ::removeUser,
            "/username/{name}" bind Method.GET to ::findUserByUsername
        )

    private fun findUserByUsername(request: Request): Response {
        request.checkApiAcceptHeader()
        val username = request.path("name")!!
        val user = usersService.findByUsername(username)!!
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
            throw BadRequest("Invalid username or password.")
        val user = usersService.findByUsername(form.username)!!
        user.lastLogin = LocalDateTime.now()
        usersService.save(user)
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
        val form = userLens(request)
        if (form.user.isAdmin && !request.currentUser!!.isAdmin) {
            throw BadRequest("You don't have permission to create administrators.")
        }
        form.user.passwordHash = securityService.encode(form.password)
        val user = try {
            usersService.newUser(form.user, form.password)
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
    @Suppress("UNCHECKED_CAST")
    private fun listUsers(request: Request): Response {
        request.checkApiAcceptHeader()
        val limit = request.query("limit") ?: "10"
        val offset = request.query("offset") ?: "0"
        val users: List<User> = factory.openSession().use {
            it.createQuery("From User")
                .setFirstResult(offset.toInt())
                .setMaxResults(limit.toInt())
                .list() as List<User>
        }
        return listUsersLens(users, Response(Status.OK))
    }

    private fun removeUser(request: Request): Response {
        if (!request.currentUser!!.isAdmin) throw WebError(Status.FORBIDDEN, "You don't have permission to delete users.")
        val id = request.path("id")!!.toInt()
        factory.openSession().use {
            val user = it.find(User::class.java, id)
            if (request.currentUser!!.id == user.id) throw BadRequest("You cannot remove yourself =/")
            val tr = it.beginTransaction()
            it.createQuery("delete User where id = :id")
                .setParameter("id", id)
                .executeUpdate()
            tr.commit()
        }
        return Response(Status.NO_CONTENT)
    }
}
