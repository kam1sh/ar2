package ar2.exceptions

import org.http4k.core.Status

class NoSuchUserException : Ar2Exception("No such user.", "USER_NOT_FOUND") {
    override fun toHTTPResponse() =
        WebError(Status.NOT_FOUND, message!!, codeText!!).toHTTPResponse()
}
