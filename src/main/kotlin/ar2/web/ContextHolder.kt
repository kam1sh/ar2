package ar2.web

import ar2.users.User
import org.http4k.core.*
import org.http4k.core.cookie.cookie
import org.http4k.lens.RequestContextKey
import org.slf4j.LoggerFactory

val context = RequestContexts()
val userKey = RequestContextKey.required<User>(context)

class ContextHolder(val request: Request) {
    var currentUser: User
        get() = userKey[request]
        set(value) {userKey[request] = value }
}

class LookupSessionTokenFilter {
    private val log = LoggerFactory.getLogger(LookupSessionTokenFilter::class.java)
    operator fun invoke() = Filter {next ->
        {req ->
            val token = req.cookie("AR2SESSION")
            if (token != null) {
                log.info("Token value: {}", token)
            }
            next(req)
        }
    }
}
