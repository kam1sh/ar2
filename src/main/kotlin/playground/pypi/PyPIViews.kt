package playground.pypi

import org.http4k.core.*
import org.http4k.lens.MultipartFormFile
import org.http4k.routing.path
import org.koin.core.KoinComponent
import org.koin.core.get
import playground.Config
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PyPIViews: KoinComponent {

    val cfg: Config = get()

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
                                fields.put(next.name, next.value)
                                println("${next.name} = ${next.value}")
                            }
                        }
                    }
                }
            }
            resp = Response(Status.OK).body("Uploaded!\n")
        } catch (exist: PackageExists) {
            resp = Response(Status.CONFLICT).body("Package already uploaded")
        }
        resp
    }

    private fun uploadFile(file: MultipartFormFile, group: String, repo: String) {
        val path = Paths.get(cfg.storage.path, file.filename)
        if (path.toFile().exists())
            throw PackageExists()
        Files.copy(file.content, path)

    }

}

class PackageExists: Exception()