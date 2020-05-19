package ar2.exceptions

import org.http4k.core.*
import org.http4k.format.Jackson.auto

class WebError : Ar2Exception {
    private var status: Status
    private var msg: String

    constructor(status: Status, msg: String, codeText: String) : super(msg, hideStackTrace = true) {
        this.status = status
        this.msg = msg
        this.codeText = codeText
    }

    constructor(status: Status, msg: String) : super(msg, hideStackTrace = true) {
        this.status = status
        this.msg = msg
        this.codeText = "NO_CODE_TEXT"
    }

    constructor(exc: Ar2Exception, status: Status) : super(exc.message ?: throw IllegalArgumentException(exc), hideStackTrace = true) {
        this.status = status
        msg = exc.message!!
        codeText = exc.codeText
    }

    data class WebErrorPayload(val message: String, val codeText: String)
    val webErrorPayloadLens = Body.auto<WebErrorPayload>().toLens()

    override fun toHTTPResponse(): Response {
        val payload = WebErrorPayload(msg, codeText!!)
        return webErrorPayloadLens(payload, Response(status))
    }
}
