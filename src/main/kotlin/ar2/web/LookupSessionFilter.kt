package ar2.web

import ar2.services.SessionsService
import org.http4k.core.*
import org.koin.core.KoinComponent
import org.koin.core.inject

object LookupSessionFilter : KoinComponent {
    private val sessionsService: SessionsService by inject()

    operator fun invoke() = Filter { next -> { request -> doLookup(next, request) } }

    private fun doLookup(next: HttpHandler, request: Request): Response {
        sessionsService.attachUser(request)
        return next(request)
    }
}
