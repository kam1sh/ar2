package ar2.web.views

import ar2.Config
import ar2.db.Sessions
import ar2.db.Users
import ar2.db.toUser
import ar2.security.SecurityService
import ar2.users.BaseUser
import ar2.users.User
import ar2.users.UsersService
import ar2.web.BadRequest
import ar2.web.WebError
import ar2.web.currentUser
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import org.http4k.base64Encode
import org.http4k.core.*
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.format.Jackson.auto
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("ar2.web.views")

class UserViews(
    val usersService: UsersService,
    val securityService: SecurityService
) : KoinComponent {

    val cfg: Config by inject()

    fun views(): RoutingHttpHandler {
        return routes(
                "/" bind Method.GET to ::listUsers,
                "/" bind Method.POST to ::newUser,
                "/_current" bind Method.GET to ::currentUser,
                "/{id}" bind Method.DELETE to ::deleteUser
        )
    }

    data class AuthRequest(val username: String, val password: String)
    data class AuthResponse(val success: Boolean, val message: String)
    val authLens = Body.auto<AuthRequest>().toLens()
    val authResponseLens = Body.auto<AuthResponse>().toLens()

    fun authenticate(request: Request): Response {
        request.checkApiAcceptHeader()
        request.checkApiCTHeader()
        val form = authLens(request)
        if (securityService.authenticate(Credentials(form.username, form.password)) == null)
            throw BadRequest("Invalid username or password.")
        val user = usersService.findByUsername(form.username)!!
        val byteArr = ByteArray(10)
        securityService.secureRandom.nextBytes(byteArr)
        val cookieValue = String(byteArr).base64Encode()
        val expires = DateTime.now().plusDays(cfg.security.sessionLifetimeDays)
        Sessions.new(cookieValue, user = user, expires = expires)
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(expires.millis), TimeZone.getDefault().toZoneId())
        val cookie = Cookie("AR2SESSION", cookieValue, expires = dt)
        return authResponseLens(AuthResponse(true, "Successfully authenticated as $user."), Response(Status.OK).cookie(cookie))
    }

    data class NewUserRequest(val user: BaseUser, val password: String)
    val userLens = Body.auto<NewUserRequest>().toLens()

    private fun newUser(request: Request): Response {
        request.checkApiAcceptHeader()
        request.checkApiCTHeader()
        val form = userLens(request)
        if (form.user.admin && !request.currentUser!!.admin) {
            throw BadRequest("You don't have permission to create administrators.")
        }
        try {
            Users.new(
                    form.user.username,
                    passwordHash = securityService.encode(form.password),
                    name = form.user.name,
                    email = form.user.email,
                    admin = form.user.admin
            )
        } catch (e: ExposedSQLException) {
            return Response(Status.CONFLICT).body("This user already exists.")
        }
        return Response(Status.CREATED)
    }

    val userResponseLens = Body.auto<User>().toLens()
    private fun currentUser(request: Request): Response {
        val body = request.currentUser!!
        return userResponseLens(body, Response(Status.OK))
    }

    val listUsersLens = Body.auto<List<User>>().toLens()
    private fun listUsers(request: Request): Response {
        request.checkApiAcceptHeader()
        val limit = request.query("limit") ?: "10"
        val offset = request.query("offset") ?: "0"
        return listUsersLens(Users.findAll(limit = limit.toInt(), offset = offset.toInt()), Response(Status.OK))
    }

    private fun deleteUser(request: Request): Response {
        if(!request.currentUser!!.admin) throw WebError(Status.FORBIDDEN, "You don't have permission to delete users.")
        val id = request.path("id")!!.toInt()
        transaction {
            Users.select { Users.id eq id }.singleOrNull() ?: throw BadRequest("User with ID $id does not exist.")
            Users.deleteWhere { Users.id eq id }
        }
        return Response(Status.NO_CONTENT)
    }
}

fun Request.checkApiAcceptHeader() {
    val header = header("Accept") ?: ""
    log.trace("'Accept' header value: {}", header)
    if (!(header.contains("application/json") || header.contains("*/*"))) {
        throw WebError(Status.NOT_ACCEPTABLE, "Client does not accepts application/json, can't process request.")
    }
}

fun Request.checkApiCTHeader() {
    val header = header("Content-Type") ?: ""
    log.trace("'Content-Type' header value: {}", header)
    if (!header.contains("application/json; charset=UTF-8"))
        throw WebError(Status.NOT_ACCEPTABLE, "Client body is not application/json with UTF-8 charset, can't process request")
}
