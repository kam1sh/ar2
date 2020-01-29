package ar2.web.views

import ar2.db.Sessions
import ar2.security.SecurityService
import ar2.users.User
import ar2.users.UsersService
import ar2.web.currentUser
import org.http4k.base64Encode
import org.http4k.core.*
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.format.Jackson.auto
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.joda.time.DateTime
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

class UserViews(
        val usersService: UsersService,
        val securityService: SecurityService
) {
    fun views(): RoutingHttpHandler {
        return routes(
                "/" bind Method.POST to ::newUser,
                "/current" bind Method.GET to ::currentUser
        )
    }

    data class AuthRequest(val username: String, val password: String)
    data class AuthResponse(val success: Boolean, val message: String)
    val authLens = Body.auto<AuthRequest>().toLens()
    val authResponseLens = Body.auto<AuthResponse>().toLens()

    fun authenticate(request: Request): Response {
        val form = authLens(request)
        if (securityService.authenticate(Credentials(form.username, form.password)) == null)
            return authResponseLens(AuthResponse(false, "Invalid username or password."), Response(Status.BAD_REQUEST))
        val user = usersService.findByUsername(form.username)!!
        val byteArr = ByteArray(10)
        securityService.secureRandom.nextBytes(byteArr)
        val cookieValue = String(byteArr).base64Encode()
        val expires = DateTime.now().plusDays(2)
        Sessions.new(cookieValue, user = user, expires = expires)
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(expires.millis), TimeZone.getDefault().toZoneId())
        val cookie = Cookie("AR2SESSION", cookieValue, expires = dt)
        return authResponseLens(AuthResponse(true, "Successfully authenticated as $user."), Response(Status.OK).cookie(cookie))
    }

    fun newUser(request: Request): Response {
        return Response(Status.CREATED)
    }


    val userResponseLens = Body.auto<User>().toLens()
    fun currentUser(request: Request): Response {
        val body = request.currentUser!!
        return userResponseLens(body, Response(Status.OK))
    }
}