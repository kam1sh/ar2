package ar2.exceptions

import org.http4k.core.Status

class NoSuchGroupException : Ar2Exception("No such group.", "GROUP_NOT_FOUND") {
    override fun toHTTPResponse() =
        WebError(Status.NOT_FOUND, message!!, codeText!!).toHTTPResponse()
}
