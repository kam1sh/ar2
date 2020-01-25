package ar2.web

import ar2.web.views.PackageExists
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.slf4j.LoggerFactory
import java.lang.Exception

class ExceptionHandler {
    val log = LoggerFactory.getLogger(ExceptionHandler::class.java)

    operator fun invoke() = Filter { next ->
        { req: Request ->
            try {
                next(req)
            } catch (exists: PackageExists) {
                Response(Status.CONFLICT).body("Package already uploaded.")
            } catch (exc: Exception) {
                log.error("Caught exception:", exc)
                Response(Status.INTERNAL_SERVER_ERROR)
            }
        }
    }
}