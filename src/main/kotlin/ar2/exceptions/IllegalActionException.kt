package ar2.exceptions

import org.http4k.core.Status

class IllegalActionException(val msg: String, val code: String) : Ar2Exception(msg, hideStackTrace = true) {
    override fun toHTTPResponse() = WebError(Status.BAD_REQUEST, msg, code).toHTTPResponse()
}