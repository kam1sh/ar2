package ar2.web

import ar2.exceptions.Ar2Exception
import java.lang.Exception
import org.http4k.core.*
import org.http4k.format.Jackson.auto
import org.http4k.lens.LensFailure
import org.slf4j.LoggerFactory

object ExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    data class BadRequestBody(val message: String)
    val BRLens = Body.auto<BadRequestBody>().toLens()

    operator fun invoke() = Filter { next ->
        { req: Request ->
            try {
                next(req)
            } catch (exc: Ar2Exception) {
                log.trace("Returning WebResult, message - {}", exc.message)
                exc.toHTTPResponse()
            } catch (exc: LensFailure) {
                log.debug("Error parsing request body: '{}'", exc.message)
                log.trace("Exception info: ", exc)
                if (log.isTraceEnabled) log.trace("Body: {}", req.bodyString())
                BRLens(BadRequestBody("Invalid body provided"), Response(Status.BAD_REQUEST))
            } catch (exc: Exception) {
                log.error("Caught exception:", exc)
                Response(Status.INTERNAL_SERVER_ERROR)
            }
        }
    }
}
