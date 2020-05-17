package ar2.exceptions

import org.http4k.core.Response
import org.http4k.core.Status

class NoSuchGroupException : Ar2Exception("No such group.") {
    override fun toHTTPResponse() =
        WebError(Status.NOT_FOUND, message!!, "GROUP_NOT_FOUND").toHTTPResponse()
}
