package playground.security

import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status

class BasicAuthFilter {
    operator fun invoke() = Filter { next ->
        {
            Response(Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic Realm")
        }
    }
}