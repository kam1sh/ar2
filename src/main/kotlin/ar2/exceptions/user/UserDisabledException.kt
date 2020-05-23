package ar2.exceptions.user

import ar2.exceptions.Ar2Exception
import org.http4k.core.Status

class UserDisabledException : Ar2Exception("User disabled.", "USER_DISABLED") {
    override val httpStatus = Status.BAD_REQUEST
}