package ar2.exceptions

import org.http4k.core.Status

class IllegalActionException(msg: String, code: String) : Ar2Exception(msg, codeText = code) {
    override val httpStatus = Status.BAD_REQUEST
}
