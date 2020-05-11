package ar2.cli

import ar2.App
import ar2.services.SessionsService
import com.github.ajalt.clikt.core.CliktCommand
import java.time.LocalDateTime
import org.koin.core.KoinComponent
import org.koin.core.inject

class Maintenance(val app: App) : CliktCommand(), KoinComponent {
    private val sessionsService: SessionsService by inject()

    override fun run() {
        sessionsService.pruneOld(LocalDateTime.now())
    }
}
