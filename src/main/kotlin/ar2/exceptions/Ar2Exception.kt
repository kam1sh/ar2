package ar2.exceptions

import java.lang.Exception
import org.http4k.core.Response

abstract class Ar2Exception : Exception {
    constructor(msg: String) : super(msg)
    constructor(msg: String, hideStackTrace: Boolean) : super(msg, null, true, !hideStackTrace)

    abstract fun toHTTPResponse(): Response
}
