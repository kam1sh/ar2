package ar2.web.views

import ar2.db.entities.assertAdmin
import ar2.services.extractUser
import org.http4k.core.*
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.koin.core.KoinComponent


class ManagementViews : KoinComponent {
    fun views() = routes(
        "/info" bind Method.GET to ::getStats,
        "/gc" bind Method.POST to ::doGC
    )

    // https://stackoverflow.com/a/18375641/8282597
    data class Stats(
        val jvmVersion: String,
        // -Xmx value
        val maxMemory: Long,
        // space reserved for the java process
        val totalMemory: Long,
        // space ready for new objects
        val freeMemory: Long
    )
    val statsLens = Body.auto<Stats>().toLens()
    fun getStats(request: Request): Response {
        extractUser(request).assertAdmin()
        val runtime = Runtime.getRuntime()
        val stats = Stats(
            jvmVersion = Runtime.version().toString(),
            maxMemory = runtime.maxMemory(),
            totalMemory = runtime.totalMemory(),
            freeMemory = runtime.freeMemory()
        )
        return statsLens(stats, Response(Status.OK))
    }

    fun doGC(request: Request): Response {
        extractUser(request).assertAdmin()
        val runtime = Runtime.getRuntime()
        runtime.gc()
        return Response(Status.NO_CONTENT)
    }
}