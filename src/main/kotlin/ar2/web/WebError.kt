package ar2.web

import org.http4k.core.*
import org.http4k.format.Jackson.auto

open class WebError(val status: Status, val msg: String) : WebResult() {

    data class Payload(val message: String, val exception: String)
    val payloadLens = Body.auto<Payload>().toLens()

    override fun toResponse(): Response {
        val payload = Payload(msg, this.javaClass.canonicalName)
        val response = payloadLens(payload, Response(status))
        return response
    }
}
