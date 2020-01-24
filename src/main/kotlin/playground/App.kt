package playground

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
import org.koin.core.inject
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import playground.pypi.PyPIViews
import playground.security.SecurityService
import playground.security.SecurityServiceImpl
import java.lang.Exception
import java.nio.file.FileSystems
import java.nio.file.Files


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
                        "/py/{group}/{repo}/upload" bind POST to securityService.basicAuth()
                                .then(ServerFilters.CatchLensFailure())
                                .then(pypiViews.upload())
                )
            )
        )
    }

    fun loadConfig(fileName: String): Config {
        val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
        return Files.newBufferedReader(FileSystems.getDefault().getPath(fileName)).use { mapper.readValue(it, Config::class.java) }
    }

    fun startServer(): Http4kServer {
        val server = getHandler().asServer(Undertow(config.listen.port));
        log.info("Launching server at port {}", config.listen.port)
        return server.start()
    }
}

fun main(args: Array<String>) {
    val lc = LoggerFactory.getILoggerFactory() as LoggerContext
    lc.getLogger(Logger.ROOT_LOGGER_NAME).level = Level.WARN
    lc.getLogger("playground").level = Level.INFO

    val koinApp = startKoin { modules(modules) }.koin
    val app = App()
    val fileName: String
    fileName = if (args.isEmpty()) "example-config.yaml" else args[0]
    app.config = app.loadConfig(fileName)
    koinApp.declare(app.config)
    app.startServer().block()
}
