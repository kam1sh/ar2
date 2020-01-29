package ar2.web

import ar2.db.Sessions
import ar2.users.User
import org.http4k.core.*
import org.http4k.core.cookie.cookie
import org.http4k.lens.RequestContextKey
import org.http4k.lens.RequestContextLens
import org.koin.core.KoinComponent
import org.slf4j.LoggerFactory

val context = RequestContexts()
val userKey = RequestContextKey.optional<User>(context)

class LookupSessionTokenFilter : KoinComponent {
    private val log = LoggerFactory.getLogger(javaClass)

    operator fun invoke() = Filter {next -> {request -> doLookup(next, request) } }

    private fun doLookup(next: HttpHandler, request: Request): Response {
        val token = request.cookie("AR2SESSION")
        if (token != null) {
            val user = Sessions.findUser(token.value)
            request.currentUser = user
        }
        return next(request)
    }
}

var Request.currentUser: User?
    get() = userKey(this)
    set(value) {userKey[this] = value}