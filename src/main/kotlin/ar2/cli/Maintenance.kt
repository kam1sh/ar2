package ar2.cli

import ar2.App
import ar2.services.SessionsService
import com.github.ajalt.clikt.core.CliktCommand
import java.time.LocalDateTime
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory

class Maintenance(val app: App) : CliktCommand(), KoinComponent {
    val log = LoggerFactory.getLogger(javaClass)

    val sessionsService: SessionsService by inject()

    override fun run() {
        sessionsService.pruneOld(LocalDateTime.now())
    }
}
