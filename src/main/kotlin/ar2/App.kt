package ar2

import ar2.cli.CreateAdmin
import ar2.cli.Serve
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.http4k.core.*
import org.http4k.core.Method.POST
import org.http4k.filter.GzipCompressionMode
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.get
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ar2.views.PyPIViews
import ar2.security.SecurityService
import ar2.security.SecurityServiceImpl
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


val modules = module {
    single { SecurityServiceImpl() as SecurityService }
    single { PyPIViews() }
}

class App : KoinComponent {

    val log = LoggerFactory.getLogger(App::class.java)

    lateinit var config: Config

    val pypiViews: PyPIViews = get()

    val securityService: SecurityService = get()

    fun getHandler(): HttpHandler {

        fun catchErrors() = Filter { next: HttpHandler ->
            { req: Request ->
                try {
                    next(req)
                } catch (exc: Exception) {
                    log.error("Caught exception:", exc)
                    Response(Status.INTERNAL_SERVER_ERROR)
                }
            }
        }

        return ServerFilters.GZip(compressionMode = GzipCompressionMode.Streaming).then(
                catchErrors().then(routes(
//                        "/users"
                        "/py/{group}/{repo}/upload" bind POST to securityService.basicAuth()
                                .then(ServerFilters.CatchLensFailure())
                                .then(pypiViews.upload())
                )
            )
        )
    }

    fun loadConfig(file: File?) {
        val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
        config = (file ?: Paths.get("config.yaml").toFile())
                .bufferedReader()
                .use { mapper.readValue(it, Config::class.java) }
        getKoin().declare(config)
    }

    fun startServer(): Http4kServer {
        val server = getHandler().asServer(Undertow(config.listen.port));
        log.info("Launching server at port {}", config.listen.port)
        return server.start()
    }
}
class CliApp(val app: App): CliktCommand() {
    val config: File? by option(help = "Path to configuration file").file(exists = true, fileOkay = true)
    override fun run() {
        app.loadConfig(config)
    }
}

fun main(args: Array<String>) {
    val lc = LoggerFactory.getILoggerFactory() as LoggerContext
    lc.getLogger(Logger.ROOT_LOGGER_NAME).level = Level.WARN
    lc.getLogger("ar2").level = Level.INFO

    val koinApp = startKoin { modules(modules) }.koin
    val app = App()
    CliApp(app)
            .subcommands(Serve(app), CreateAdmin(app))
            .main(args)
}
