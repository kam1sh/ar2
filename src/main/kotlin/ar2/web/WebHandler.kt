package ar2.web

import ar2.App
import ar2.services.SecurityService
import ar2.services.SessionsService
import ar2.services.contexts
import ar2.web.views.AuthenticationViews
import ar2.web.views.GroupViews
import ar2.web.views.PyPIViews
import ar2.web.views.UserViews
import org.http4k.core.*
import org.http4k.filter.GzipCompressionMode
import org.http4k.filter.ServerFilters
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.koin.core.KoinComponent
import org.koin.core.inject

class WebHandler : KoinComponent, HttpHandler {

    private val authenticationViews: AuthenticationViews by inject()
    private val userViews: UserViews by inject()
    private val groupViews: GroupViews by inject()
    private val pyPIViews: PyPIViews by inject()
    private val securityService: SecurityService by inject()
    private val sessionsService: SessionsService by inject()

    val handler: HttpHandler

    constructor() {
        handler = toHttpHandler(null)
    }

    constructor(handler: RoutingHttpHandler? = null) {
        this.handler = toHttpHandler(handler)
    }

    fun toHttpHandler(routes: RoutingHttpHandler?): HttpHandler = ServerFilters.GZip(compressionMode = GzipCompressionMode.Streaming)
            .then(ExceptionHandler())
            .then(ServerFilters.InitialiseRequestContext(contexts))
            .then(LookupSessionFilter())
            .then(routes ?: apiRoutes())

    private fun apiRoutes() = routes("/api" bind routes(
        "/v1/login" bind Method.POST to authenticationViews::authenticateByCredentials,
        "/v1/users" bind securityService.requireSession().then(userViews.views()),
        "/v1/groups" bind securityService.requireSession().then(groupViews.views()),
        "/py/{group}/{repo}" bind pyPIViews.views()
    ))

    override fun invoke(p1: Request) = handler(p1)
}