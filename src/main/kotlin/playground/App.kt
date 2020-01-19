package playground

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer
import java.nio.file.FileSystems
import java.nio.file.Files


class App {
    fun getHandler(): HttpHandler {
        return routes(
                "/py/upload" bind POST to {req: Request -> upload(req)}
        )
    }

    fun loadConfig(fileName: String): Config {
        val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
        return Files.newBufferedReader(FileSystems.getDefault().getPath(fileName)).use { mapper.readValue(it, Config::class.java) }
    }

    fun startServer(port: Int): Http4kServer {
        val server = getHandler().asServer(Undertow(port))
        return server.start()
    }
}

fun upload(request: Request): Response {
    return Response(Status.OK).body("Uploaded!\n")
}

fun main(args: Array<String>) {
    val app = App()
    val fileName: String
    if(args.isEmpty())
        fileName = "example-config.yaml"
    else
        fileName = args[0]
    val cfg = app.loadConfig(fileName)
    app.startServer(cfg.listen.port).block()
}
