package ar2.exceptions

import org.http4k.core.Status

class NoSuchGroupException : WebError(Status.NOT_FOUND, "No such group.", "GROUP_NOT_FOUND")
