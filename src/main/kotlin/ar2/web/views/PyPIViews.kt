package ar2.web.views

import ar2.Config
import ar2.exceptions.Ar2Exception
import ar2.exceptions.WebError
import ar2.services.SecurityService
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import org.http4k.core.*
import org.http4k.lens.MultipartFormFile
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory

class PyPIViews(private val securityService: SecurityService) : KoinComponent {

    val log = LoggerFactory.getLogger(javaClass)

    private val cfg: Config by inject()

    fun views() = routes(
            "/upload" bind Method.POST to securityService.basicAuth().then(::upload)
    )

    private fun upload(request: Request): Response {
        val groupName = request.path("group")!!
        val repoName = request.path("repo")!!
        // todo check access

        val classifiers = ArrayList<String>()
        val requiresDist = ArrayList<String>()
        val fields = HashMap<String, String>()

        for (next in request.multipartIterator()) {
            when (next) {
                is MultipartEntity.File -> {
                    uploadFile(next.file, groupName, repoName)
                }
                is MultipartEntity.Field -> {
                    when (next.name) {
                        "classifiers" -> {
                            classifiers.add(next.value)
                        }
                        "requires_dist" -> requiresDist.add(next.value)
                        else -> {
                            fields[next.name] = next.value
                            log.debug("{} = {}", next.name, next.value)
                        }
                    }
                }
            }
        }
        log.debug("classifiers: {}", classifiers)
        log.debug("requires_dist: {}", requiresDist)
        return Response(Status.CREATED).body("Uploaded!\n")
    }

    private fun uploadFile(file: MultipartFormFile, group: String, repo: String) {
        var path = Paths.get(cfg.storage.path, group, repo)
        Files.createDirectories(path)
        path = path.resolve(file.filename)
        if (path.toFile().exists())
            throw PackageExists()
        Files.copy(file.content, path)
    }
}

class PackageExists : Ar2Exception("Package already uploaded.", "PACKAGE_ALREADY_UPLOADED") {
    override fun toHTTPResponse() =
        WebError(this, Status.CONFLICT).toHTTPResponse()
}
