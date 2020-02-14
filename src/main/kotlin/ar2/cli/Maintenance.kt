package ar2.cli

import ar2.App
import ar2.db.Sessions
import com.github.ajalt.clikt.core.CliktCommand
import org.slf4j.LoggerFactory

class Maintenance(val app: App) : CliktCommand() {
    val log = LoggerFactory.getLogger(javaClass)

    override fun run() {
        Sessions.pruneOld()
    }
}
