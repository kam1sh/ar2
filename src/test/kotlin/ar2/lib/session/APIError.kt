package ar2.lib.session

import java.lang.StringBuilder
import org.http4k.core.ContentType
import org.http4k.core.Response

val printableContentTypes = setOf("text/plain", ContentType.APPLICATION_JSON.toHeaderValue())
class APIError(val resp: Response) : Exception() {
    val _message: String

    init {
        val sb = StringBuilder()
        sb.append("HTTP ${resp.status}")
        if (resp.header("Content-Type") in printableContentTypes) {
            sb.append(": ").append(resp.bodyString())
        }
        _message = sb.toString()
    }

    override val message: String?
        get() = _message
}
