package playground.pypi

import org.http4k.core.*
import org.http4k.lens.MultipartFormFile
import org.http4k.routing.path
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.slf4j.LoggerFactory
import playground.Config
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PyPIViews: KoinComponent {

    val log = LoggerFactory.getLogger(PyPIViews::class.java)

    val cfg: Config by inject()

    fun upload(): HttpHandler = { request ->
        val group = request.path("group")!!
        val repo = request.path("repo")!!
        var resp: Response
        try {
            val classifiers = ArrayList<String>()
            val requiresDist = ArrayList<String>()
            val fields = HashMap<String, String>()
            for (next in request.multipartIterator()) {
                when (next) {
                    is MultipartEntity.File -> uploadFile(next.file, group, repo)
                    is MultipartEntity.Field -> {
                        when (next.name) {
                            "classifiers" -> classifiers.add(next.value)
                            "requires_dist" -> requiresDist.add(next.value)
                            else -> {
                                fields[next.name] = next.value
                                log.debug("{} = {}", next.name, next.value)
                            }
                        }
                    }
                }
            }
            resp = Response(Status.CREATED).body("Uploaded!\n")
        } catch (exist: PackageExists) {
            resp = Response(Status.CONFLICT).body("This package is already uploaded")
        }
        resp
    }

    private fun uploadFile(file: MultipartFormFile, group: String, repo: String) {
        var path = Paths.get(cfg.storage.path)
        for (item in setOf(group, repo)) {
            path = path.resolve(item)
            if (!Files.exists(path))
                Files.createDirectory(path)
            else if (!Files.isDirectory(path))
                throw IOException("$path is not a directory")
        }
        path = path.resolve(file.filename)
        if (path.toFile().exists())
            throw PackageExists()
        Files.copy(file.content, path)
    }

}

class PackageExists: Exception()
