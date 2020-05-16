package ar2.exceptions

import org.http4k.core.*
import org.http4k.format.Jackson.auto

open class WebError : Ar2Exception {
    private var status: Status
    private var msg: String
    private var codeText: String

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

    constructor(err: WebError, status: Status) : super(err.msg, hideStackTrace = true) {
        this.status = status
        msg = err.msg
        codeText = err.codeText
    }

    data class Payload(val message: String, val codeText: String)
    val payloadLens = Body.auto<Payload>().toLens()

    override fun toHTTPResponse(): Response {
        val payload = Payload(msg, codeText)
        return payloadLens(payload, Response(status))
    }
}
