package ar2.exceptions.user

import ar2.exceptions.Ar2Exception
import org.http4k.core.Status

class NoSuchUserException : Ar2Exception("No such user.", "USER_NOT_FOUND") {
    override val httpStatus = Status.NOT_FOUND
}
