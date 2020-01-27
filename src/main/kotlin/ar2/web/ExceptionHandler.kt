package ar2.web

import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.slf4j.LoggerFactory
import java.lang.Exception

object ExceptionHandler {
    private val log = LoggerFactory.getLogger(ExceptionHandler::class.java)

    operator fun invoke() = Filter { next ->
        { req: Request ->
            try {
                next(req)
            } catch (responseExc: WebResult) {
                responseExc.toResponse()
            } catch (exc: Exception) {
                log.error("Caught exception:", exc)
                Response(Status.INTERNAL_SERVER_ERROR)
            }
        }
    }
}