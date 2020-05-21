package ar2.cli

import ar2.App
import ar2.web.WebHandler
import com.github.ajalt.clikt.core.CliktCommand
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.koin.core.get
import org.slf4j.LoggerFactory

class Serve(val app: App) : CliktCommand() {
    private val log = LoggerFactory.getLogger(javaClass)

    private fun startServer(): Http4kServer {
        val server = app.get<WebHandler>().asServer(Undertow(app.config.listen.port))
        log.info("Launching server at port {}.", app.config.listen.port)
        return server.start()
    }

    override fun run() {
        startServer().block()
    }
}
