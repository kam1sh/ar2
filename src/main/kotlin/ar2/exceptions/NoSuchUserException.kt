package ar2.exceptions

import org.http4k.core.Status

class NoSuchUserException : WebError {
    constructor() : super(Status.NOT_FOUND, "User not found.", "USER_NOT_FOUND")
    constructor(status: Status) : super(status, "Uset not found.", "USER_NOT_FOUND")
}
