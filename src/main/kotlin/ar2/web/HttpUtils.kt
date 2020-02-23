package ar2.web

import ar2.web.views.log
import org.http4k.core.Request
import org.http4k.core.Status

fun Request.checkApiAcceptHeader() {
    val header = header("Accept") ?: ""
    log.trace("'Accept' header value: {}", header)
    if (!(header.contains("application/json") || header.contains("*/*"))) {
        throw WebError(Status.NOT_ACCEPTABLE, "Client does not accepts application/json, can't process request.")
    }
}

fun Request.checkApiCTHeader() {
    val header = header("Content-Type") ?: ""
    log.trace("'Content-Type' header value: {}", header)
    if (!header.contains("application/json; charset=UTF-8"))
        throw WebError(Status.NOT_ACCEPTABLE, "Client body is not application/json with UTF-8 charset, can't process request")
}
