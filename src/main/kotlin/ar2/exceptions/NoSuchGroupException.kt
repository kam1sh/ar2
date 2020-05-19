package ar2.exceptions

import org.http4k.core.Status

class NoSuchGroupException : Ar2Exception("No such group.", "GROUP_NOT_FOUND") {
    override val httpStatus = Status.NOT_FOUND
}
