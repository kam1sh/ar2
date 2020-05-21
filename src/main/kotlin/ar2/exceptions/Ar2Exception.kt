package ar2.exceptions

import java.lang.Exception
import org.http4k.core.Body
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.auto

abstract class Ar2Exception : Exception {
    open val httpStatus: Status = Status.BAD_REQUEST
    var codeText: String? = null

    constructor(msg: String, codeText: String) : super(msg) {
        this.codeText = codeText
    }
    constructor(msg: String, hideStackTrace: Boolean) : super(msg, null, true, !hideStackTrace)

    open fun toHTTPResponse() = errorResponse()

    data class WebErrorPayload(val message: String, val codeText: String)
    val webErrorPayloadLens = Body.auto<WebErrorPayload>().toLens()
    protected fun errorResponse(): Response {
        val payload = WebErrorPayload(message!!, codeText!!)
        val response = webErrorPayloadLens(payload, Response(httpStatus))
        return response
    }
}
