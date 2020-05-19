package ar2.exceptions

import org.http4k.core.Status

class GroupRoleExistsException : Ar2Exception("Role already attached", "ROLE_ALREADY_ATTACHED") {
    override val httpStatus = Status.CONFLICT
}
