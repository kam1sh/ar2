package ar2.exceptions

import org.http4k.core.Status

class GroupRoleExistsException : WebError {
    constructor() : super(Status.CONFLICT, "Role already attached", "ROLE_ALREADY_ATTACHED")
    constructor(status: Status) : super(status, "Role already attached", "ROLE_ALREADY_ATTACHED")
}
