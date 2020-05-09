package ar2.web

import org.http4k.core.Request
import org.http4k.core.Status
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("ar2.web.HttpUtils")

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
    if (!header.toLowerCase().contains("application/json; charset=utf-8"))
        throw WebError(Status.NOT_ACCEPTABLE, "Client body is not application/json with UTF-8 charset, can't process request")
}
