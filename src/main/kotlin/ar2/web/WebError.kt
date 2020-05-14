package ar2.web

import org.http4k.core.*
import org.http4k.format.Jackson.auto

open class WebError(val status: Status, private val msg: String? = null) : WebResult() {

    data class Payload(val message: String, val exception: String)
    val payloadLens = Body.auto<Payload>().toLens()

    override fun toResponse(): Response {
        val payload = Payload(message(), javaClass.canonicalName)
        return payloadLens(payload, Response(status))
    }

    override fun message() = msg ?: ""
}

class BadRequest(override val message: String) : WebError(Status.BAD_REQUEST, message)
