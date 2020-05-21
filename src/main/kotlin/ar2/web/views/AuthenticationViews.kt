package ar2.web.views

import ar2.Config
import ar2.exceptions.WebError
import ar2.services.SecurityService
import ar2.services.SessionsService
import ar2.services.UsersService
import ar2.web.checkApiAcceptHeader
import ar2.web.checkApiCTHeader
import java.time.LocalDateTime
import org.http4k.core.*
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.format.Jackson.auto
import org.koin.core.KoinComponent
import org.koin.core.inject

class AuthenticationViews(
    private val usersService: UsersService,
    private val securityService: SecurityService
) : KoinComponent {

    private val cfg: Config by inject()
    private val sessionsService: SessionsService by inject()

    data class AuthRequest(val username: String, val password: String)
    data class AuthResponse(val message: String)
    val authLens = Body.auto<AuthRequest>().toLens()
    val authResponseLens = Body.auto<AuthResponse>().toLens()

    fun authenticateByCredentials(request: Request): Response {
        request.checkApiAcceptHeader()
        request.checkApiCTHeader()
        val form = authLens(request)
        if (securityService.authenticate(Credentials(form.username, form.password)) == null)
            throw WebError(Status.BAD_REQUEST, "Invalid username or password.", "INVALID_USERNAME_OR_PASSWORD")
        val user = usersService.find(form.username)
        user.lastLogin = LocalDateTime.now()
        usersService.update(user)
        val session = sessionsService.new(user)
        val cookie = Cookie(cfg.security.cookieName, session.key, expires = session.expires)
        return authResponseLens(
            AuthResponse("Successfully authenticated as $user."),
            Response(Status.OK).cookie(cookie)
        )
    }
}
