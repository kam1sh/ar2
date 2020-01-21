package playground

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.http4k.core.*
import org.http4k.core.Method.POST
import org.http4k.filter.GzipCompressionMode
import org.http4k.filter.ServerFilters
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.Validator
import org.http4k.lens.multipartForm
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject
import org.koin.dsl.module
import playground.security.SecurityService
import playground.security.SecurityServiceImpl
import java.nio.file.FileSystems
import java.nio.file.Files


val modules = module {
    single { SecurityServiceImpl() as SecurityService }
    single { Upload() }
}

class App : KoinComponent {

    lateinit var config: Config

    val upload by inject<Upload>()

    fun getHandler(): HttpHandler {
        return ServerFilters.GZip(compressionMode = GzipCompressionMode.Streaming).then(routes(
                "/py/upload" bind POST to ServerFilters.BasicAuth("Anchor2 authentication",
                        { authorize: Credentials -> authorize == Credentials("0", "1") }
                ).then(upload())
        ))
    }

    fun loadConfig(fileName: String): Config {
        val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
        return Files.newBufferedReader(FileSystems.getDefault().getPath(fileName)).use { mapper.readValue(it, Config::class.java) }
    }

    fun startServer(): Http4kServer {
        val server = getHandler().asServer(Undertow(config.listen.port));
        println("Launching server at port ${config.listen.port}")
        return server.start()
    }
}

class Upload() {
    private val files = MultipartFormFile.multi.required("file")
    private val form = Body.multipartForm(Validator.Strict, files).toLens()

    operator fun invoke(): HttpHandler = { request ->
        Response(Status.OK).body("Uploaded!\n")
    }
}

fun main(args: Array<String>) {
    startKoin { modules(modules) }
    val app = App()
    val fileName: String
    fileName = if (args.isEmpty()) "example-config.yaml" else args[0]
    app.config = app.loadConfig(fileName)
    app.startServer().block()
}
