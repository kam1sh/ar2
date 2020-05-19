package ar2.exceptions

import org.http4k.core.Status

class IllegalActionException(val msg: String, val code: String) : Ar2Exception(msg, hideStackTrace = true) {
    override val httpStatus = Status.BAD_REQUEST
}
