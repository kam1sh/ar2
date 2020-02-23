package ar2.web

import java.lang.Exception
import org.http4k.core.Response

abstract class WebResult : Exception(null, null, true, false) {
    abstract fun toResponse(): Response
}
