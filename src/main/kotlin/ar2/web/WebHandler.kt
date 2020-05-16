package ar2.web

import ar2.App
import ar2.services.SecurityService
import ar2.web.views.GroupViews
import ar2.web.views.PyPIViews
import ar2.web.views.UserViews
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.filter.GzipCompressionMode
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.koin.core.KoinComponent
import org.koin.core.get

class WebHandler : KoinComponent {

    private val userViews: UserViews = get()
    private val groupViews: GroupViews = get()
    private val pyPIViews: PyPIViews = get()
    private val securityService: SecurityService = get()

    fun toHttpHandler(): HttpHandler = ServerFilters.GZip(compressionMode = GzipCompressionMode.Streaming)
            .then(ExceptionHandler())
            .then(ServerFilters.InitialiseRequestContext(context))
            .then(LookupSessionTokenFilter())
            .then(
                routes("/api" bind apiRoutes()))
    fun apiRoutes() = routes(
        "/v1/login" bind Method.POST to userViews::authenticate,
        "/v1/users" bind securityService.requireSession().then(userViews.views()),
        "/v1/groups" bind securityService.requireSession().then(groupViews.views()),
        "/py/{group}/{repo}" bind pyPIViews.views()
    )
}

fun App.getWebHandler() = WebHandler().toHttpHandler()
