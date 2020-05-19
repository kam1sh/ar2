package ar2.exceptions

import ar2.db.entities.User
import org.http4k.core.Status

class UserExistsException(val user: User) : Ar2Exception("User exists.", "USER_EXISTS") {
    override fun toHTTPResponse() =
        WebError(Status.CONFLICT, message!!, codeText!!).toHTTPResponse()
}
