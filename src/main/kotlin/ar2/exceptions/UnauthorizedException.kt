package ar2.exceptions

import org.http4k.core.Status

class UnauthorizedException : Ar2Exception("Not authorised.", "UNAUTHORISED") {
    override val httpStatus = Status.UNAUTHORIZED
}
