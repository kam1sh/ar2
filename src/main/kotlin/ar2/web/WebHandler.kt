package ar2.web

import ar2.App
import ar2.services.SecurityService
import ar2.web.views.PyPIViews
import ar2.web.views.RepositoryGroupViews
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

class WebHandler(context: KoinComponent) {

    private val userViews: UserViews = context.get()
    private val groupViews: RepositoryGroupViews = context.get()
    private val pyPIViews: PyPIViews = context.get()
    private val securityService: SecurityService = context.get()

    fun toHttpHandler(): HttpHandler = ServerFilters.GZip(compressionMode = GzipCompressionMode.Streaming)
            .then(ExceptionHandler())
            .then(ServerFilters.InitialiseRequestContext(context))
            .then(LookupSessionTokenFilter()())
            .then(routes(
                    "/login" bind Method.POST to userViews::authenticate,
                    "/users" bind securityService.requireSession().then(userViews.views()),
                    "/groups" bind securityService.requireSession().then(groupViews.views()),
                    "/py/{group}/{repo}" bind pyPIViews.views()
            ))
}

fun App.getWebHandler() = WebHandler(this).toHttpHandler()
