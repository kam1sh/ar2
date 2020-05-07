package ar2.web

import java.lang.Exception
import org.http4k.core.Response

abstract class WebResult : Exception(null, null, true, false) {
    open fun message(): String = "No WebResult message provided."
    abstract fun toResponse(): Response
}
