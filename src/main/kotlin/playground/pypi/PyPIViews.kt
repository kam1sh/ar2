package playground.pypi

import org.http4k.core.*
import org.koin.core.KoinComponent
import org.koin.core.get
import playground.Config
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths

class Upload: KoinComponent {

    val cfg: Config = get()

    operator fun invoke(): HttpHandler = { request ->
        var resp: Response
        try {
            request.multipartIterator().asSequence().fold(emptyList<MultipartEntity.Field>()) { memo, next ->
                when (next) {
                    is MultipartEntity.File -> {
                        val path = Paths.get(cfg.storage.path, next.file.filename)
                        if (path.toFile().exists())
                            throw PackageExists()
                        Files.copy(next.file.content, path)
                        memo
                    }
                    is MultipartEntity.Field -> memo.plus(next)
                }
            }
            resp = Response(Status.OK).body("Uploaded!\n")
        } catch (exist: PackageExists) {
            resp = Response(Status.CONFLICT).body("Package already uploaded")
        }
        resp
    }

}

class PackageExists: Exception()