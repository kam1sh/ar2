package ar2.exceptions

import org.http4k.core.Status

class NoPermissionException(msg: String) : Ar2Exception(msg, codeText = "NO_PERMISSION") {
    override val httpStatus = Status.FORBIDDEN
}
