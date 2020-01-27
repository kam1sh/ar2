package ar2.web.views

import ar2.security.SecurityService
import ar2.security.SecurityServiceImpl
import ar2.users.UsersService
import org.http4k.core.*
import org.http4k.core.cookie.Cookie
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.time.LocalDateTime

class UserViews(
        val usersService: UsersService,
        val securityService: SecurityService
) {
    fun views(): HttpHandler {
        return routes(
            "/" bind Method.POST to ::newUser
        )
    }

    data class AuthRequest(val username: String, val password: String)
    data class AuthResponse(val success: Boolean, val message: String)
    val authLens = Body.auto<AuthRequest>().toLens()
    val authResponse = Body.auto<AuthResponse>().toLens()

    fun authorize(request: Request): Response {
        val form = authLens(request)
        if (securityService.authenticate(Credentials(form.username, form.password)) == null)
            return authResponse(AuthResponse(false, "Invalid username or password."), Response(Status.BAD_REQUEST))
        val user = usersService.findByUsername(form.username)
        val byteArr = ByteArray(40)
        val cookieValue = (securityService as SecurityServiceImpl).secureRandom.nextBytes(byteArr)

        val cookie = Cookie("AR2SESSION", "123", expires = LocalDateTime.now().plusHours(2))
        return authResponse(AuthResponse(true, "Successfully authorized as $user."), Response(Status.OK))
    }

    fun newUser(request: Request): Response {
        return Response(Status.CREATED)
    }
}