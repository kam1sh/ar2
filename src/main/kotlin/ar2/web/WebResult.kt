package ar2.web

import org.http4k.core.Response
import java.lang.Exception

abstract class WebResult: Exception() {
    abstract fun toResponse(): Response
}