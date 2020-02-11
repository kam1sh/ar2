package ar2.web

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
            } catch (responseExc: WebResult) {
                responseExc.toResponse()
            } catch (exc: LensFailure) {
                log.debug("Error parsing request body: '{}'", exc.message)
                BRLens(BadRequestBody("Invalid body provided"), Response(Status.BAD_REQUEST))
            } catch (exc: Exception) {
                log.error("Caught exception:", exc)
                Response(Status.INTERNAL_SERVER_ERROR)
            }
        }
    }
}
